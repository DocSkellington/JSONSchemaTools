package be.ac.umons.jsonschematools;

import be.ac.umons.jsonschematools.handlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.handlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.handlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.handlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.handlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.handlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.handlers.DefaultStringHandler;

public class DefaultGenerator extends Generator {
    public DefaultGenerator(int maxItemsArray) {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(), new DefaultEnumHandler(), new DefaultObjectHandler(),
                new DefaultArrayHandler(maxItemsArray));
    }

    public DefaultGenerator() {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(), new DefaultEnumHandler(), new DefaultObjectHandler(),
                new DefaultArrayHandler());
    }
}
