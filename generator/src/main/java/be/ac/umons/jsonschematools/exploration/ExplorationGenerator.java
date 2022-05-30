package be.ac.umons.jsonschematools.exploration;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;
import be.ac.umons.jsonschematools.Type;
import be.ac.umons.jsonschematools.exploration.generatorhandlers.IHandler;

/**
 * A generator that produces documents by exhaustively exploring the
 * possibilities described in a JSON schema.
 * 
 * If the schema contains a recursive part, an infinite number of documents can
 * be generated.
 * 
 * @author Gaëtan Staquet
 */
public class ExplorationGenerator {

    private final IHandler stringHandler;
    private final IHandler integerHandler;
    private final IHandler numberHandler;
    private final IHandler booleanHandler;
    private final IHandler enumHandler;
    private final IHandler objectHandler;
    private final IHandler arrayHandler;

    public ExplorationGenerator(final IHandler stringHandler, final IHandler integerHandler,
            final IHandler numberHandler, final IHandler booleanHandler, final IHandler enumHandler,
            final IHandler objectHandler, final IHandler arrayHandler) {
        this.stringHandler = stringHandler;
        this.integerHandler = integerHandler;
        this.numberHandler = numberHandler;
        this.booleanHandler = booleanHandler;
        this.enumHandler = enumHandler;
        this.objectHandler = objectHandler;
        this.arrayHandler = arrayHandler;
    }

    /**
     * Creates an iterator over the documents this generator can produce.
     * 
     * Documents are created when calling {@code next()}.
     * 
     * The depth of the documents is not bounded, i.e., they can be an infinite
     * number of documents if the schema is recursive.
     * 
     * @param schema The schema
     * @return An iterator
     */
    public Iterator<JSONObject> createIterator(JSONSchema schema) {
        return createIterator(schema, -1);
    }

    /**
     * Creates an iterator over the documents this generator can produce up to the
     * given document depth.
     * 
     * Documents are created when calling {@code next()}.
     * 
     * The depth of the documents is bounded, i.e., in any document, there can only
     * be {@code maxDocumentDepth} nested objects and arrays.
     * Note that if the bound is set too low, only invalid documents may be
     * generated as the deepest objects or arrays may not be correct.
     * In particular, no documents will be generated with a depth of zero.
     * 
     * @param schema The schema
     * @return An iterator
     */
    public Iterator<JSONObject> createIterator(JSONSchema schema, int maxDocumentDepth) {
        return new ExplorationIterator(schema, maxDocumentDepth, this);
    }

    JSONObject generateDocument(JSONSchema schema, int maxDocumentDepth, ChoicesSequence choices)
            throws JSONException, JSONSchemaException {
        choices.prepareForNewExploration();
        Optional<Object> object = generateValueAccordingToConstraints(schema, maxDocumentDepth, choices);
        // If it was not possible to generate the object with the previous choices, we
        // start again if it is possible.
        while (object.isEmpty() && choices.containsChoiceWithNextValue()) {
            choices.prepareForNewExploration();
            object = generateValueAccordingToConstraints(schema, maxDocumentDepth, choices);
        }
        if (object.isEmpty()) {
            return null;
        }
        return (JSONObject) object.get();
    }

    private Optional<Object> generateValue(JSONSchema schema, Type type, int maxDocumentDepth, ChoicesSequence choices)
            throws JSONException, JSONSchemaException {
        IHandler handler;
        switch (type) {
            case ARRAY:
                handler = arrayHandler;
                break;
            case BOOLEAN:
                handler = booleanHandler;
                break;
            case ENUM:
                handler = enumHandler;
                break;
            case INTEGER:
                handler = integerHandler;
                break;
            case NULL:
                return Optional.of(JSONObject.NULL);
            case NUMBER:
                handler = numberHandler;
                break;
            case OBJECT:
                handler = objectHandler;
                break;
            case STRING:
                handler = stringHandler;
                break;
            default:
                return Optional.empty();
        }

        return handler.generate(schema, this, maxDocumentDepth, choices);
    }

