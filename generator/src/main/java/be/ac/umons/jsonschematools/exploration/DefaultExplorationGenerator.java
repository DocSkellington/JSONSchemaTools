package be.ac.umons.jsonschematools.exploration;

import be.ac.umons.jsonschematools.exploration.generatorhandlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.exploration.generatorhandlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.exploration.generatorhandlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.exploration.generatorhandlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.exploration.generatorhandlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.exploration.generatorhandlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.exploration.generatorhandlers.DefaultStringHandler;

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
