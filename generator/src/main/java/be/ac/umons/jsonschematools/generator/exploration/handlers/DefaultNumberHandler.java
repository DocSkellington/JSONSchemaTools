package be.ac.umons.jsonschematools.generator.exploration.handlers;

import java.util.Optional;

import org.json.JSONException;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.generator.exploration.ChoicesSequence;
import be.ac.umons.jsonschematools.generator.exploration.ExplorationGenerator;

/**
 * Generates an abstracted number value.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultNumberHandler extends AHandler {

    @Override
    public Optional<Object> generate(final JSONSchema schema, final ExplorationGenerator generator,
            int maxDocumentDepth, boolean canGenerateInvalid, final ChoicesSequence choices)
            throws JSONSchemaException, JSONException {
        return Optional.of(AbstractConstants.numberConstant);
    }

}