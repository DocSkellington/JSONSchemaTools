package be.ac.umons.jsonschematools.validator.handlers;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.validator.Validator;

/**
 * A handler for the validator.
 * 
 * @author Gaëtan Staquet
 */
public interface Handler {
    boolean validate(final Validator validator, final JSONSchema schema, final Object object)
            throws JSONSchemaException;
}
