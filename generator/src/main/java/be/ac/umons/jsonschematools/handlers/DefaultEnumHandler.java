package be.ac.umons.jsonschematools.handlers;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;

public class DefaultEnumHandler implements Handler<String> {

    @Override
    public <ST, IT, NT, BT, ET, OT extends JSONObject, AT extends JSONArray> String generate(
            Generator<ST, IT, NT, BT, ET, OT, AT> generator, JSONSchema schema, int maxTreeSize, Random rand)
            throws JSONSchemaException {
        return AbstractConstants.enumConstant;
    }
    
}
