package be.ac.umons.jsonschematools.generator.exploration;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
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
import be.ac.umons.jsonschematools.generator.IGenerator;
import be.ac.umons.jsonschematools.generator.exploration.handlers.IHandler;

/**
 * A generator that produces documents by exhaustively exploring the
 * possibilities described in a JSON schema.
 * 
 * If the schema contains a recursive part, an infinite number of documents can
 * be generated.
 * 
 * @author Gaëtan Staquet
 */
public class ExplorationGenerator implements IGenerator {

    public static final Optional<Object> EMPTY_VALUE_DUE_TO_MAX_DEPTH = Optional.of(Type.NULL);

    private static final List<Type> allTypes = new ArrayList<>(EnumSet.allOf(Type.class));

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

    @Override
    public Iterator<JSONObject> createIterator(JSONSchema schema, int maxDocumentDepth, boolean canGenerateInvalid) {
        return new ExplorationIterator(schema, maxDocumentDepth, canGenerateInvalid, this);
    }

    public JSONObject generateDocument(JSONSchema schema, int maxDocumentDepth, boolean canGenerateInvalid,
            ChoicesSequence choices)
            throws JSONException, JSONSchemaException {
        choices.prepareForNewExploration();
        Optional<Object> object = generateValueAccordingToConstraints(schema, maxDocumentDepth, canGenerateInvalid,
                choices, true);
        // If it was not possible to generate the object with the previous choices, we
        // start again if it is possible.
        while (object.isEmpty() && choices.containsChoiceWithNextValue()) {
            choices.prepareForNewExploration();
            object = generateValueAccordingToConstraints(schema, maxDocumentDepth, canGenerateInvalid, choices, true);
        }
        if (object.isEmpty() || object == ExplorationGenerator.EMPTY_VALUE_DUE_TO_MAX_DEPTH) {
            return null;
        }
        return (JSONObject) object.get();
    }

    private Optional<Object> generateValue(JSONSchema schema, Type type, int maxDocumentDepth,
            boolean canGenerateInvalid, ChoicesSequence choices)
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

