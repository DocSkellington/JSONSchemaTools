package be.ac.umons.jsonschematools.handlers;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Validator;

public interface Handler {
    boolean validate(final Validator validator, final JSONSchema schema, final Object object)
            throws JSONSchemaException;
}
