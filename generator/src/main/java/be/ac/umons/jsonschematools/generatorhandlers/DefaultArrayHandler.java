package be.ac.umons.jsonschematools.generatorhandlers;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Type;

public class DefaultArrayHandler implements Handler {

    private final int maxItems;

    public DefaultArrayHandler() {
        maxItems = Integer.MAX_VALUE - 1;
    }

    public DefaultArrayHandler(int maxItems) {
        this.maxItems = maxItems;
    }

    @Override
    public Object generate(Generator generator, JSONSchema schema, int maxTreeSize,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        JSONArray array = new JSONArray();

        if (maxTreeSize == 0) {
            return new JSONArray();
        }

        int minItems = schema.getIntOr("minItems", 0);
        int maxItems = schema.getIntOr("maxItems", this.maxItems);
        if (minItems > maxItems) {
            throw new GeneratorException("Array: minItems can not be strictly greater than maxItems");
        }
        List<JSONSchema> itemsSchemaList = null;
        itemsSchemaList = schema.getItemsArray();
        JSONSchema itemsSchema = itemsSchemaList.get(rand.nextInt(itemsSchemaList.size()));

        int size = rand.nextInt(maxItems - minItems + 1) + minItems;
        if (itemsSchema == null) {
            for (int i = 0 ; i < size ; i++) {
                array.put(new JSONObject());
            }
        }
        else {
            for (int i = 0; i < size; i++) {
                Object value = generator.generateAccordingToConstraints(itemsSchema, maxTreeSize - 1, rand);
                if (!Objects.equals(value, Type.NULL)) {
                    array.put(value);
                }
            }
        }

        return array;
    }

}
