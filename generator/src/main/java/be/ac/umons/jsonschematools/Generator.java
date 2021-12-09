package be.ac.umons.jsonschematools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static JSONSchema getMergedSchema(final JSONSchema baseSchema, final JSONSchema allOf,
            final JSONSchema anyOf, final JSONSchema oneOf, final JSONSchema not) throws JSONSchemaException {
        return baseSchema.dropAllOfAnyOfOneOfAndNot().merge(allOf).merge(anyOf).merge(oneOf).merge(not);
    }

    public JSONObject generate(final JSONSchema schema, final int maxTreeSize)
            throws JSONSchemaException, JSONException, GeneratorException {
        return generate(schema, maxTreeSize, new Random());
    }

    public JSONObject generate(final JSONSchema schema, final int maxTreeSize, final Random rand)
            throws JSONSchemaException, JSONException, GeneratorException {
        return generateRoot(schema, maxTreeSize, rand);
    }

    private JSONObject generateRoot(final JSONSchema schema, final int maxTreeSize, final Random rand)
            throws JSONSchemaException, GeneratorException {
        final JSONSchema allOf = schema.getAllOf();
        final List<JSONSchema> anyOfList = schema.getAnyOf();
        final List<JSONSchema> oneOfList = schema.getOneOf();
        final List<JSONSchema> notList = schema.getNot();

        final List<Integer> indicesAnyOf = generateIndicesRandomOrder(anyOfList, rand);
        final List<Integer> indicesOneOf = generateIndicesRandomOrder(oneOfList, rand);
        final List<Integer> indicesNot = generateIndicesRandomOrder(notList, rand);

        for (final int indexAnyOf : indicesAnyOf) {
            final JSONSchema anyOf = anyOfList.get(indexAnyOf);
            for (final int indexOneOf : indicesOneOf) {
                final JSONSchema oneOf = oneOfList.get(indexOneOf);
                for (final int indexNot : indicesNot) {
                    final JSONSchema not = notList.get(indexNot);
                    final JSONSchema fullSchema = getMergedSchema(schema, allOf, anyOf, oneOf, not);
                    List<Type> types = fullSchema.getListTypes();
                    if (!types.contains(Type.OBJECT)) {
                        continue;
                    }
                    try {
                        return (JSONObject) generateValue(Type.OBJECT, fullSchema, maxTreeSize, rand);
                    } catch (GeneratorException e) {
                        // The choice we made lead to an invalid schema. We retry with a different
                        // choice
                    }
                }
            }
        }
        throw new GeneratorException("Impossible to generate a document: all tries failed for the schema " + schema);
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

    public Object generateAccordingToConstraints(JSONSchema schema, int maxTreeSize, Random rand)
            throws JSONException, JSONSchemaException, GeneratorException {
        final JSONSchema allOf = schema.getAllOf();
        final List<JSONSchema> anyOfList = schema.getAnyOf();
        final List<JSONSchema> oneOfList = schema.getOneOf();
        final List<JSONSchema> notList = schema.getNot();

        final List<Integer> indicesAnyOf = generateIndicesRandomOrder(anyOfList, rand);
        final List<Integer> indicesOneOf = generateIndicesRandomOrder(oneOfList, rand);
        final List<Integer> indicesNot = generateIndicesRandomOrder(notList, rand);

        for (final int indexAnyOf : indicesAnyOf) {
            final JSONSchema anyOf = anyOfList.get(indexAnyOf);
            for (final int indexOneOf : indicesOneOf) {
                final JSONSchema oneOf = oneOfList.get(indexOneOf);
                for (final int indexNot : indicesNot) {
                    final JSONSchema not = notList.get(indexNot);
                    final JSONSchema fullSchema = getMergedSchema(schema, allOf, anyOf, oneOf, not);
                    try {
                        // If we still have some constraints behind "allOf", "anyOf", "oneOf", or "not",
                        // we unfold them
                        if (fullSchema.needsFurtherUnfolding()) {
                            return generateAccordingToConstraints(fullSchema, maxTreeSize, rand);
                        }

                        Set<Type> allowedTypes = fullSchema.getAllowedTypes();

                        if (allowedTypes.isEmpty()) {
                            throw new GeneratorException("Impossible to generate a value for the schema " + fullSchema
                                    + " as the set of allowed types is empty");
                        }

                        Type type = new ArrayList<>(allowedTypes).get(rand.nextInt(allowedTypes.size()));
                        return generateValue(type, fullSchema, maxTreeSize, rand);
                    } catch (GeneratorException e) {
                        // The choice we made lead to an invalid schema. We retry with a different
                        // choice
                    }
                }
            }
        }
        throw new GeneratorException("Impossible to generate a document: all tries failed for the schema " + schema);
    }

    public Object generateValue(Type type, JSONSchema schema, int maxTreeSize, Random rand)
            throws JSONSchemaException, JSONException, GeneratorException {
        Handler handler;
        switch (type) {
            case BOOLEAN:
                handler = getBooleanHandler();
                break;
            case ENUM:
                handler = getEnumHandler();
                break;
            case INTEGER:
                handler = getIntegerHandler();
                break;
            case NUMBER:
                handler = getNumberHandler();
                break;
            case STRING:
                handler = getStringHandler();
                break;
            case ARRAY:
                handler = getArrayHandler();
                break;
            case OBJECT:
                handler = getObjectHandler();
                break;
            default:
                return null;
        }

        final JSONSchema allOf = schema.getAllOf();
        final List<JSONSchema> anyOfList = schema.getAnyOf();
        final List<JSONSchema> oneOfList = schema.getOneOf();
        final List<JSONSchema> notList = schema.getNot();

        final List<Integer> indicesAnyOf = generateIndicesRandomOrder(anyOfList, rand);
        final List<Integer> indicesOneOf = generateIndicesRandomOrder(oneOfList, rand);
        final List<Integer> indicesNot = generateIndicesRandomOrder(notList, rand);

        for (final int indexAnyOf : indicesAnyOf) {
            final JSONSchema anyOf = anyOfList.get(indexAnyOf);
            for (final int indexOneOf : indicesOneOf) {
                final JSONSchema oneOf = oneOfList.get(indexOneOf);
                for (final int indexNot : indicesNot) {
                    final JSONSchema not = notList.get(indexNot);
                    final JSONSchema fullSchema = getMergedSchema(schema, allOf, anyOf, oneOf, not);
                    try {
                        return handler.generate(this, fullSchema, maxTreeSize, rand);
                    } catch (GeneratorException e) {
                        // The choice we made lead to an invalid schema. We retry with a different
                        // choice
                    }
                }
            }
        }

        throw new GeneratorException("Impossible to generate a document: all tries failed for the schema " + schema);
    }

    private static List<Integer> generateIndicesRandomOrder(final List<?> list, Random rand) {
        return rand.ints(0, list.size()).distinct().limit(list.size()).boxed().collect(Collectors.toList());
    }
}
