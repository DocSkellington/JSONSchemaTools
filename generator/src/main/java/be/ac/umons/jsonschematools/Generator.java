package be.ac.umons.jsonschematools;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.handlers.Handler;

public class Generator {

    private final Handler stringHandler;
    private final Handler integerHandler;
    private final Handler numberHandler;
    private final Handler booleanHandler;
    private final Handler enumHandler;
    private final Handler objectHandler;
    private final Handler arrayHandler;

    public Generator(final Handler stringHandler, final Handler integerHandler, final Handler numberHandler,
            final Handler booleanHandler, final Handler enumHandler, final Handler objectHandler,
            final Handler arrayHandler) {
        this.stringHandler = stringHandler;
        this.integerHandler = integerHandler;
        this.numberHandler = numberHandler;
        this.booleanHandler = booleanHandler;
        this.enumHandler = enumHandler;
        this.objectHandler = objectHandler;
        this.arrayHandler = arrayHandler;
    }

    public JSONObject generate(final JSONSchema schema, final int maxTreeSize)
            throws JSONSchemaException, JSONException, GeneratorException {
        return generate(schema, maxTreeSize, new Random());
    }

    public JSONObject generate(final JSONSchema schema, final int maxTreeSize, final Random rand)
            throws JSONSchemaException, JSONException, GeneratorException {
        return (JSONObject) objectHandler.generate(this, schema, maxTreeSize, rand);
    }

    public Handler getStringHandler() {
        return stringHandler;
    }

    public Handler getIntegerHandler() {
        return integerHandler;
    }

    public Handler getNumberHandler() {
        return numberHandler;
    }

    public Handler getObjectHandler() {
        return objectHandler;
    }

    public Handler getArrayHandler() {
        return arrayHandler;
    }

    public Handler getBooleanHandler() {
        return booleanHandler;
    }

    public Handler getEnumHandler() {
        return enumHandler;
    }
}
