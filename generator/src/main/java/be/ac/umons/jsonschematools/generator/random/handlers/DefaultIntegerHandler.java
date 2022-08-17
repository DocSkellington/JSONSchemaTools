package be.ac.umons.jsonschematools.generator.random.handlers;

import java.util.Random;

import org.json.JSONException;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.generator.random.GeneratorException;
import be.ac.umons.jsonschematools.generator.random.RandomGenerator;

/**
 * An integer handler that returns an abstracted integer, i.e., "\I".
 * 
 * This means that constraints on integers are ignored by this generator.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultIntegerHandler implements IHandler {

    @Override
    public Object generate(RandomGenerator generator, JSONSchema schema, int maxTreeSize, boolean canGenerateInvalid,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        return AbstractConstants.integerConstant;
    }

}
