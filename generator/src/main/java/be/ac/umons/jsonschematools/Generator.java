package be.ac.umons.jsonschematools;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.handlers.Handler;

public class Generator<ST, IT, NT, BT, ET, OT extends JSONObject, AT extends JSONArray> {

    private final Handler<ST> stringHandler;
    private final Handler<IT> integerHandler;
    private final Handler<NT> numberHandler;
    private final Handler<BT> booleanHandler;
    private final Handler<ET> enumHandler;
    private final Handler<OT> objectHandler;
    private final Handler<AT> arrayHandler;

    public Generator(final Handler<ST> stringHandler, final Handler<IT> integerHandler, final Handler<NT> numberHandler,
            final Handler<BT> booleanHandler, final Handler<ET> enumHandler, final Handler<OT> objectHandler,
            final Handler<AT> arrayHandler) {
        this.stringHandler = stringHandler;
        this.integerHandler = integerHandler;
        this.numberHandler = numberHandler;
        this.booleanHandler = booleanHandler;
        this.enumHandler = enumHandler;
        this.objectHandler = objectHandler;
        this.arrayHandler = arrayHandler;
    }

    public OT generate(final JSONSchema schema, final int maxTreeSize)
            throws JSONSchemaException, JSONException, GeneratorException {
        return generate(schema, maxTreeSize, new Random());
    }

    public OT generate(final JSONSchema schema, final int maxTreeSize, final Random rand)
            throws JSONSchemaException, JSONException, GeneratorException {
        return objectHandler.generate(this, schema, maxTreeSize, rand);
    }

    public Handler<ST> getStringHandler() {
        return stringHandler;
    }

    public Handler<IT> getIntegerHandler() {
        return integerHandler;
    }

    public Handler<NT> getNumberHandler() {
        return numberHandler;
    }

    public Handler<OT> getObjectHandler() {
        return objectHandler;
    }

    public Handler<AT> getArrayHandler() {
        return arrayHandler;
    }

    public Handler<BT> getBooleanHandler() {
        return booleanHandler;
    }

    public Handler<ET> getEnumHandler() {
        return enumHandler;
    }
}
