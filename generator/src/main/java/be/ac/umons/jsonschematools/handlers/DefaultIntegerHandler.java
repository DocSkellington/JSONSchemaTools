package be.ac.umons.jsonschematools.handlers;

import java.util.Random;

import org.json.JSONException;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.Constraints;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;

public class DefaultIntegerHandler implements Handler {

    @Override
    public Object generate(Generator generator, Constraints constraints, JSONSchema schema, int maxTreeSize,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        return AbstractConstants.integerConstant;
    }

}