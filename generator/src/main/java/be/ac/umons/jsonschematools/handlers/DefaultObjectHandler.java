package be.ac.umons.jsonschematools.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;

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
    public Object generate(Generator generator, JSONSchema schema, int maxTreeSize,
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

        for (Map.Entry<String, JSONSchema> entry : schema.getRequiredProperties().entrySet()) {
            JSONSchema subSchema = entry.getValue();
            String key = entry.getKey();
            jsonObject.put(key, generator.generateAccordingToConstraints(subSchema, maxTreeSize, rand));
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
            JSONSchema subSchema = schema.getSubSchemaProperties(key);
            jsonObject.put(key, generator.generateAccordingToConstraints(subSchema, maxTreeSize, rand));
            nonRequiredProperties.remove(key);
        }

        if (jsonObject.length() >= maxProperties) {
            return jsonObject;
        }

        for (Map.Entry<String, JSONSchema> entry : nonRequiredProperties.entrySet()) {
            JSONSchema subSchema = entry.getValue();
            String key = entry.getKey();
            if (rand.nextBoolean()) {
                jsonObject.put(key, generator.generateAccordingToConstraints(subSchema, maxTreeSize, rand));

                if (jsonObject.length() >= maxProperties) {
                    break;
                }
            }
        }

        return jsonObject;
    }

}
