package be.ac.umons.jsonschematools;

import be.ac.umons.jsonschematools.generatorhandlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultStringHandler;

/**
 * Constructs a {@link Generator} with the default handlers that can produce
 * invalid documents.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultGeneratorInvalid extends Generator {
    public DefaultGeneratorInvalid(int maxPropertiesObject, int maxItemsArray) {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(true), new DefaultEnumHandler(),
                new DefaultObjectHandler(true, maxPropertiesObject),
                new DefaultArrayHandler(true, maxItemsArray), true);
    }

    public DefaultGeneratorInvalid() {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(true), new DefaultEnumHandler(), new DefaultObjectHandler(true),
                new DefaultArrayHandler(true), true);
    }
}