        if ((type == Type.OBJECT || type == Type.ARRAY) && maxDocumentDepth == 0) {
            return EMPTY_VALUE_DUE_TO_MAX_DEPTH;
        }
        return handler.generate(schema, this, maxDocumentDepth, canGenerateInvalid, choices);
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
            boolean canGenerateInvalid, ChoicesSequence choices) throws JSONException, JSONSchemaException {
        return generateValueAccordingToConstraints(schema, maxDocumentDepth, canGenerateInvalid, choices, false);
    }

    public Optional<Object> generateValueAccordingToConstraints(JSONSchema schema, int maxDocumentDepth,
            boolean canGenerateInvalid, ChoicesSequence choices, boolean mustBeObject)
            throws JSONException, JSONSchemaException {
        final JSONSchema allOf = schema.getAllOf();
        final List<JSONSchema> anyOfList, oneOfList, notList;
        final boolean exclusiveChoiceAnyOf, exclusiveChoiceOneOf, exclusiveChoiceNot;
        final boolean skipFirstAnyOf, skipFirstOneOf, skipFirstNot;
        final Boolean generateInvalid = invalidGenerationChoice(canGenerateInvalid, choices);
        if (generateInvalid == null) {
            return Optional.empty();
        }
        else if (generateInvalid) {
            anyOfList = schema.getAnyOf();
            oneOfList = schema.getOneOf();
            notList = schema.getNot();

            // We want to reduce the number of useless generations. So, if the list only
            // contains the true schema, we revert to the classical behavior even if we want
            // to generate an invalid document.
            if (anyOfList.size() == 1 && JSONSchemaStore.isTrueSchema(anyOfList.get(0))) {
                exclusiveChoiceAnyOf = false;
                skipFirstAnyOf = true;
            } else {
                exclusiveChoiceAnyOf = false;
                skipFirstAnyOf = false;
            }

            if (oneOfList.size() == 1 && JSONSchemaStore.isTrueSchema(oneOfList.get(0))) {
                exclusiveChoiceOneOf = false;
                skipFirstOneOf = true;
            } else {
                exclusiveChoiceOneOf = false;
                skipFirstOneOf = false;
            }

            if (notList.size() == 1 && JSONSchemaStore.isTrueSchema(notList.get(0))) {
                exclusiveChoiceNot = false;
                skipFirstNot = true;
            } else {
                exclusiveChoiceNot = false;
                skipFirstNot = false;
            }
        } else {
            // @formatter:off
            anyOfList = schema.getAnyOf().stream()
                .filter(s -> !JSONSchemaStore.isFalseSchema(s))
                .collect(Collectors.toList())
            ;
            oneOfList = schema.getOneOf().stream()
                .filter(s -> !JSONSchemaStore.isFalseSchema(s))
                .collect(Collectors.toList())
            ;
            notList = schema.getNot().stream()
                .filter(s -> !JSONSchemaStore.isFalseSchema(s))
                .collect(Collectors.toList())
            ;
            // @formatter:on

            exclusiveChoiceAnyOf = false;
            exclusiveChoiceOneOf = true;
            exclusiveChoiceNot = false;
            skipFirstAnyOf = true;
            skipFirstOneOf = false;
            skipFirstNot = true;
        }

        Optional<Object> value = null;
        Choice choiceAnyOf = choices.getChoiceForSelectionInList(anyOfList.size(), exclusiveChoiceAnyOf,
                skipFirstAnyOf);
        // If we have to change the value of choiceAnyOf, we also remove all the choices
        // that came after
        if (!choices.hasUnseenValueFurtherInExploration() && choiceAnyOf.hasNextValue()) {
            choices.removeAllChoicesAfterCurrentChoiceInExploration();
            choiceAnyOf.nextValue();
        }
        Choice choiceOneOf = choices.getChoiceForSelectionInList(oneOfList.size(), exclusiveChoiceOneOf,
                skipFirstOneOf);
        if (!choices.hasUnseenValueFurtherInExploration() && choiceOneOf.hasNextValue()) {
            choices.removeAllChoicesAfterCurrentChoiceInExploration();
            choiceOneOf.nextValue();
        }
        Choice choiceNot = choices.getChoiceForSelectionInList(notList.size(), exclusiveChoiceNot, skipFirstNot);
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
                choiceOneOf = choices.getChoiceForSelectionInList(oneOfList.size(), exclusiveChoiceOneOf,
                        skipFirstOneOf);
                choiceOneOf.nextValue();
            }
            final JSONSchema oneOf = selectSchema(oneOfList, choices, choiceOneOf, samePossibilityOneOf);
            if (!samePossibilityAnyOf || !samePossibilityOneOf) {
                choiceNot = choices.getChoiceForSelectionInList(notList.size(), exclusiveChoiceNot, skipFirstNot);
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

            value = generateValueForMergedSchema(mergedSchema, maxDocumentDepth, generateInvalid, choices,
                    mustBeObject);
        } while (value.isEmpty());
        return value;
    }

    private Optional<Object> generateValueForMergedSchema(final JSONSchema mergedSchema, int maxDocumentDepth,
            boolean generateInvalid, final ChoicesSequence choices, boolean mustBeObject)
            throws JSONException, JSONSchemaException {
        if (mergedSchema.needsFurtherUnfolding()) {
            return generateValueAccordingToConstraints(mergedSchema, maxDocumentDepth, generateInvalid, choices);
        }

        List<Type> allowedTypes = mergedSchema.getAllowedTypes();
        if (allowedTypes.isEmpty()) {
            return Optional.empty();
        }

        final Type selectedType = selectType(allowedTypes, generateInvalid, choices, mustBeObject);
        if (selectedType == null) {
            return Optional.empty();
        }
        return generateValue(mergedSchema, selectedType, maxDocumentDepth, generateInvalid, choices);
    }

    private Type selectType(List<Type> allowedTypes, boolean generateInvalid, ChoicesSequence choices,
            boolean mustBeObject) {
        if (mustBeObject) {
            if (allowedTypes.contains(Type.OBJECT)) {
                return Type.OBJECT;
            }
            else {
                return null;
            }
        }
        if (!generateInvalid) {
            final Integer index = choices.getIndexNextExclusiveSelectionInList(allowedTypes.size());
            if (index == null) {
                return null;
            }
            return allowedTypes.get(index);
        }
        else {
            Boolean booleanValue = choices.getNextBooleanValue();
            if (booleanValue) {
                final Integer index = choices.getIndexNextExclusiveSelectionInList(allowedTypes.size());
                if (index == null) {
                    return null;
                }
                return allowedTypes.get(index);
            }
            else {
                final List<Type> typesNotAllowed = new ArrayList<>(allTypes);
                typesNotAllowed.removeAll(allowedTypes);
                if (typesNotAllowed.isEmpty()) {
                    return null;
                }
                final Integer index = choices.getIndexNextExclusiveSelectionInList(typesNotAllowed.size());
                if (index == null) {
                    return null;
                }
                return typesNotAllowed.get(index);
            }
        }
    }

    private JSONSchema selectSchema(List<JSONSchema> schemas, ChoicesSequence choices, Choice choice,
            boolean reuseSamePossibility) throws JSONSchemaException {
        final boolean exclusiveChoice = choice.isExclusive();
        if (reuseSamePossibility) {
            if (exclusiveChoice) {
                return schemas.get(choice.currentValue());
            } else {
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
        if (bit == -1) {
            return schemas.get(0).getStore().trueSchema();
        }
        JSONSchema merged = schemas.get(bit);

        for (bit = bitset.nextSetBit(0); bit != -1; bit = bitset.nextSetBit(bit + 1)) {
            merged = merged.merge(schemas.get(bit));
        }

        return merged;
    }

    private Boolean invalidGenerationChoice(final boolean canGenerateInvalid, final ChoicesSequence choices) {
        if (!canGenerateInvalid) {
            return false;
        }

        return choices.getNextBooleanValue();
    }
}
