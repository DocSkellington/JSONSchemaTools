package be.ac.umons.jsonschematools;

import be.ac.umons.jsonschematools.validatorhandlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.validatorhandlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.validatorhandlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.validatorhandlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.validatorhandlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.validatorhandlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.validatorhandlers.DefaultStringHandler;

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
