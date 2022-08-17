package be.ac.umons.jsonschematools.validator.handlers;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.validator.Validator;

/**
 * A handler to validate abstracted string values.
 * 
 * @author Gaëtan Staquet
 */
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