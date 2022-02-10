package be.ac.umons.jsonschematools.validatorhandlers;

import java.util.Objects;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.Validator;

/**
 * A handler to validate boolean values.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultBooleanHandler implements Handler {

    @Override
    public boolean validate(Validator validator, final JSONSchema schema, final Object object) {
        if (!(object instanceof Boolean)) {
            return false;
        }

        if (schema.getForbiddenValues().contains(object)) {
            return false;
        }

        if (schema.getConstValue() != null && !Objects.equals(schema.getConstValue(), object)) {
            return false;
        }
        return true;
    }

}
