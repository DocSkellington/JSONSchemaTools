package be.ac.umons.jsonschematools.exploration.generatorhandlers;

import java.util.Optional;

import org.json.JSONException;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.exploration.ChoicesSequence;
import be.ac.umons.jsonschematools.exploration.ExplorationGenerator;

/**
 * A handler for the exploration generator.
 * 
 * @author Gaëtan Staquet
 */
public interface IHandler {
    /**
     * Generates the value according to the schema.
     * 
     * @param schema    The JSON schema
     * @param generator The generator
     * @param choices   The sequence of choices
     * @return The generated value
     * @throws JSONSchemaException
     * @throws JSONException
     */
    Optional<Object> generate(final JSONSchema schema, final ExplorationGenerator generator,
            final ChoicesSequence choices) throws JSONSchemaException, JSONException;
}
