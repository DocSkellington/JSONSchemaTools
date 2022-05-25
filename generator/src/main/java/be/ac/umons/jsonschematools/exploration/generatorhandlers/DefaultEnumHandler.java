package be.ac.umons.jsonschematools.exploration.generatorhandlers;

import java.util.Optional;

import org.json.JSONException;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.exploration.ChoicesSequence;
import be.ac.umons.jsonschematools.exploration.ExplorationGenerator;

/**
 * Generates an abstracted enumeration value.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultEnumHandler extends AHandler {

    @Override
    public Optional<Object> generate(final JSONSchema schema, final ExplorationGenerator generator,
            final ChoicesSequence choices) throws JSONSchemaException, JSONException {
        return Optional.of(AbstractConstants.enumConstant);
    }

}
