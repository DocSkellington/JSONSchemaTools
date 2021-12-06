package be.ac.umons.jsonschematools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.handlers.Handler;

public class Generator {

    private final Handler stringHandler;
    private final Handler integerHandler;
    private final Handler numberHandler;
    private final Handler booleanHandler;
    private final Handler enumHandler;
    private final Handler objectHandler;
    private final Handler arrayHandler;

    public Generator(final Handler stringHandler, final Handler integerHandler, final Handler numberHandler,
            final Handler booleanHandler, final Handler enumHandler, final Handler objectHandler,
            final Handler arrayHandler) {
        this.stringHandler = stringHandler;
        this.integerHandler = integerHandler;
        this.numberHandler = numberHandler;
        this.booleanHandler = booleanHandler;
        this.enumHandler = enumHandler;
        this.objectHandler = objectHandler;
        this.arrayHandler = arrayHandler;
    }

    public static JSONSchema getActualSchema(final JSONSchema schema, Random rand) throws JSONSchemaException {
        JSONSchema allOf = schema.getAllOf();
        List<JSONSchema> anyOfList = schema.getAnyOf();
        List<JSONSchema> oneOfList = schema.getOneOf();
        JSONSchema anyOf = anyOfList.get(rand.nextInt(anyOfList.size()));
        JSONSchema oneOf = oneOfList.get(rand.nextInt(oneOfList.size()));

        return schema.merge(allOf).merge(anyOf).merge(oneOf);
    }

    public JSONObject generate(final JSONSchema schema, final int maxTreeSize)
            throws JSONSchemaException, JSONException, GeneratorException {
        return generate(schema, maxTreeSize, new Random());
    }

    public JSONObject generate(final JSONSchema schema, final int maxTreeSize, final Random rand)
            throws JSONSchemaException, JSONException, GeneratorException {
        return generateRoot(schema, maxTreeSize, rand);
    }

    private JSONObject generateRoot(final JSONSchema schema, final int maxTreeSize, final Random rand) throws JSONSchemaException, GeneratorException {
        final JSONSchema finalSchema = getActualSchema(schema, rand);
        List<Type> types = finalSchema.getListTypes();
        if (!types.contains(Type.OBJECT)) {
            throw new GeneratorException("Impossible to generate a document since the top element of a document must be an object. Allowed types " + types);
        }
        return (JSONObject) generateValue(Type.OBJECT, schema, maxTreeSize + 1, rand);
    }

    public Handler getStringHandler() {
        return stringHandler;
    }

    public Handler getIntegerHandler() {
        return integerHandler;
    }

    public Handler getNumberHandler() {
        return numberHandler;
    }

    public Handler getObjectHandler() {
        return objectHandler;
    }

    public Handler getArrayHandler() {
        return arrayHandler;
    }

    public Handler getBooleanHandler() {
        return booleanHandler;
    }

    public Handler getEnumHandler() {
        return enumHandler;
    }

    public Object generateAccordingToConstraints(JSONSchema schema, int maxTreeSize, Random rand) throws JSONException, JSONSchemaException, GeneratorException {
        if (schema.hasKey(Keys.dueToMergeKey)) {
            return generateAccordingToConstraints(schema.getSubSchemaInAllOfDueToMerge("allOf"), maxTreeSize, rand);
        }

        Set<Type> allowedTypes = schema.getAllowedTypes();

        Type type = new ArrayList<>(allowedTypes).get(rand.nextInt(allowedTypes.size()));
        return generateValue(type, schema, maxTreeSize, rand);
    }

    public Object generateValue(Type type, JSONSchema schema, int maxTreeSize, Random rand)
            throws JSONSchemaException, JSONException, GeneratorException {
        switch (type) {
            case BOOLEAN:
                return getBooleanHandler().generate(this, schema, maxTreeSize, rand);
            case ENUM:
                return getEnumHandler().generate(this, schema, maxTreeSize, rand);
            case INTEGER:
                return getIntegerHandler().generate(this, schema, maxTreeSize, rand);
            case NUMBER:
                return getNumberHandler().generate(this, schema, maxTreeSize, rand);
            case STRING:
                return getStringHandler().generate(this, schema, maxTreeSize, rand);
            case ARRAY:
                return getArrayHandler().generate(this, schema, maxTreeSize - 1, rand);
            case OBJECT:
                return getObjectHandler().generate(this, schema, maxTreeSize - 1, rand);
            default:
                return null;
        }
    }
}
