package be.ac.umons.jsonschematools.validator;

import java.util.List;
import java.util.Objects;

import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;
import be.ac.umons.jsonschematools.Type;
import be.ac.umons.jsonschematools.validator.handlers.Handler;

/**
 * Decides whether a JSON document satisfies a JSON schema.
 * 
 * This implementation relies on external classes to handle each type allowed in
 * a JSON schema. See the {@link be.ac.umons.jsonschematools.validator.handlers
 * handlers package} for implemented handlers.
 * 
 * @author Gaëtan Staquet
 */
public class Validator {

    private final Handler stringHandler;
    private final Handler integerHandler;
    private final Handler numberHandler;
    private final Handler booleanHandler;
    private final Handler enumHandler;
    private final Handler objectHandler;
    private final Handler arrayHandler;
    private long maxMemory = 0L;
    private long memoryStart = 0L;

    public Validator(final Handler stringHandler, final Handler integerHandler, final Handler numberHandler,
            final Handler booleanHandler, final Handler enumHandler, final Handler objectHandler,
            final Handler arrayHandler) {
        this.stringHandler = stringHandler;
        this.integerHandler = integerHandler;
        this.numberHandler = numberHandler;
        this.booleanHandler = booleanHandler;
        this.enumHandler = enumHandler;
        this.objectHandler = objectHandler;
        this.arrayHandler = arrayHandler;
    }

    public boolean validate(final JSONSchema schema, final JSONObject document) throws JSONSchemaException {
        memoryStart = getMemoryInUse();
        maxMemory = memoryStart;
        return this.objectHandler.validate(this, schema, document);
    }

    public Handler getArrayHandler() {
        return arrayHandler;
    }

    public Handler getStringHandler() {
        return stringHandler;
    }

    public Handler getBooleanHandler() {
        return booleanHandler;
    }

    public Handler getObjectHandler() {
        return objectHandler;
    }

    public Handler getEnumHandler() {
        return enumHandler;
    }

    public Handler getIntegerHandler() {
        return integerHandler;
    }

    public Handler getNumberHandler() {
        return numberHandler;
    }

    private boolean validateAllOf(final JSONSchema schema, final Object object) throws JSONSchemaException {
        final JSONSchema allOf = schema.getAllOf();
        if (JSONSchemaStore.isTrueSchema(allOf)) {
            return true;
        } else if (JSONSchemaStore.isFalseSchema(allOf)) {
            return false;
        }
        return validateValue(allOf, object);
    }

    private boolean validateAnyOf(final JSONSchema schema, final Object object) throws JSONSchemaException {
        final List<JSONSchema> listAnyOf = schema.getAnyOf();
        return validateAnyOf(listAnyOf, object);
    }

    private boolean validateAnyOf(final List<JSONSchema> listAnyOf, final Object object) throws JSONSchemaException {
        for (final JSONSchema anyOf : listAnyOf) {
            if (JSONSchemaStore.isTrueSchema(anyOf)) {
                return true;
            } else if (JSONSchemaStore.isFalseSchema(anyOf)) {
                continue;
            } else if (validateValue(anyOf, object)) {
                return true;
            }
        }
        return false;
    }

    private boolean validateOneOf(final JSONSchema schema, final Object object) throws JSONSchemaException {
        final List<JSONSchema> listOneOf = schema.getOneOf();
        boolean alreadyOne = false;
        for (final JSONSchema oneOf : listOneOf) {
            final boolean thisOne;
            if (JSONSchemaStore.isTrueSchema(oneOf)) {
                thisOne = true;
            } else if (JSONSchemaStore.isFalseSchema(oneOf)) {
                thisOne = false;
            } else {
                thisOne = validateAllOf(oneOf, object);
            }
            if (alreadyOne && thisOne) {
                return false;
            }
            alreadyOne = alreadyOne || thisOne;
        }
        return alreadyOne;
    }

    private boolean validateNot(final JSONSchema schema, final Object object) throws JSONSchemaException {
        final JSONSchema not = schema.getRawNot();
        return !validateValue(not, object, false);
    }

    private boolean validateValue(final JSONSchema schema, final Object object, final boolean abstractConstValue)
            throws JSONSchemaException {
        if (schema == null || JSONSchemaStore.isTrueSchema(schema)) {
            return true;
        } else if (JSONSchemaStore.isFalseSchema(schema)) {
            return false;
        }

        for (final Object forbidden : schema.getForbiddenValues()) {
            if (object.equals(forbidden)) {
                return false;
            }
        }

        List<Type> allowedTypes = schema.getAllowedTypes();

        for (Type type : allowedTypes) {
            final Handler handler;
            switch (type) {
                case BOOLEAN:
                    handler = getBooleanHandler();
                    break;
                case ENUM:
                    handler = getEnumHandler();
                    break;
                case INTEGER:
                    handler = getIntegerHandler();
                    break;
                case NUMBER:
                    handler = getNumberHandler();
                    break;
                case STRING:
                    handler = getStringHandler();
                    break;
                case OBJECT:
                    handler = getObjectHandler();
                    break;
                case ARRAY:
                    handler = getArrayHandler();
                    break;
                case NULL:
                default:
                    handler = null;
                    break;
            }

            final boolean valid;
            if (handler == null) {
                valid = (Objects.equals(object, null) || Objects.equals(object, JSONObject.NULL));
            } else {
                if (schema.getConstValue() != null && !(type == Type.ARRAY || type == Type.OBJECT)) {
                    if (abstractConstValue) {
                        return Objects.equals(object, AbstractConstants.abstractConstValue(schema.getConstValue()));
                    } else {
                        return Objects.equals(object, schema.getConstValue());
                    }
                }
                valid = handler.validate(this, schema, object);
            }

            // @formatter:off
            if (valid
                && validateAllOf(schema, object)
                && validateAnyOf(schema, object)
                && validateOneOf(schema, object)
                && validateNot(schema, object)) {
                return true;
            }
            // @formatter:on
        }
        return false;
    }

    /**
     * Validate a JSON value against a schema
     * 
     * @param schema The JSON schema
     * @param value  The value to validate
     * @return True iff the value is correct for the provided schema
     * @throws JSONSchemaException
     */
    public boolean validateValue(final JSONSchema schema, final Object value) throws JSONSchemaException {
        maxMemory = Math.max(maxMemory, getMemoryInUse());
        boolean valid = validateValue(schema, value, true);
        maxMemory = Math.max(maxMemory, getMemoryInUse());
        return valid;
    }

    /**
     * Gets the maximal memory (in kilobytes) used by the validator during the last
     * run.
     * 
     * @return The maximal memory consumed
     */
    public long getMaxMemoryUsed() {
        return maxMemory - memoryStart;
    }

    private long getMemoryInUse() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;
    }
}