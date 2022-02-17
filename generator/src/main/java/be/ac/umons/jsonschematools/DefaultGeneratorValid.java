package be.ac.umons.jsonschematools;

import be.ac.umons.jsonschematools.generatorhandlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultStringHandler;

/**
 * Constructs a {@link Generator} with the default handlers that can not produce
 * invalid documents.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultGeneratorValid extends Generator {
    public DefaultGeneratorValid(int maxPropertiesObject, int maxItemsArray) {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(false), new DefaultEnumHandler(),
                new DefaultObjectHandler(false, maxPropertiesObject),
                new DefaultArrayHandler(false, maxItemsArray), false);
    }

    public DefaultGeneratorValid() {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(false), new DefaultEnumHandler(), new DefaultObjectHandler(false),
                new DefaultArrayHandler(false), false);
    }
}
