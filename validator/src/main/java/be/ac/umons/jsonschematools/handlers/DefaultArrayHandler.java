package be.ac.umons.jsonschematools.handlers;

import org.json.JSONArray;
import org.json.JSONException;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Validator;

public class DefaultArrayHandler implements Handler {

    @Override
    public boolean validate(Validator validator, final JSONSchema schema, final Object object)
            throws JSONException, JSONSchemaException {
        if (!(object instanceof JSONArray)) {
            return false;
        }
        JSONArray array = (JSONArray) object;

        JSONSchema itemsArraySchema = schema.getItemsArray();
        int minItem = schema.getIntOr("minItems", 0);
        int maxItem = schema.getIntOr("maxItems", Integer.MAX_VALUE);

        if (array.length() < minItem || array.length() > maxItem) {
            return false;
        }

        for (Object item : array) {
            if (!validator.validateValue(itemsArraySchema, item)) {
                return false;
            }
        }

        return true;
    }

}
