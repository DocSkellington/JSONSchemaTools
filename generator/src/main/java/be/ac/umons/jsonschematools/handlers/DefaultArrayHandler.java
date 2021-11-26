package be.ac.umons.jsonschematools.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Type;

public class DefaultArrayHandler implements Handler<JSONArray> {

    private final int maxItems;

    public DefaultArrayHandler() {
        maxItems = Integer.MAX_VALUE;
    }

    public DefaultArrayHandler(int maxItems) {
        this.maxItems = maxItems;
    }

    @Override
    public <ST, IT, NT, BT, ET, OT extends JSONObject, AT extends JSONArray> JSONArray generate(
            final Generator<ST, IT, NT, BT, ET, OT, AT> generator, final JSONSchema schema, final int maxTreeSize,
            final Random rand) throws JSONSchemaException, GeneratorException {
        JSONArray array = new JSONArray();
        if (maxTreeSize <= 0) {
            return array;
        }

        int minItem = schema.getIntOr("minItems", 0);
        int maxItem = schema.getIntOr("maxItems", this.maxItems);
        if (minItem > maxItem) {
            throw new GeneratorException("Array: minItems can not be strictly greater than maxItems");
        }
        JSONSchema itemsSchema = null;
        try {
            itemsSchema = schema.getItemsArray();
        }
        catch (JSONSchemaException e) {
        }

        List<Type> typeItems = new ArrayList<>(itemsSchema.getTypes());
        
        int size = rand.nextInt(maxItem - minItem) + minItem;
        for (int i = 0 ; i < size ; i++) {
            Type type = typeItems.get(rand.nextInt(typeItems.size()));
            array.put(DefaultObjectHandler.generateValue(type, generator, itemsSchema, maxTreeSize, rand));
        }

        return array;
    }

}
