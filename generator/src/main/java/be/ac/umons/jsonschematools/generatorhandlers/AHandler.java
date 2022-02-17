package be.ac.umons.jsonschematools.generatorhandlers;

import java.util.Random;

/**
 * Abstract handler.
 * 
 * It stores whether the handler is allowed to generate invalid documents.
 * 
 * @author Gaëtan Staquet
 */
public abstract class AHandler implements Handler {
    private final boolean generateInvalid;

    protected AHandler(final boolean generateInvalid) {
        this.generateInvalid = generateInvalid;
    }

    protected boolean generateInvalid(final Random rand) {
        return generateInvalid && rand.nextBoolean();
    }
}
