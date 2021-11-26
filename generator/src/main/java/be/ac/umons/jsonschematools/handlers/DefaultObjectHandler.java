package be.ac.umons.jsonschematools.handlers;

import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Type;

public class DefaultObjectHandler implements Handler<JSONObject> {

    @Override
    public <ST, IT, NT, BT, ET, OT extends JSONObject, AT extends JSONArray> JSONObject generate(
            Generator<ST, IT, NT, BT, ET, OT, AT> generator, JSONSchema schema, int maxTreeSize, Random rand) throws JSONException, JSONSchemaException, GeneratorException {
        JSONObject jsonObject = new JSONObject();
        if (maxTreeSize <= 0) {
            return jsonObject;
        }

        for (Map.Entry<String, JSONSchema> entry : schema.getRequiredProperties().entrySet()) {
            JSONSchema s = entry.getValue();
            String key = entry.getKey();

            Type type = s.getListTypes().get(rand.nextInt(s.getTypes().size()));
            jsonObject.put(key, generateValue(type, generator, s, maxTreeSize, rand));
        }

        for (Map.Entry<String, JSONSchema> entry : schema.getNonRequiredProperties().entrySet()) {
            JSONSchema s = entry.getValue();
            String key = entry.getKey();
            if (rand.nextBoolean()) {
                Type type = s.getListTypes().get(rand.nextInt(s.getTypes().size()));
                jsonObject.put(key, generateValue(type, generator, s, maxTreeSize, rand));
            }
        }

        return jsonObject;
    }

    static <ST, IT, NT, BT, ET, OT extends JSONObject, AT extends JSONArray> Object generateValue(Type type, Generator<ST, IT, NT, BT, ET, OT, AT> generator, JSONSchema schema, int maxTreeSize, Random rand) throws JSONSchemaException, JSONException, GeneratorException {
        switch (type) {
        case BOOLEAN:
            return generator.getBooleanHandler().generate(generator, schema, maxTreeSize, rand);
        case ENUM:
            return generator.getEnumHandler().generate(generator, schema, maxTreeSize, rand);
        case INTEGER:
            return generator.getIntegerHandler().generate(generator, schema, maxTreeSize, rand);
        case NUMBER:
            return generator.getNumberHandler().generate(generator, schema, maxTreeSize, rand);
        case STRING:
            return generator.getStringHandler().generate(generator, schema, maxTreeSize, rand);
        case ARRAY:
            return generator.getArrayHandler().generate(generator, schema, maxTreeSize - 1, rand);
        case OBJECT:
            return generator.getObjectHandler().generate(generator, schema, maxTreeSize - 1, rand);
        default:
            return null;
        }
    }
    
}
