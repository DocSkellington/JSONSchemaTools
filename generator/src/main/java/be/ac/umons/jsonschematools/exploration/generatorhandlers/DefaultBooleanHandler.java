package be.ac.umons.jsonschematools.exploration.generatorhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONException;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.exploration.Choice;
import be.ac.umons.jsonschematools.exploration.ChoicesSequence;
import be.ac.umons.jsonschematools.exploration.ExplorationGenerator;

/**
 * Generates a boolean by exploring all the possibilities in the schema.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultBooleanHandler extends AHandler {

    private static final List<Boolean> booleanValues = List.of(true, false);

    @Override
    public Optional<Object> generate(final JSONSchema schema, final ExplorationGenerator generator,
            int maxDocumentDepth, final ChoicesSequence choices) throws JSONSchemaException, JSONException {
        final List<Boolean> forbiddenValues = new ArrayList<>(schema.getForbiddenValuesFilteredByType(Boolean.class));

        if (forbiddenValues.contains(true) && forbiddenValues.contains(false)) {
            return Optional.empty();
        }

        final Object constValue = schema.getConstValue();
        if (constValue != null) {
            if (forbiddenValues.contains(constValue)) {
                return Optional.empty();
            }
            return Optional.of(constValue);
        }

        if (forbiddenValues.contains(true)) {
            return Optional.of(false);
        } else if (forbiddenValues.contains(false)) {
            return Optional.of(true);
        }

        if (choices.hasNextChoiceInExploration()) {
            Choice lastChoice = choices.getNextChoiceInExploration();
            if (choices.hasUnseenValueFurtherInExploration()) {
                return Optional.of(booleanValues.get(lastChoice.currentValue()));
            }
            choices.removeAllChoicesAfterCurrentChoiceInExploration();

            if (lastChoice.hasNextValue()) {
                return Optional.of(booleanValues.get(lastChoice.nextValue()));
            } else {
                choices.popLastChoice();
                return Optional.empty();
            }
        } else {
            // It is the first time we see this choice in this run
            Choice choice = choices.createNewChoice(2, true);
            return Optional.of(booleanValues.get(choice.nextValue()));
        }
    }

}
