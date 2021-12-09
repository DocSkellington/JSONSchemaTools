package be.ac.umons.jsonschematools.handlers;

import java.util.Random;
import java.util.Set;

import org.json.JSONException;

import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;

public class DefaultBooleanHandler implements Handler {

    @Override
    public Object generate(Generator generator, JSONSchema schema, int maxTreeSize,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        Set<Object> forbiddenValues = schema.getForbiddenValues();
        if (forbiddenValues.contains(true) && forbiddenValues.contains(false)) {
            throw new GeneratorException("Impossible to generate a boolean as both true and false are forbidden " + schema);
        }
        else if (forbiddenValues.contains(true)) {
            return false;
        }
        else if (forbiddenValues.contains(false)) {
            return true;
        }
        return rand.nextBoolean();
    }

}
