package be.ac.umons.jsonschematools.validatorhandlers;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.Validator;

public class DefaultBooleanHandler implements Handler {

    @Override
    public boolean validate(Validator validator, final JSONSchema schema, final Object object) {
        if (!(object instanceof Boolean)) {
            return false;
        }
        return true;
    }

}
