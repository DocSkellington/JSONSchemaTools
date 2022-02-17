package be.ac.umons.jsonschematools.validatorhandlers;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Validator;

/**
 * A handler to validate abstract arrays.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultArrayHandler implements Handler {

    @Override
    public boolean validate(Validator validator, final JSONSchema schema, final Object object)
            throws JSONException, JSONSchemaException {
        if (!(object instanceof JSONArray)) {
            return false;
        }
        JSONArray array = (JSONArray) object;

        int minItem = schema.getIntOr("minItems", 0);
        int maxItem = schema.getIntOr("maxItems", Integer.MAX_VALUE);

        if (array.length() < minItem || array.length() > maxItem) {
            return false;
        }

        boolean atLeastOne = false;
        for (JSONSchema itemsArraySchema : schema.getItemsArray()) {
            boolean valid = true;
            for (Object item : array) {
                if (!validator.validateValue(itemsArraySchema, item)) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                atLeastOne = true;
                break;
            }
        }
        if (!atLeastOne) {
            return false;
        }

        Set<JSONArray> forbiddenValues = schema.getForbiddenValuesFilteredByType(JSONArray.class);
        if (forbiddenValues.stream().filter(v -> array.similar(v)).count() != 0) {
            return false;
        }

        if (schema.getConstValue() != null) {
            JSONArray abstracted = (JSONArray) AbstractConstants.abstractConstValue(schema.getConstValue());
            if (!array.similar(abstracted)) {
                return false;
            }
        }

        return true;
    }

}
