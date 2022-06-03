package be.ac.umons.jsonschematools.exploration.generatorhandlers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.exploration.ChoicesSequence;
import be.ac.umons.jsonschematools.exploration.ExplorationGenerator;

/**
 * Generated an object by exhaustively exploring the possibilities described in
 * a schema.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultObjectHandler extends AHandler {

    private final int maxProperties;

    public DefaultObjectHandler() {
        this(Integer.MAX_VALUE - 1);
    }

    public DefaultObjectHandler(int maxProperties) {
        this.maxProperties = maxProperties;
    }

    @Override
    public Optional<Object> generate(final JSONSchema schema, final ExplorationGenerator generator,
            int maxDocumentDepth, boolean canGenerateInvalid, final ChoicesSequence choices)
            throws JSONSchemaException, JSONException {
        if (maxDocumentDepth == 0) {
            return Optional.empty();
        }

        final List<JSONObject> forbiddenValues = new ArrayList<>(
                schema.getForbiddenValuesFilteredByType(JSONObject.class));

        final int newMaxDocumentDepth;
        if (maxDocumentDepth == -1) {
            newMaxDocumentDepth = -1;
        } else {
            newMaxDocumentDepth = maxDocumentDepth - 1;
        }
        Optional<Object> value = generateObject(schema, generator, newMaxDocumentDepth, canGenerateInvalid, choices);
        if (value.isEmpty()) {
            return value;
        }
        JSONObject object = (JSONObject) value.get();
        if (!canGenerateInvalid) {
            for (Object forbidden : forbiddenValues) {
                if (object.similar(forbidden)) {
                    return Optional.empty();
                }
            }
        }
        return value;
    }

    private Optional<Object> generateObject(final JSONSchema schema, final ExplorationGenerator generator,
            int maxDocumentDepth, boolean canGenerateInvalid, final ChoicesSequence choices)
            throws JSONSchemaException, JSONException {
        final JSONObject jsonObject = new JSONObject();

        final BiConsumer<String, Optional<Object>> addToDocumentIfNotNullType = (key, value) -> {
            if (value.isPresent() && value != ExplorationGenerator.EMPTY_VALUE_DUE_TO_MAX_DEPTH) {
                jsonObject.put(key, value.get());
            }
        };

        final int minProperties, maxProperties;
        final boolean ignoreMinProperties, ignoreMaxProperties;

        if (canGenerateInvalid) {
            ignoreMinProperties = choices.getNextBooleanValue();
            ignoreMaxProperties = choices.getNextBooleanValue();
        } else {
            ignoreMinProperties = ignoreMaxProperties = false;
        }

        if (ignoreMinProperties) {
            minProperties = 0;
        } else {
            minProperties = schema.getIntOr("minProperties", 0);
        }

        if (ignoreMaxProperties) {
            maxProperties = this.maxProperties;
        } else {
            maxProperties = schema.getIntOr("maxProperties", this.maxProperties);
        }

        if (!canGenerateInvalid && maxProperties < minProperties) {
            return Optional.empty();
        }

        final JSONObject constValue = schema.getConstValueIfType(JSONObject.class);
        if (constValue != null) {
            if (!canGenerateInvalid && !(minProperties <= constValue.length() && constValue.length() <= maxProperties
                    && constValue.keySet().containsAll(schema.getRequiredPropertiesKeys()))) {
                return Optional.empty();
            }
            return Optional.of(AbstractConstants.abstractConstValue(constValue));
        }

        for (Map.Entry<String, JSONSchema> entry : schema.getRequiredProperties().entrySet()) {
            if (canGenerateInvalid && choices.getNextBooleanValue()) {
                // We skip the required property
                continue;
            }
            JSONSchema subSchema = entry.getValue();
            String key = entry.getKey();
            Optional<Object> value = generator.generateValueAccordingToConstraints(subSchema, maxDocumentDepth,
                    canGenerateInvalid, choices);
            if (value.isEmpty()) {
                return Optional.empty();
            }
            addToDocumentIfNotNullType.accept(key, value);
        }

        final int missingProperties = Math.max(0, minProperties - jsonObject.length());

        final Map<String, JSONSchema> nonRequiredProperties = schema.getNonRequiredProperties();
        if (!canGenerateInvalid && missingProperties > nonRequiredProperties.size()) {
            return Optional.empty();
        }

        final List<String> allNonRequiredKeys = new ArrayList<>(nonRequiredProperties.keySet());
        final int maxPropertiesThatCanBeAdded = Math.min(maxProperties, allNonRequiredKeys.size());

        if (maxPropertiesThatCanBeAdded == 0) {
            return Optional.of(jsonObject);
        }

        List<String> selectedKeys = null;
        while (selectedKeys == null) {
            final int numberPropertiesToAdd = length(missingProperties, maxPropertiesThatCanBeAdded, choices);
            selectedKeys = selectOptionalKeys(allNonRequiredKeys, numberPropertiesToAdd, choices);
        }

        for (final String key : selectedKeys) {
            final JSONSchema subSchema = schema.getSubSchemaProperties(key);
            final Optional<Object> value = generator.generateValueAccordingToConstraints(subSchema, maxDocumentDepth,
                    canGenerateInvalid, choices);
            if (value.isEmpty()) {
                return Optional.empty();
            }
            addToDocumentIfNotNullType.accept(key, value);
            nonRequiredProperties.remove(key);
        }

        return Optional.of(jsonObject);
    }

    private List<String> selectOptionalKeys(final List<String> nonRequiredKeys, final int length,
            final ChoicesSequence choices) {
        BitSet selection = choices.getBitSetNextInclusiveSelectionInList(nonRequiredKeys.size(), length);
        if (selection == null) {
            return null;
        }

        return listOfSelectedKeys(nonRequiredKeys, length, selection);
    }

    private List<String> listOfSelectedKeys(final List<String> nonRequiredKeys, final int length,
            final BitSet selection) {
        final List<String> selectedKeys = new ArrayList<>(length);
        int nextBit = 0;
        for (nextBit = selection.nextSetBit(nextBit); nextBit != -1; nextBit = selection.nextSetBit(nextBit + 1)) {
            selectedKeys.add(nonRequiredKeys.get(nextBit));
        }
        assert selectedKeys.size() == length;
        return selectedKeys;
    }
}