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

    public Generator(Handler<ST> stringHandler, Handler<IT> integerHandler, Handler<NT> numberHandler,
            Handler<BT> booleanHandler, Handler<ET> enumHandler, Handler<OT> objectHandler, Handler<AT> arrayHandler) {
        this.stringHandler = stringHandler;
        this.integerHandler = integerHandler;
        this.numberHandler = numberHandler;
        this.booleanHandler = booleanHandler;
        this.enumHandler = enumHandler;
        this.objectHandler = objectHandler;
        this.arrayHandler = arrayHandler;
    }

    public OT generate(JSONSchema schema, int maxTreeSize) throws JSONSchemaException, JSONException, GeneratorException {
        return generate(schema, maxTreeSize, new Random());
    }

    public OT generate(JSONSchema schema, int maxTreeSize, Random rand) throws JSONSchemaException, JSONException, GeneratorException {
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
