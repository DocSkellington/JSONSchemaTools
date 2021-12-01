package be.ac.umons.jsonschematools.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.Constraints;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Type;

public class DefaultObjectHandler implements Handler {

    private final int maxProperties;

    public DefaultObjectHandler() {
        maxProperties = Integer.MAX_VALUE;
    }

    public DefaultObjectHandler(int maxProperties) {
        this.maxProperties = maxProperties;
    }

    // TODO: handle dependentRequired
    @Override
    public Object generate(Generator generator, Constraints constraints, JSONSchema schema, int maxTreeSize,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        JSONObject jsonObject = new JSONObject();
        if (maxTreeSize <= 0) {
            return jsonObject;
        }

        int minProperties = schema.getIntOr("minProperties", 0);
        int maxProperties = schema.getIntOr("maxProperties", this.maxProperties);
        if (maxProperties < minProperties) {
            throw new GeneratorException("Impossible to generate an object for schema " + schema
                    + " as minProperties = " + minProperties + " > maxProperties = " + maxProperties);
        }

        // TODO: for anyOf: randomly select one

        for (Map.Entry<String, JSONSchema> entry : schema.getRequiredProperties().entrySet()) {
            JSONSchema subSchema = entry.getValue();
            String key = entry.getKey();

            Set<Type> allowedTypes = subSchema.getAllowedTypes();
            Constraints allOf = subSchema.getAllOf();
            allowedTypes.retainAll(allOf.getAllowedTypes());

            Type type = new ArrayList<>(allowedTypes).get(rand.nextInt(allowedTypes.size()));
            jsonObject.put(key, generateValue(type, allOf, generator, subSchema, maxTreeSize, rand));
        }

        int missingProperties = Math.max(0, minProperties - jsonObject.length());

        Map<String, JSONSchema> nonRequiredProperties = schema.getNonRequiredProperties();
        if (missingProperties > nonRequiredProperties.size()) {
            throw new GeneratorException("Impossible to generate an object for schema " + schema
                    + " as minProperties = " + minProperties + " exceeds the number of defined properties");
        }

        // First, we randomly select enough properties to satisfy minProperties
        // Second, we randomly pick other properties, as long as we do not exceed
        // maxProperties
        Set<String> selectedProperties = new HashSet<>();
        List<String> allKeys = new ArrayList<>(nonRequiredProperties.keySet());
        while (missingProperties > 0) {
            int id = rand.nextInt(allKeys.size());
            selectedProperties.add(allKeys.get(id));
            allKeys.remove(id);
            missingProperties--;
        }

        for (String key : selectedProperties) {
            // TODO: generate object
            jsonObject.put(key, nonRequiredProperties.get(key));
            nonRequiredProperties.remove(key);
        }

        for (Map.Entry<String, JSONSchema> entry : nonRequiredProperties.entrySet()) {
            JSONSchema subSchema = entry.getValue();
            String key = entry.getKey();
            if (rand.nextBoolean()) {
                Set<Type> allowedTypes = subSchema.getAllowedTypes();
                allowedTypes.retainAll(subSchema.getAllOf().getAllowedTypes());
                Type type = new ArrayList<>(allowedTypes).get(rand.nextInt(allowedTypes.size()));
                // TODO: handle constraints
                jsonObject.put(key, generateValue(type, new Constraints(), generator, subSchema, maxTreeSize, rand));

                if (jsonObject.length() >= maxProperties) {
                    break;
                }
            }
        }

        return jsonObject;
    }

    private Constraints mergeConstraints(Constraints allOf, Constraints anyOf, Constraints oneOf) {
        Constraints mergedConstraints = new Constraints();

        return mergedConstraints;
    }

    private Map<String, JSONSchema> getRequiredProperties(JSONSchema schema, Constraints allOf, Constraints anyOf, Constraints oneOf) {

        return null;
    }

    static Object generateValue(Type type, Constraints constraints, Generator generator, JSONSchema schema, int maxTreeSize, Random rand)
            throws JSONSchemaException, JSONException, GeneratorException {
        switch (type) {
            case BOOLEAN:
                return generator.getBooleanHandler().generate(generator, constraints, schema, maxTreeSize, rand);
            case ENUM:
                return generator.getEnumHandler().generate(generator, constraints, schema, maxTreeSize, rand);
            case INTEGER:
                return generator.getIntegerHandler().generate(generator, constraints, schema, maxTreeSize, rand);
            case NUMBER:
                return generator.getNumberHandler().generate(generator, constraints, schema, maxTreeSize, rand);
            case STRING:
                return generator.getStringHandler().generate(generator, constraints, schema, maxTreeSize, rand);
            case ARRAY:
                return generator.getArrayHandler().generate(generator, constraints, schema, maxTreeSize - 1, rand);
            case OBJECT:
                return generator.getObjectHandler().generate(generator, constraints, schema, maxTreeSize - 1, rand);
            default:
                return null;
        }
    }

}
