package be.ac.umons.jsonschematools.exploration.generatorhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.exploration.ChoicesSequence;
import be.ac.umons.jsonschematools.exploration.ExplorationGenerator;

/**
 * Generates an array by exploring all the possibilities in the schema.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultArrayHandler extends AHandler {

    private final int maxItems;

    public DefaultArrayHandler() {
        this(Integer.MAX_VALUE - 1);
    }

    public DefaultArrayHandler(int maxItems) {
        this.maxItems = maxItems;
    }

    @Override
    public Optional<Object> generate(final JSONSchema schema, final ExplorationGenerator generator,
            final ChoicesSequence choices) throws JSONSchemaException, JSONException {
        final List<JSONArray> forbiddenValues = new ArrayList<>(
                schema.getForbiddenValuesFilteredByType(JSONArray.class));

        Optional<Object> value = generateArray(schema, generator, choices);
        if (value.isEmpty()) {
            return value;
        }
        JSONArray array = (JSONArray) value.get();
        for (Object forbidden : forbiddenValues) {
            if (array.similar(forbidden)) {
                return Optional.empty();
            }
        }
        return value;
    }

    private Optional<Object> generateArray(final JSONSchema schema, final ExplorationGenerator generator,
            final ChoicesSequence choices) throws JSONSchemaException, JSONException {
        final JSONArray array = new JSONArray();

        final int minItems, maxItems;
        minItems = schema.getIntOr("minItems", 0);
        maxItems = schema.getIntOr("maxItems", this.maxItems);

        if (schema.getConstValue() != null) {
            JSONArray constValue = (JSONArray) schema.getConstValue();
            if (!(minItems <= constValue.length() && constValue.length() <= maxItems)) {
                return Optional.empty();
            }
            return Optional.of(AbstractConstants.abstractConstValue(constValue));
        }

        List<JSONSchema> itemsSchemaList = null;
        itemsSchemaList = schema.getItemsArray();
        assert itemsSchemaList.size() == 1;
        JSONSchema itemsSchema = itemsSchemaList.get(0);

        final int length = length(minItems, maxItems, choices);
        if (itemsSchema == null) {
            for (int i = 0; i < length; i++) {
                array.put(new JSONObject());
            }
        } else {
            for (int i = 0; i < length; i++) {
                Optional<Object> value = generator.generateValueAccordingToConstraints(itemsSchema, choices);
                if (value.isEmpty()) {
                    return Optional.empty();
                }
                array.put(value.get());
            }
        }

        return Optional.of(array);
    }

}
