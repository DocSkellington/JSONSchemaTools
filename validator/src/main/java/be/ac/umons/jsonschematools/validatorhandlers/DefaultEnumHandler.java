package be.ac.umons.jsonschematools.validatorhandlers;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.Validator;

/**
 * A handler to validate abstracted enumeration values.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultEnumHandler implements Handler {

    @Override
    public boolean validate(
            Validator validator, final JSONSchema schema, final Object object) {
        if (!(object instanceof String)) {
            return false;
        }
        return object.equals(AbstractConstants.enumConstant);
    }

}
