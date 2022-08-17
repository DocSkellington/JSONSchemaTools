package be.ac.umons.jsonschematools.generator.random.handlers;

import java.util.Random;

import org.json.JSONException;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.generator.random.GeneratorException;
import be.ac.umons.jsonschematools.generator.random.RandomGenerator;

/**
 * A handler for the generator.
 * 
 * @author Gaëtan Staquet
 */
public interface IHandler {
    Object generate(final RandomGenerator generator, final JSONSchema schema, final int maxTreeSize,
            final boolean canGenerateInvalid, final Random rand)
            throws JSONSchemaException, GeneratorException, JSONException;

    default boolean generateInvalid(final boolean canGenerateInvalid, final Random rand) {
        return canGenerateInvalid && rand.nextBoolean();
    }
}
