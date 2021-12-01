package be.ac.umons.jsonschematools.handlers;

import java.util.Random;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.JSONSchema;

public class DefaultNumberHandler implements Handler {

    @Override
    public String generate(
            final Generator generator, final JSONSchema schema, final int maxTreeSize,
            final Random rand) {
        return AbstractConstants.numberConstant;
    }

}
