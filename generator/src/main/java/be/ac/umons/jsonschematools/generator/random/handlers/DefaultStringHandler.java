package be.ac.umons.jsonschematools.generator.random.handlers;

import java.util.Random;

import org.json.JSONException;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.generator.random.GeneratorException;
import be.ac.umons.jsonschematools.generator.random.RandomGenerator;

/**
 * A string handler that returns an abstracted string, i.e., "\S".
 * 
 * This means that constraints on strings are ignored by this generator.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultStringHandler implements IHandler {

    @Override
    public Object generate(RandomGenerator generator, JSONSchema schema, int maxTreeSize, boolean canGenerateInvalid,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        return AbstractConstants.stringConstant;
    }

}