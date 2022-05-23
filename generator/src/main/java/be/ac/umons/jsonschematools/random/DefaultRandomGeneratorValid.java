package be.ac.umons.jsonschematools.random;

import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.random.generatorhandlers.DefaultStringHandler;

/**
 * Constructs a {@link RandomGenerator} with the default handlers that can not produce
 * invalid documents.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultRandomGeneratorValid extends RandomGenerator {
    public DefaultRandomGeneratorValid(int maxPropertiesObject, int maxItemsArray) {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(false), new DefaultEnumHandler(),
                new DefaultObjectHandler(false, maxPropertiesObject),
                new DefaultArrayHandler(false, maxItemsArray), false);
    }

    public DefaultRandomGeneratorValid() {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(false), new DefaultEnumHandler(), new DefaultObjectHandler(false),
                new DefaultArrayHandler(false), false);
    }
}
