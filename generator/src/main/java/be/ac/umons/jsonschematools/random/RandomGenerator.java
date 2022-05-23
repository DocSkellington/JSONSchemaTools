package be.ac.umons.jsonschematools.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;
import be.ac.umons.jsonschematools.Type;
import be.ac.umons.jsonschematools.random.generatorhandlers.IHandler;

/**
 * A generator for JSON documents, given a JSON schema.
 * 
 * This implementation relies on external classes to handle each type allowed in
 * a JSON schema. See the {@link be.ac.umons.jsonschematools.generatorhandlers
 * handlers package} for implemented handlers.
 * 
 * @author Gaëtan Staquet
 */
public class RandomGenerator {

    private final IHandler stringHandler;
    private final IHandler integerHandler;
    private final IHandler numberHandler;
    private final IHandler booleanHandler;
    private final IHandler enumHandler;
    private final IHandler objectHandler;
    private final IHandler arrayHandler;
    private final boolean generateInvalid;
    private final Set<Type> allTypes = EnumSet.allOf(Type.class);

    public RandomGenerator(final IHandler stringHandler, final IHandler integerHandler, final IHandler numberHandler,
            final IHandler booleanHandler, final IHandler enumHandler, final IHandler objectHandler,
            final IHandler arrayHandler, final boolean generateInvalid) {
        this.stringHandler = stringHandler;
        this.integerHandler = integerHandler;
        this.numberHandler = numberHandler;
        this.booleanHandler = booleanHandler;
        this.enumHandler = enumHandler;
        this.objectHandler = objectHandler;
        this.arrayHandler = arrayHandler;
        this.generateInvalid = generateInvalid;
    }

    private static JSONSchema getMergedSchema(final JSONSchema baseSchema, final JSONSchema allOf,
            final JSONSchema anyOf, final JSONSchema oneOf, final JSONSchema not)
            throws JSONSchemaException, GeneratorException {
        if (JSONSchemaStore.isFalseSchema(baseSchema) || JSONSchemaStore.isFalseSchema(allOf)
                || JSONSchemaStore.isFalseSchema(anyOf) || JSONSchemaStore.isFalseSchema(oneOf)
                || JSONSchemaStore.isFalseSchema(not)) {
            throw new GeneratorException(
                    "Impossible to generate a document as one of the sub schemas is the false schema");
        }
        return baseSchema.dropAllOfAnyOfOneOfAndNot().merge(allOf).merge(anyOf).merge(oneOf).merge(not);
    }

    public JSONObject generate(final JSONSchema schema, final int maxTreeSize)
            throws JSONSchemaException, JSONException, GeneratorException {
        return generateRoot(schema, maxTreeSize, new Random());
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
                    try {
                        final JSONSchema fullSchema = getMergedSchema(schema, allOf, anyOf, oneOf, not);
                        List<Type> types = fullSchema.getAllowedTypes();
                        if (!types.contains(Type.OBJECT)) {
                            continue;
                        }
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

    public IHandler getStringHandler() {
        return stringHandler;
    }

    public IHandler getIntegerHandler() {
        return integerHandler;
    }

    public IHandler getNumberHandler() {
        return numberHandler;
    }

    public IHandler getObjectHandler() {
        return objectHandler;
    }

    public IHandler getArrayHandler() {
        return arrayHandler;
    }

    public IHandler getBooleanHandler() {
        return booleanHandler;
    }

    public IHandler getEnumHandler() {
        return enumHandler;
    }

    /**
     * Generates a value according to the constraints given by <code>schema</code>.
     * 
     * @param schema      The JSON schema
     * @param maxTreeSize The maximal tree depth that can be generated
     * @param rand        The random generator
     * @return A JSON document that satisfies the schema.
     * @throws JSONException
     * @throws JSONSchemaException
     * @throws GeneratorException
     */
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
                    try {
                        final JSONSchema fullSchema = getMergedSchema(schema, allOf, anyOf, oneOf, not);
                        // If we still have some constraints behind "allOf", "anyOf", "oneOf", or "not",
                        // we unfold them
                        if (fullSchema.needsFurtherUnfolding()) {
                            return generateAccordingToConstraints(fullSchema, maxTreeSize, rand);
                        }

                        List<Type> allowedTypes = fullSchema.getAllowedTypes();

                        if (allowedTypes.isEmpty()) {
                            throw new GeneratorException("Impossible to generate a value for the schema " + fullSchema
                                    + " as the set of allowed types is empty");
                        }

                        Type type = selectType(allowedTypes, rand);
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

    private Type selectType(final Collection<Type> allowedTypes, final Random rand) {
        final Collection<Type> typesToConsider;
        if (generateInvalid && allTypes.size() != Type.values().length && rand.nextBoolean()) {
            typesToConsider = new HashSet<>(allTypes);
            typesToConsider.removeAll(allowedTypes);
        }
        else {
            typesToConsider = allowedTypes;
        }

        return new ArrayList<>(typesToConsider).get(rand.nextInt(typesToConsider.size()));
    }

    private Object generateValue(Type type, JSONSchema schema, int maxTreeSize, Random rand)
            throws JSONSchemaException, JSONException, GeneratorException {
        IHandler handler;
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
                return JSONObject.NULL;
        }

        if ((type == Type.OBJECT || type == Type.ARRAY) && maxTreeSize == 0) {
            return Type.NULL;
        }
        return handler.generate(this, schema, maxTreeSize, rand);
    }

    private static List<Integer> generateIndicesRandomOrder(final List<?> list, Random rand) {
        // @formatter:off
        List<Integer> values = rand.ints(0, list.size())
            .distinct()
            .limit(list.size())
            .boxed()
            .collect(Collectors.toList());
        // @formatter:on
        return values;
    }
}
