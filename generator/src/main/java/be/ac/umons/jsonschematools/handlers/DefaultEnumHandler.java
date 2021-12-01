package be.ac.umons.jsonschematools.handlers;

import java.util.Random;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;

public class DefaultEnumHandler implements Handler {

    @Override
    public String generate(
            Generator generator, JSONSchema schema, int maxTreeSize, Random rand)
            throws JSONSchemaException {
        return AbstractConstants.enumConstant;
    }
    
}
