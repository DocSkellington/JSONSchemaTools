package be.ac.umons.jsonschematools.random;

import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultStringHandler;

/**
 * Constructs a {@link RandomGenerator} with the default handlers that can produce
 * invalid documents.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultRandomGeneratorInvalid extends RandomGenerator {
    public DefaultRandomGeneratorInvalid(int maxPropertiesObject, int maxItemsArray) {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(true), new DefaultEnumHandler(),
                new DefaultObjectHandler(true, maxPropertiesObject),
                new DefaultArrayHandler(true, maxItemsArray), true);
    }

    public DefaultRandomGeneratorInvalid() {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(true), new DefaultEnumHandler(), new DefaultObjectHandler(true),
                new DefaultArrayHandler(true), true);
    }
}
