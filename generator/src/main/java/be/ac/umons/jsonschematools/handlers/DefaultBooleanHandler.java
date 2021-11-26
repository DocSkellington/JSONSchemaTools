package be.ac.umons.jsonschematools.handlers;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.JSONSchema;

public class DefaultBooleanHandler implements Handler<Boolean> {

    @Override
    public <ST, IT, NT, BT, ET, OT extends JSONObject, AT extends JSONArray> Boolean generate(
            Generator<ST, IT, NT, BT, ET, OT, AT> generator, JSONSchema schema, int maxTreeSize, Random rand) {
        return rand.nextBoolean();
    }
    
}
