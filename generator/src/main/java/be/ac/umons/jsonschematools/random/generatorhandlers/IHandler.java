package be.ac.umons.jsonschematools.random.generatorhandlers;

import java.util.Random;

import org.json.JSONException;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.random.GeneratorException;
import be.ac.umons.jsonschematools.random.RandomGenerator;

/**
 * A handler for the generator.
 * 
 * @author Gaëtan Staquet
 */
public interface IHandler {
    Object generate(final RandomGenerator generator, final JSONSchema schema, final int maxTreeSize, final Random rand)
            throws JSONSchemaException, GeneratorException, JSONException;
}
