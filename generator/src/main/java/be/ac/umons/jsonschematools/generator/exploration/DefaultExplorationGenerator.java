package be.ac.umons.jsonschematools.generator.exploration;

import be.ac.umons.jsonschematools.generator.exploration.handlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.generator.exploration.handlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.generator.exploration.handlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.generator.exploration.handlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.generator.exploration.handlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.generator.exploration.handlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.generator.exploration.handlers.DefaultStringHandler;

public class DefaultExplorationGenerator extends ExplorationGenerator {
    public DefaultExplorationGenerator(int maxPropertiesObject, int maxItemsArray) {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(), new DefaultEnumHandler(),
                new DefaultObjectHandler(maxPropertiesObject),
                new DefaultArrayHandler(maxItemsArray));
    }

    public DefaultExplorationGenerator() {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(),
                new DefaultBooleanHandler(), new DefaultEnumHandler(), new DefaultObjectHandler(),
                new DefaultArrayHandler());
    }
}
