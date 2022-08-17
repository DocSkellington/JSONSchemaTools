package be.ac.umons.jsonschematools.generator.random;

import be.ac.umons.jsonschematools.generator.random.handlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.generator.random.handlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.generator.random.handlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.generator.random.handlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.generator.random.handlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.generator.random.handlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.generator.random.handlers.DefaultStringHandler;

/**
 * Constructs a {@link RandomGenerator} with the default handlers to produce
 * values in a JSON document.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultRandomGenerator extends RandomGenerator {
    public DefaultRandomGenerator(int maxPropertiesObject, int maxItemsArray) {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(), new DefaultEnumHandler(),
                new DefaultObjectHandler(maxPropertiesObject),
                new DefaultArrayHandler(maxItemsArray));
    }

    public DefaultRandomGenerator() {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(), new DefaultEnumHandler(), new DefaultObjectHandler(),
                new DefaultArrayHandler());
    }
}
