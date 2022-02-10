package be.ac.umons.jsonschematools.generatorhandlers;

import java.util.Random;

import org.json.JSONException;

import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;

/**
 * A handler for the generator.
 * 
 * @author Gaëtan Staquet
 */
public interface Handler {
    Object generate(final Generator generator, final JSONSchema schema, final int maxTreeSize, final Random rand)
            throws JSONSchemaException, GeneratorException, JSONException;
}
