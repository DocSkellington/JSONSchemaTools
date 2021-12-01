package be.ac.umons.jsonschematools.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import be.ac.umons.jsonschematools.Constraints;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Type;

public class DefaultArrayHandler implements Handler {

    private final int maxItems;

    public DefaultArrayHandler() {
        maxItems = Integer.MAX_VALUE;
    }

    public DefaultArrayHandler(int maxItems) {
        this.maxItems = maxItems;
    }

    @Override
    public Object generate(Generator generator, Constraints constraints, JSONSchema schema, int maxTreeSize,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        JSONArray array = new JSONArray();
        if (maxTreeSize <= 0) {
            return array;
        }

        int minItems = schema.getIntOr("minItems", 0);
        int maxItems = schema.getIntOr("maxItems", this.maxItems);
        if (minItems > maxItems) {
            throw new GeneratorException("Array: minItems can not be strictly greater than maxItems");
        }
        JSONSchema itemsSchema = null;
        try {
            itemsSchema = schema.getItemsArray();
        } catch (JSONSchemaException e) {
        }

        List<Type> typeItems = new ArrayList<>(itemsSchema.getAllowedTypes());
        
        int size = rand.nextInt(maxItems - minItems + 1) + minItems;
        for (int i = 0; i < size; i++) {
            Type type = typeItems.get(rand.nextInt(typeItems.size()));
            array.put(DefaultObjectHandler.generateValue(type, generator, itemsSchema, maxTreeSize, rand));
        }

        return array;
    }

}
