package be.ac.umons.jsonschematools.random;

import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultStringHandler;

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