    /**
     * Generates a value according to the constraints given in the schema and
     * following the sequence of choices.
     * 
     * @param schema           The schema
     * @param maxDocumentDepth The maximal depth of the document
     * @param choices          The sequence of choices
     * @return An optional containing the value, if it was possible to generate it.
     * @throws JSONException
     * @throws JSONSchemaException
     */
    public Optional<Object> generateValueAccordingToConstraints(JSONSchema schema, int maxDocumentDepth,
            ChoicesSequence choices)
            throws JSONException, JSONSchemaException {
        final JSONSchema allOf = schema.getAllOf();
        // @formatter:off
        final List<JSONSchema> anyOfList = schema.getAnyOf().stream()
            .filter(s -> !JSONSchemaStore.isFalseSchema(s))
            .collect(Collectors.toList())
        ;
        final List<JSONSchema> oneOfList = schema.getOneOf().stream()
            .filter(s -> !JSONSchemaStore.isFalseSchema(s))
            .collect(Collectors.toList())
        ;
        final List<JSONSchema> notList = schema.getNot().stream()
            .filter(s -> !JSONSchemaStore.isFalseSchema(s))
            .collect(Collectors.toList())
        ;
        // @formatter:on

        Optional<Object> value = null;
        Choice choiceAnyOf = getChoiceForSelectionOfSchema(anyOfList, choices, false);
        // If we have to change the value of choiceAnyOf, we also remove all the choices
        // that came after
        if (!choices.hasUnseenValueFurtherInExploration() && choiceAnyOf.hasNextValue()) {
            choices.removeAllChoicesAfterCurrentChoiceInExploration();
            choiceAnyOf.nextValue();
        }
        Choice choiceOneOf = getChoiceForSelectionOfSchema(oneOfList, choices, true);
        if (!choices.hasUnseenValueFurtherInExploration() && choiceOneOf.hasNextValue()) {
            choices.removeAllChoicesAfterCurrentChoiceInExploration();
            choiceOneOf.nextValue();
        }
        Choice choiceNot = getChoiceForSelectionOfSchema(notList, choices, false);
        if (!choices.hasUnseenValueFurtherInExploration() && choiceNot.hasNextValue()) {
            choices.removeAllChoicesAfterCurrentChoiceInExploration();
            choiceNot.nextValue();
        }

        do {
            final boolean samePossibilityAnyOf, samePossibilityOneOf, samePossibilityNot;
            if (value == null) {
                samePossibilityAnyOf = samePossibilityOneOf = samePossibilityNot = true;
            } else if (choiceNot.hasNextValue()) {
                samePossibilityAnyOf = samePossibilityOneOf = true;
                samePossibilityNot = false;
            } else if (choiceOneOf.hasNextValue()) {
                samePossibilityAnyOf = true;
                samePossibilityOneOf = false;
                samePossibilityNot = true;
            } else if (choiceAnyOf.hasNextValue()) {
                samePossibilityAnyOf = false;
                samePossibilityOneOf = samePossibilityNot = true;
            } else {
                return Optional.empty();
            }

            final JSONSchema anyOf = selectSchema(anyOfList, choices, choiceAnyOf, samePossibilityAnyOf);
            if (!samePossibilityAnyOf) {
                choiceOneOf = getChoiceForSelectionOfSchema(oneOfList, choices, true);
                choiceOneOf.nextValue();
            }
            final JSONSchema oneOf = selectSchema(oneOfList, choices, choiceOneOf, samePossibilityOneOf);
            if (!samePossibilityAnyOf || !samePossibilityOneOf) {
                choiceNot = getChoiceForSelectionOfSchema(notList, choices, false);
                choiceNot.nextValue();
            }
            final JSONSchema not = selectSchema(notList, choices, choiceNot, samePossibilityNot);

            // @formatter:off
            final JSONSchema mergedSchema = schema.dropAllOfAnyOfOneOfAndNot()
                .merge(allOf)
                .merge(anyOf)
                .merge(oneOf)
                .merge(not)
            ;
            // @formatter:on

            value = generateValueForMergedSchema(mergedSchema, maxDocumentDepth, choices);
        } while (value.isEmpty());
        return value;
    }

