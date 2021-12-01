package be.ac.umons.jsonschematools.handlers;

import java.util.Random;

import org.json.JSONException;

import be.ac.umons.jsonschematools.Constraints;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;

public interface Handler {
    default Object generate(final Generator generator, final JSONSchema schema, final int maxTreeSize, final Random rand)
            throws JSONSchemaException, GeneratorException, JSONException {
        return generate(generator, new Constraints(), schema, maxTreeSize, rand);
    }

    Object generate(final Generator generator, final Constraints constraints, final JSONSchema schema,
            final int maxTreeSize, final Random rand) throws JSONSchemaException, GeneratorException, JSONException;
}
