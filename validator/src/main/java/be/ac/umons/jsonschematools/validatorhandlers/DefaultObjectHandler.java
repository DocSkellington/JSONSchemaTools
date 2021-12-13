package be.ac.umons.jsonschematools.validatorhandlers;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Validator;

public class DefaultObjectHandler implements Handler {

    @Override
    public boolean validate(final Validator validator, final JSONSchema schema, final Object object)
            throws JSONSchemaException {
        if (!(object instanceof JSONObject)) {
            return false;
        }

        final JSONObject document = (JSONObject) object;
        final int minProperties = schema.getIntOr("minProperties", 0);
        final int maxProperties = schema.getIntOr("maxProperties", Integer.MAX_VALUE);
        
        if (!(minProperties <= document.length() && document.length() <= maxProperties)) {
            return false;
        }

        Set<String> requiredKeys = schema.getRequiredPropertiesKeys();
        if (!document.keySet().containsAll(requiredKeys)) {
            return false;
        }

        for (String key : document.keySet()) {
            JSONSchema schemaForKey;
            final Object objectForKey = document.get(key);

            try {
                schemaForKey = schema.getSubSchemaProperties(key);
            }
            catch (JSONException e) {
                schemaForKey = schema.getStore().trueSchema();
            }

            if (!validator.validateValue(schemaForKey, objectForKey)) {
                return false;
            }
        }

        return true;
    }

}
