package be.ac.umons.jsonschematools;

import be.ac.umons.jsonschematools.generatorhandlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.generatorhandlers.DefaultStringHandler;

public class DefaultGenerator extends Generator {
    public DefaultGenerator(int maxPropertiesObject, int maxItemsArray) {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(), new DefaultEnumHandler(), new DefaultObjectHandler(maxPropertiesObject),
                new DefaultArrayHandler(maxItemsArray));
    }

    public DefaultGenerator() {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(), new DefaultEnumHandler(), new DefaultObjectHandler(),
                new DefaultArrayHandler());
    }
}
