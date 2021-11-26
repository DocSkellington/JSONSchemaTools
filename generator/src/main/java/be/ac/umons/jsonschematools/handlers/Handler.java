package be.ac.umons.jsonschematools.handlers;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;

public interface Handler<T> {
    <ST, IT, NT, BT, ET, OT extends JSONObject, AT extends JSONArray> T generate(final Generator<ST, IT, NT, BT, ET, OT, AT> generator,
            final JSONSchema schema, final int maxTreeSize, final Random rand) throws JSONSchemaException, GeneratorException, JSONException;
}
