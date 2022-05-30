package be.ac.umons.jsonschematools.exploration.generatorhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONException;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
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
            int maxDocumentDepth, boolean generateInvalid, final ChoicesSequence choices)
            throws JSONSchemaException, JSONException {
        final List<Boolean> forbiddenValues = new ArrayList<>(schema.getForbiddenValuesFilteredByType(Boolean.class));

        if (forbiddenValues.contains(true) && forbiddenValues.contains(false)) {
            if (generateInvalid) {
                return generateBoolean(choices);
            }
            return Optional.empty();
        }

        final Boolean constValue = schema.getConstValueIfType(Boolean.class);
        if (constValue != null) {
            if (forbiddenValues.contains(constValue)) {
                if (generateInvalid) {
                    return Optional.of(constValue);
                }
                return Optional.empty();
            }

            if (generateInvalid) {
                return Optional.of(!constValue);
            }
            return Optional.of(constValue);
        }

        if (forbiddenValues.contains(true)) {
            if (generateInvalid) {
                return Optional.of(true);
            }
            return Optional.of(false);
        } else if (forbiddenValues.contains(false)) {
            if (generateInvalid) {
                return Optional.of(false);
            }
            return Optional.of(true);
        }

        if (generateInvalid) {
            return Optional.empty();
        }

        return generateBoolean(choices);
    }

    private Optional<Object> generateBoolean(final ChoicesSequence choices) {
        int index = choices.getIndexNextExclusiveSelectionInList(booleanValues.size());
        return Optional.of(booleanValues.get(index));
    }

}
