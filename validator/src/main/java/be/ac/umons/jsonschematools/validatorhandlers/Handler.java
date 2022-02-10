package be.ac.umons.jsonschematools.validatorhandlers;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Validator;

/**
 * A handler for the validator.
 * 
 * @author Gaëtan Staquet
 */
public interface Handler {
    boolean validate(final Validator validator, final JSONSchema schema, final Object object)
            throws JSONSchemaException;
}
