package be.ac.umons.jsonschematools.exploration.generatorhandlers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Type;
import be.ac.umons.jsonschematools.exploration.Choice;
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
            final ChoicesSequence choices) throws JSONSchemaException, JSONException {
        final List<JSONObject> forbiddenValues = new ArrayList<>(
                schema.getForbiddenValuesFilteredByType(JSONObject.class));

        Optional<Object> value = generateObject(schema, generator, choices);
        if (value.isEmpty()) {
            return value;
        }
        JSONObject object = (JSONObject) value.get();
        for (Object forbidden : forbiddenValues) {
            if (object.similar(forbidden)) {
                return Optional.empty();
            }
        }
        return value;
    }

    private Optional<Object> generateObject(final JSONSchema schema, final ExplorationGenerator generator,
            final ChoicesSequence choices) throws JSONSchemaException, JSONException {
        final JSONObject jsonObject = new JSONObject();

        final BiConsumer<String, Optional<Object>> addToDocumentIfNotNullType = (key, value) -> {
            if (value.isPresent() && !Objects.equals(value.get(), Type.NULL)) {
                jsonObject.put(key, value.get());
            }
        };

        final int minProperties, maxProperties;
        minProperties = schema.getIntOr("minProperties", 0);
        maxProperties = schema.getIntOr("maxProperties", this.maxProperties);

        if (maxProperties < minProperties) {
            return Optional.empty();
        }

        if (schema.getConstValue() != null) {
            JSONObject constValue = (JSONObject) schema.getConstValue();
            if (!(minProperties <= constValue.length() && constValue.length() <= maxProperties
                    && constValue.keySet().containsAll(schema.getRequiredPropertiesKeys()))) {
                return Optional.empty();
            }
            return Optional.of(AbstractConstants.abstractConstValue(constValue));
        }

        for (Map.Entry<String, JSONSchema> entry : schema.getRequiredProperties().entrySet()) {
            JSONSchema subSchema = entry.getValue();
            String key = entry.getKey();
            Optional<Object> value = generator.generateValueAccordingToConstraints(subSchema, choices);
            if (value.isEmpty()) {
                return Optional.empty();
            }
            addToDocumentIfNotNullType.accept(key, value);
        }

        final int missingProperties = Math.max(0, minProperties - jsonObject.length());

        final Map<String, JSONSchema> nonRequiredProperties = schema.getNonRequiredProperties();
        if (missingProperties > nonRequiredProperties.size()) {
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
            final Optional<Object> value = generator.generateValueAccordingToConstraints(subSchema, choices);
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
        final BitSet selection;
        if (choices.hasNextChoiceInExploration()) {
            Choice choiceInRun = choices.getNextChoiceInExploration();
            if (choices.hasUnseenValueFurtherInExploration()) {
                selection = choiceInRun.getBitSet();
            } else {
                choices.removeAllChoicesAfterCurrentChoiceInExploration();

                selection = getNextValidBitSetForOptionalKeys(length, choiceInRun);
                if (selection == null) {
                    choices.popLastChoice();
                    return null;
                }
            }
        } else {
            Choice choice = choices.createNewChoice(nonRequiredKeys.size(), false);
            selection = getNextValidBitSetForOptionalKeys(length, choice);
            if (selection == null) {
                choices.popLastChoice();
                return null;
            }
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

    private BitSet getNextValidBitSetForOptionalKeys(final int length, final Choice choice) {
        BitSet selection = null;
        while (selection == null || selection.cardinality() != length) {
            if (choice.hasNextValue()) {
                choice.nextValue();
                selection = choice.getBitSet();
            } else {
                return null;
            }
        }
        return selection;
    }
}
