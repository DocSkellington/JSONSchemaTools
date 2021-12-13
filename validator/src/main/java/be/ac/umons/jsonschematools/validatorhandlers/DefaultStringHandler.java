package be.ac.umons.jsonschematools.validatorhandlers;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.Validator;

public class DefaultStringHandler implements Handler {

    @Override
    public boolean validate(
            Validator validator, final JSONSchema schema, Object object) {
        if (!(object instanceof String)) {
            return false;
        }
        return object.equals(AbstractConstants.stringConstant);
    }

}
