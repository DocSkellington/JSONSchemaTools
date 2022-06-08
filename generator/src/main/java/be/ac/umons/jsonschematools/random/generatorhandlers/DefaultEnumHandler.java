package be.ac.umons.jsonschematools.random.generatorhandlers;

import java.util.Random;

import org.json.JSONException;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.random.GeneratorException;
import be.ac.umons.jsonschematools.random.RandomGenerator;

/**
 * An enumeration handler that returns an abstracted enum value, i.e., "\E".
 * 
 * This means that constraints on enumerations are ignored by this generator.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultEnumHandler implements IHandler {

    @Override
    public Object generate(RandomGenerator generator, JSONSchema schema, int maxTreeSize, boolean canGenerateInvalid,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        return AbstractConstants.enumConstant;
    }

}
