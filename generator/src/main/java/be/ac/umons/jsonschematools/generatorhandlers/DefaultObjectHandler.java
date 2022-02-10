package be.ac.umons.jsonschematools.generatorhandlers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Type;

/**
 * An object handler that returns an object in which elements are abstracted.
 * 
 * It does not support every keyword that can be used in a schema.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultObjectHandler implements Handler {

    private final int maxProperties;

    public DefaultObjectHandler() {
        maxProperties = Integer.MAX_VALUE;
    }

    public DefaultObjectHandler(int maxProperties) {
        this.maxProperties = maxProperties;
    }

    @Override
    public Object generate(Generator generator, JSONSchema schema, int maxTreeSize,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        JSONObject jsonObject = generateObject(generator, schema, maxTreeSize, rand);

        for (int i = 0 ; i < 1000 ; i++) {
            boolean correct = true;
            for (Object forbidden : schema.getForbiddenValues()) {
                if (jsonObject.similar(forbidden)) {
                    correct = false;
                    break;
                }
            }
            if (correct) {
                return jsonObject;
            }
            else {
                jsonObject = generateObject(generator, schema, maxTreeSize, rand);
            }
        }

        throw new GeneratorException("Impossible to generate a valid object with 1000 tries " + schema);
    }

    private JSONObject generateObject(Generator generator, JSONSchema schema, int maxTreeSize,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        final JSONObject jsonObject = new JSONObject();
        final int newMaxTreeSize = maxTreeSize - 1;
        int minProperties = schema.getIntOr("minProperties", 0);
        int maxProperties = schema.getIntOr("maxProperties", this.maxProperties);
        if (maxProperties < minProperties) {
            throw new GeneratorException("Impossible to generate an object for schema " + schema
                    + " as minProperties = " + minProperties + " > maxProperties = " + maxProperties);
        }

        if (schema.getConstValue() != null) {
            JSONObject constValue = (JSONObject)schema.getConstValue();
            if (!(minProperties <= constValue.length() && constValue.length() <= maxProperties && constValue.keySet().containsAll(schema.getRequiredPropertiesKeys()))) {
                throw new GeneratorException("Impossible to generate an object for schema " + schema + " since the const value is incorrect, with regards to minProperties, maxProperties, or required");
            }
            return (JSONObject)AbstractConstants.abstractConstValue(constValue);
        }

        for (Map.Entry<String, JSONSchema> entry : schema.getRequiredProperties().entrySet()) {
            JSONSchema subSchema = entry.getValue();
            String key = entry.getKey();
            Object value = generator.generateAccordingToConstraints(subSchema, newMaxTreeSize, rand);
            if (!Objects.equals(value, Type.NULL)) {
                jsonObject.put(key, value);
            }
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
        Set<String> selectedProperties = new LinkedHashSet<>();
        List<String> allKeys = new ArrayList<>(nonRequiredProperties.keySet());
        while (missingProperties > 0) {
            int id = rand.nextInt(allKeys.size());
            selectedProperties.add(allKeys.get(id));
            allKeys.remove(id);
            missingProperties--;
        }

        for (String key : selectedProperties) {
            JSONSchema subSchema = schema.getSubSchemaProperties(key);
            Object value = generator.generateAccordingToConstraints(subSchema, newMaxTreeSize, rand);
            if (!Objects.equals(value, Type.NULL)) {
                jsonObject.put(key, value);
            }
            nonRequiredProperties.remove(key);
        }

        if (jsonObject.length() >= maxProperties) {
            return jsonObject;
        }

        for (Map.Entry<String, JSONSchema> entry : nonRequiredProperties.entrySet()) {
            JSONSchema subSchema = entry.getValue();
            String key = entry.getKey();
            if (rand.nextBoolean()) {
                Object value = generator.generateAccordingToConstraints(subSchema, newMaxTreeSize, rand);
                if (!Objects.equals(value, Type.NULL)) {
                    jsonObject.put(key, value);
                }

                if (jsonObject.length() >= maxProperties) {
                    break;
                }
            }
        }

        return jsonObject;
    }
}
