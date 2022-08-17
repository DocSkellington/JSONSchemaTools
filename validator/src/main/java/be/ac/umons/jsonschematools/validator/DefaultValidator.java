package be.ac.umons.jsonschematools.validator;

import be.ac.umons.jsonschematools.validator.handlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultStringHandler;

/**
 * Constructs a {@link Validator} with default handlers.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultValidator extends Validator {
    public DefaultValidator() {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(), new DefaultBooleanHandler(), new DefaultEnumHandler(), new DefaultObjectHandler(), new DefaultArrayHandler());
    }
}
