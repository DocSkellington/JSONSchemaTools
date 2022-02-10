package be.ac.umons.jsonschematools.generatorhandlers;

import java.util.Random;

import org.json.JSONException;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;

/**
 * A number handler that returns an abstracted number, i.e., "\D".
 * 
 * This means that constraints on numbers are ignored by this generator.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultNumberHandler implements Handler {

    @Override
    public Object generate(Generator generator, JSONSchema schema, int maxTreeSize,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        return AbstractConstants.numberConstant;
    }

}
