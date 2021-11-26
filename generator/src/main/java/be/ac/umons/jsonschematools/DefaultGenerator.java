package be.ac.umons.jsonschematools;

import org.json.JSONArray;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.handlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.handlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.handlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.handlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.handlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.handlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.handlers.DefaultStringHandler;

public class DefaultGenerator extends Generator<String, String, String, Boolean, String, JSONObject, JSONArray> {
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
