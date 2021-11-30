package be.ac.umons.jsonschematools;

import org.json.JSONObject;

import be.ac.umons.jsonschematools.handlers.Handler;

public class Validator {

    private final Handler stringHandler;
    private final Handler integerHandler;
    private final Handler numberHandler;
    private final Handler booleanHandler;
    private final Handler enumHandler;
    private final Handler objectHandler;
    private final Handler arrayHandler;

    public Validator(final Handler stringHandler, final Handler integerHandler, final Handler numberHandler,
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

    public boolean validate(final JSONSchema schema, final JSONObject document) throws JSONSchemaException {
        return this.objectHandler.validate(this, schema, document);
    }

    public Handler getArrayHandler() {
        return arrayHandler;
    }

    public Handler getStringHandler() {
        return stringHandler;
    }

    public Handler getBooleanHandler() {
        return booleanHandler;
    }

    public Handler getObjectHandler() {
        return objectHandler;
    }

    public Handler getEnumHandler() {
        return enumHandler;
    }

    public Handler getIntegerHandler() {
        return integerHandler;
    }

    public Handler getNumberHandler() {
        return numberHandler;
    }
}