package be.ac.umons.jsonschematools.generator.random.handlers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Type;
import be.ac.umons.jsonschematools.generator.random.GeneratorException;
import be.ac.umons.jsonschematools.generator.random.RandomGenerator;

/**
 * An object handler that returns an object in which values are abstracted.
 * 
 * It does not support every keyword that can be used in a schema.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultObjectHandler implements IHandler {

    private final int maxProperties;
    private static final float PROBABILITY_SKIP_REQUIRED = 0.7f;

    public DefaultObjectHandler() {
        this(Integer.MAX_VALUE - 1);
    }

    public DefaultObjectHandler(int maxProperties) {
        this.maxProperties = maxProperties;
    }

    @Override
    public Object generate(RandomGenerator generator, JSONSchema schema, int maxTreeSize, boolean canGenerateInvalid,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        if (maxTreeSize == 0) {
            return new JSONObject();
        }
        final Set<JSONObject> forbiddenValues = schema.getForbiddenValuesFilteredByType(JSONObject.class);
        final boolean generateInvalid = generateInvalid(canGenerateInvalid, rand);

        if (generateInvalid && !forbiddenValues.isEmpty()) {
            return new ArrayList<>(forbiddenValues).get(rand.nextInt(forbiddenValues.size()));
        }

        JSONObject jsonObject = generateObject(generator, schema, maxTreeSize, rand, generateInvalid);

        for (int i = 0; i < 1000; i++) {
            boolean correct = true;
            if (!generateInvalid) {
                for (Object forbidden : forbiddenValues) {
                    if (jsonObject.similar(forbidden)) {
                        correct = false;
                        break;
                    }
                }
            }
            if (correct) {
                return jsonObject;
            } else {
                jsonObject = generateObject(generator, schema, maxTreeSize, rand, generateInvalid);
            }
        }

        throw new GeneratorException("Impossible to generate a valid object with 1000 tries " + schema);
    }

    private JSONObject generateObject(RandomGenerator generator, JSONSchema schema, int maxTreeSize,
            Random rand, boolean generateInvalid) throws JSONSchemaException, GeneratorException, JSONException {
        final JSONObject jsonObject = new JSONObject();

        final BiConsumer<String, Object> addToDocumentIfNotNullType = (key, value) -> {
            if (!Objects.equals(value, Type.NULL)) {
                jsonObject.put(key, value);
            }
        };

        final int newMaxTreeSize = maxTreeSize - 1;
        final int minProperties, maxProperties;
        if (generateInvalid && rand.nextBoolean()) {
            minProperties = 0;
        } else {
            minProperties = schema.getIntOr("minProperties", 0);
        }
        if (generateInvalid && rand.nextBoolean()) {
            maxProperties = this.maxProperties;
        } else {
            maxProperties = schema.getIntOr("maxProperties", this.maxProperties);
        }

        if (!generateInvalid && maxProperties < minProperties) {
            throw new GeneratorException("Impossible to generate an object for schema " + schema
                    + " as minProperties = " + minProperties + " > maxProperties = " + maxProperties);
        }

        final JSONObject constValue = schema.getConstValueIfType(JSONObject.class);
        if (constValue != null) {
            if (!(minProperties <= constValue.length() && constValue.length() <= maxProperties
                    && constValue.keySet().containsAll(schema.getRequiredPropertiesKeys()))) {
                if (generateInvalid) {
                    return constValue;
                } else {
                    throw new GeneratorException("Impossible to generate an object for schema " + schema
                            + " since the const value is incorrect, with regards to minProperties, maxProperties, or required");
                }
            }
            return (JSONObject) AbstractConstants.abstractConstValue(constValue);
        }

        for (Map.Entry<String, JSONSchema> entry : schema.getRequiredProperties().entrySet()) {
            if (generateInvalid && rand.nextFloat() > PROBABILITY_SKIP_REQUIRED) {
                continue;
            }
            JSONSchema subSchema = entry.getValue();
            String key = entry.getKey();
            Object value = generator.generateAccordingToConstraints(subSchema, newMaxTreeSize, generateInvalid, rand);
            addToDocumentIfNotNullType.accept(key, value);
        }

        int missingProperties = Math.max(0, minProperties - jsonObject.length());

        Map<String, JSONSchema> nonRequiredProperties = schema.getNonRequiredProperties();
        if (!generateInvalid && missingProperties > nonRequiredProperties.size()) {
            throw new GeneratorException("Impossible to generate an object for schema " + schema
                    + " as minProperties = " + minProperties + " exceeds the number of defined properties");
        }

        if (generateInvalid) {
            missingProperties = rand.nextInt(missingProperties + 1);
        }

        final Set<String> allNonRequiredKeys = nonRequiredProperties.keySet();
        if (generateInvalid && !allNonRequiredKeys.contains(AbstractConstants.stringConstant) && rand.nextBoolean()) {
            JSONSchema trueSchema = schema.getStore().trueSchema();
            Object value = generator.generateAccordingToConstraints(trueSchema, newMaxTreeSize, generateInvalid, rand);
            addToDocumentIfNotNullType.accept(AbstractConstants.stringConstant, value);
        }

        // First, we randomly select enough properties to satisfy minProperties
        // Second, we randomly pick other properties, as long as we do not exceed
        // maxProperties
        Set<String> selectedKeys = new LinkedHashSet<>();
        List<String> unusedKeys = new ArrayList<>(nonRequiredProperties.keySet());
        while (missingProperties > 0) {
            int id = rand.nextInt(unusedKeys.size());
            selectedKeys.add(unusedKeys.get(id));
            unusedKeys.remove(id);
            missingProperties--;
        }

        for (String key : selectedKeys) {
            if (generateInvalid && rand.nextFloat() > PROBABILITY_SKIP_REQUIRED) {
                continue;
            }
            JSONSchema subSchema = schema.getSubSchemaProperties(key);
            Object value = generator.generateAccordingToConstraints(subSchema, newMaxTreeSize, generateInvalid, rand);
            addToDocumentIfNotNullType.accept(key, value);
            nonRequiredProperties.remove(key);
        }

        if (jsonObject.length() == maxProperties) {
            return jsonObject;
        }

        for (String key : unusedKeys) {
            JSONSchema subSchema = nonRequiredProperties.get(key);
            if (rand.nextBoolean()) {
                Object value = generator.generateAccordingToConstraints(subSchema, newMaxTreeSize, generateInvalid,
                        rand);
                addToDocumentIfNotNullType.accept(key, value);

                if (jsonObject.length() >= maxProperties) {
                    break;
                }
            }
        }

        return jsonObject;
    }
}