    private Optional<Object> generateValueForMergedSchema(final JSONSchema mergedSchema, int maxDocumentDepth,
            final ChoicesSequence choices)
            throws JSONException, JSONSchemaException {
        if (mergedSchema.needsFurtherUnfolding()) {
            return generateValueAccordingToConstraints(mergedSchema, maxDocumentDepth, choices);
        }

        List<Type> allowedTypes = mergedSchema.getAllowedTypes();
        if (allowedTypes.isEmpty()) {
            return Optional.empty();
        }

        final Type selectedType = selectType(allowedTypes, choices);
        if (selectedType == null) {
            return Optional.empty();
        }
        return generateValue(mergedSchema, selectedType, maxDocumentDepth, choices);
    }

    private Type selectType(List<Type> allowedTypes, ChoicesSequence choices) {
        if (choices.hasNextChoiceInExploration()) {
            Choice choiceInRun = choices.getNextChoiceInExploration();
            if (choices.hasUnseenValueFurtherInExploration()) {
                return allowedTypes.get(choiceInRun.currentValue());
            }
            choices.removeAllChoicesAfterCurrentChoiceInExploration();

            if (choiceInRun.hasNextValue()) {
                return allowedTypes.get(choiceInRun.nextValue());
            } else {
                choices.popLastChoice();
                return null;
            }
        } else {
            Choice choice = choices.createNewChoice(allowedTypes.size(), true);
            return allowedTypes.get(choice.nextValue());
        }
    }

    private Choice getChoiceForSelectionOfSchema(List<JSONSchema> schemas, ChoicesSequence choices,
            boolean exclusiveChoice) {
        if (choices.hasNextChoiceInExploration()) {
            return choices.getNextChoiceInExploration();
        } else {
            Choice choice = choices.createNewChoice(schemas.size(), exclusiveChoice);
            if (!exclusiveChoice) {
                // If the choice is for an anyOf (or a not), we explicitly ignore the first
                // value in the choice, which is zero. That is, we always take at least one
                // schema.
                choice.nextValue();
            }
            return choice;
        }
    }

    private JSONSchema selectSchema(List<JSONSchema> schemas, ChoicesSequence choices, Choice choice,
            boolean reuseSamePossibility) throws JSONSchemaException {
        final boolean exclusiveChoice = choice.isExclusive();
        if (reuseSamePossibility) {
            if (exclusiveChoice) {
                return schemas.get(choice.currentValue());
            } else {
                assert choice.currentValue() != 0;
                return getMergedSchemaForSelectedSchemasForNonExclusive(schemas, choice);
            }
        }
        // We delete all the choices that came *after* this choice.
        // This cancels all the choices that were made. That way, the next exploration
        // will create them again and all their values will be correctly explored
        choices.removeAllChoicesComingAfter(choice);

        if (choice.hasNextValue()) {
            if (exclusiveChoice) {
                return schemas.get(choice.nextValue());
            } else {
                choice.nextValue();
                assert choice.currentValue() != 0;
                return getMergedSchemaForSelectedSchemasForNonExclusive(schemas, choice);
            }
        } else {
            choices.popLastChoice();
            return null;
        }
    }

    private JSONSchema getMergedSchemaForSelectedSchemasForNonExclusive(List<JSONSchema> schemas, Choice choice)
            throws JSONSchemaException {
        BitSet bitset = choice.getBitSet();
        int bit = bitset.nextSetBit(0);
        JSONSchema merged = schemas.get(bit);

        for (bit = bitset.nextSetBit(0); bit != -1; bit = bitset.nextSetBit(bit + 1)) {
            merged = merged.merge(schemas.get(bit));
        }

        return merged;
    }
}
