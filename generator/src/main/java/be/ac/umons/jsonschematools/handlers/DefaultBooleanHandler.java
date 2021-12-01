package be.ac.umons.jsonschematools.handlers;

import java.util.Random;

import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.JSONSchema;

public class DefaultBooleanHandler implements Handler {

    @Override
    public Boolean generate(
            Generator generator, JSONSchema schema, int maxTreeSize, Random rand) {
        return rand.nextBoolean();
    }

}
