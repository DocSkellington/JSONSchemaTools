package be.ac.umons.jsonschematools.handlers;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Type;
import be.ac.umons.jsonschematools.Validator;

public class DefaultObjectHandler implements Handler {

    @Override
    public boolean validate(final Validator validator, final JSONSchema schema, final Object object)
            throws JSONSchemaException {
        if (!(object instanceof JSONObject)) {
            return false;
        }

        final JSONObject document = (JSONObject) object;
        Set<String> requiredKeys = schema.getRequiredProperties().keySet();
        if (!document.keySet().containsAll(requiredKeys)) {
            return false;
        }

        for (String key : document.keySet()) {
            if (!validateValue(validator, schema, document, key)) {
                return false;
            }
        }

        return true;
    }

    static boolean validateValue(final Validator validator, final JSONSchema schema, final JSONObject object,
            final String key) throws JSONException, JSONSchemaException {
        final Object value = object.get(key);
        final JSONSchema schemaForKey = schema.getSubSchemaProperties(key);
        final Set<Type> allowedTypes = schemaForKey.getAllowedTypes();
        for (Type type : allowedTypes) {
            final boolean valid;
            switch (type) {
            case ARRAY:
                valid = validator.getArrayHandler().validate(validator, schemaForKey, value);
                break;
            case BOOLEAN:
                valid = validator.getBooleanHandler().validate(validator, schemaForKey, value);
                break;
            case ENUM:
                valid = validator.getEnumHandler().validate(validator, schemaForKey, value);
                break;
            case INTEGER:
                valid = validator.getIntegerHandler().validate(validator, schemaForKey, value);
                break;
            case NUMBER:
                valid = validator.getNumberHandler().validate(validator, schemaForKey, value);
                break;
            case OBJECT:
                valid = validator.getObjectHandler().validate(validator, schemaForKey, value);
                break;
            case STRING:
                valid = validator.getStringHandler().validate(validator, schemaForKey, value);
                break;
            case NULL:
            default:
                valid = false;
                break;
            }

            if (valid) {
                return true;
            }
        }
        return false;
    }

}
