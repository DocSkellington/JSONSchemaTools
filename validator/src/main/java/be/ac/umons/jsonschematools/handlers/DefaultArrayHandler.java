package be.ac.umons.jsonschematools.handlers;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Type;
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
            if (!validateItem(validator, itemsArraySchema, item)) {
                return false;
            }
        }

        return true;
    }

    private static boolean validateItem(Validator validator, final JSONSchema schema, final Object object) throws JSONSchemaException {
        Set<Type> allowedTypes = schema.getTypes();
        for (Type type : allowedTypes) {
            final boolean valid;
            switch (type) {
            case BOOLEAN:
                valid = validator.getBooleanHandler().validate(validator, schema, object);
                break;
            case ENUM:
                valid = validator.getEnumHandler().validate(validator, schema, object);
                break;
            case INTEGER:
                valid = validator.getIntegerHandler().validate(validator, schema, object);
                break;
            case NUMBER:
                valid = validator.getNumberHandler().validate(validator, schema, object);
                break;
            case STRING:
                valid = validator.getStringHandler().validate(validator, schema, object);
                break;
            case OBJECT:
                valid = validator.getObjectHandler().validate(validator, schema, object);
                break;
            case ARRAY:
                valid = validator.getArrayHandler().validate(validator, schema, object);
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
