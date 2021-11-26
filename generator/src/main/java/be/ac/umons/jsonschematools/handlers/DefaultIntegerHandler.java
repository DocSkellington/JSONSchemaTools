package be.ac.umons.jsonschematools.handlers;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.JSONSchema;

public class DefaultIntegerHandler implements Handler<String> {

    @Override
    public <ST, IT, NT, BT, ET, OT extends JSONObject, AT extends JSONArray> String generate(
            final Generator<ST, IT, NT, BT, ET, OT, AT> generator, final JSONSchema schema, final int maxTreeSize,
            final Random rand) {
        return AbstractConstants.integerConstant;
    }

}
