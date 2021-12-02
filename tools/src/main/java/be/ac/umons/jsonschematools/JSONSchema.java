package be.ac.umons.jsonschematools;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A wrapper around a JSON document storing a schema.
 * 
 * TODO: allOf, anyOf, and so on
 * 
 * @author Gaëtan Staquet
 */
public final class JSONSchema {
    private final JSONObject object;
    private final JSONObject properties;
    private final Set<Type> types = new HashSet<>();
    private final JSONSchemaStore store;
    private final int fullSchemaId;

    JSONSchema(final JSONObject object, final JSONSchemaStore store, final int fullSchemaId) throws JSONSchemaException {
        this.object = object;
        this.store = store;
        this.fullSchemaId = fullSchemaId;

        if (object.has("type")) {
            try {
                JSONArray arrayType = object.getJSONArray("type");
                for (Object t : arrayType) {
                    if (t.getClass() != String.class) {
                        throw new JSONSchemaException("Elements in an array for key \"type\" must be strings");
                    }
                    addType((String) t);
                }
            } catch (JSONException e) {
                try {
                    addType(object.getString("type"));
                } catch (JSONException e2) {
                    throw new JSONSchemaException("The value for the key \"type\" must be a string");
                }
            }
        } else if (object.has("enum")) {
            types.add(Type.ENUM);
        } else if (object.has("const")) {
            // TODO: get type of data
        }

        if (types.isEmpty()) {
            types.addAll(EnumSet.allOf(Type.class));
        }

        if (isObject()) {
            JSONObject prop = null;
            try {
                prop = object.getJSONObject("properties");
            } catch (JSONException e) {
            }
            this.properties = prop;
        } else {
            this.properties = null;
        }
    }

    private void addType(String type) {
        types.add(Type.valueOf(type.toUpperCase()));
    }

    public Set<Type> getAllowedTypes() {
        return types;
    }

    public List<Type> getListTypes() {
        return new ArrayList<>(types);
    }

    public boolean isEnum() {
        return getAllowedTypes().contains(Type.ENUM);
    }

    public boolean isObject() {
        return getAllowedTypes().contains(Type.OBJECT);
    }

    public boolean isArray() {
        return getAllowedTypes().contains(Type.ARRAY);
    }

    public boolean isInteger() {
        return getAllowedTypes().contains(Type.INTEGER);
    }

    public boolean isNumber() {
        return getAllowedTypes().contains(Type.NUMBER);
    }

    public boolean isBoolean() {
        return getAllowedTypes().contains(Type.BOOLEAN);
    }

    public boolean isString() {
        return getAllowedTypes().contains(Type.STRING);
    }

    public boolean isNull() {
        return getAllowedTypes().contains(Type.NULL);
    }

    public Map<String, JSONSchema> getRequiredProperties() throws JSONSchemaException {
        if (!isObject()) {
            throw new JSONSchemaException("Required properties are only defined for objects");
        }

        if (object.has("required")) {
            Map<String, JSONSchema> requiredProperties = new HashMap<>();
            JSONArray required = object.getJSONArray("required");
            for (Object value : required) {
                if (value.getClass() != String.class) {
                    throw new JSONSchemaException("Values in the \"required\" field must be strings");
                }
                String key = (String) value;
                requiredProperties.put(key, getSubSchema(key));
            }
            return requiredProperties;
        } else {
            return Collections.emptyMap();
        }
    }

    public Map<String, JSONSchema> getNonRequiredProperties() throws JSONSchemaException {
        if (!isObject()) {
            throw new JSONSchemaException("Required properties are only defined for objects");
        }

        Set<String> requiredKeys = new HashSet<>();
        if (object.has("required")) {
            for (Object value : object.getJSONArray("required")) {
                if (value.getClass() != String.class) {
                    throw new JSONSchemaException("Values in the \"required\" field must be strings");
                }
                String key = (String) value;
                requiredKeys.add(key);
            }
        }

        Map<String, JSONSchema> nonRequired = new HashMap<>();
        for (String key : properties.keySet()) {
            if (!requiredKeys.contains(key)) {
                nonRequired.put(key, getSubSchema(key));
            }
        }

        return nonRequired;
    }

    private void parseIntConstraints(final Constraints constraints, final JSONObject schema, final String... keys) {
        for (String key : keys) {
            if (schema.has(key)) {
                constraints.addConstraint(key, schema.getInt(key));
            }
        }
    }

    private void parseDoubleConstraints(final Constraints constraints, final JSONObject schema, final String... keys) {
        for (String key : keys) {
            if (schema.has(key)) {
                constraints.addConstraint(key, schema.getDouble(key));
            }
        }
    }

    private void parseStringConstraints(final Constraints constraints, final JSONObject schema, final String... keys) {
        for (String key : keys) {
            if (schema.has(key)) {
                constraints.addConstraint(key, schema.getString(key));
            }
        }
    }

    private void parseBooleanConstraints(final Constraints constraints, final JSONObject schema, final String... keys) {
        for (String key : keys) {
            if (schema.has(key)) {
                constraints.addConstraint(key, schema.getBoolean(key));
            }
        }
    }

    private void parseObjectConstraints(final Constraints constraints, final JSONObject schema, final String... keys) {
        for (String key : keys) {
            if (schema.has(key)) {
                constraints.addConstraint(key, schema.getJSONObject(key));
            }
        }
    }

    private void parseArrayConstraints(final Constraints constraints, final JSONObject schema, final String... keys) {
        for (String key : keys) {
            if (schema.has(key)) {
                constraints.addConstraint(key, schema.getJSONArray(key));
            }
        }
    }

    public Constraints getAllOf() {
        // TODO: construct a structure with everything that is required, or null if requirements clash
        if (!object.has("allOf")) {
            return new Constraints();
        }
        JSONArray allOf = object.getJSONArray("allOf");
        Constraints constraints = new Constraints();
        for (int i = 0 ; i < allOf.length() ; i++) {
            JSONObject schema = allOf.getJSONObject(i);
            if (schema.has("type")) { 
                try {
                    constraints.addConstraint("type", schema.getString("type"));
                }
                catch (JSONException e) {
                    constraints.addConstraint("type", schema.getJSONArray("type"));
                }
            }
            // TODO: handle "const", "items", "allOf", "anyOf", "oneOf", "not"
            // TODO: handle ref?
            // TODO: handle if?
            parseIntConstraints(constraints, schema, "multipleOf", "maximum", "exclusiveMaximum", "minimum", "exclusiveMinimum", "maxLength", "minLength", "minItems", "maxItems", "maxContains", "minContains", "maxProperties", "minProperties");
            parseArrayConstraints(constraints, schema, "required", "enum", "prefixItems");
            parseObjectConstraints(constraints, schema, "properties", "contains", "dependentRequired");
            parseStringConstraints(constraints, schema, "pattern");
            parseBooleanConstraints(constraints, schema, "uniqueItems");
        }

        return constraints;
    }

    public JSONSchema getNot() throws JSONSchemaException {
        if (object.has("not")) {
            JSONObject not = object.getJSONObject("not");
            if (not.has("$ref")) {
                return handleRef(not.getString("$ref"));
            }
            return new JSONSchema(not, store, fullSchemaId);
        }
        return null;
    }

    public JSONSchema mergeWithConstraints(Constraints constraints) {
    }

    public Set<String> getAllKeys() {
        // TODO
        return null;
    }

    public Set<Type> getAllValueTypes() {
        // TODO
        return null;
    }

    private JSONSchema handleRef(String reference) throws JSONException, JSONSchemaException {
        if (reference.charAt(0) == '#') {
            // Recursive reference
            String[] decompositions = reference.split("/");
            JSONSchema targetSchema = store.get(fullSchemaId);
            for (int i = 1 ; i < decompositions.length ; i++) {
                String key = decompositions[i];
                targetSchema = targetSchema.getSubSchema(key);
            }
            return targetSchema;
        }
        else {
            // Reference to an other file
            try {
                return store.loadRelative(fullSchemaId, reference);
            }
            catch (FileNotFoundException e) {
                throw new JSONSchemaException("The schema referenced by " + reference + " can not be found. Check that the file is present in our local machine as this implementation does not download files.");
            }
        }
    }

    public JSONSchema getSubSchema(String key) throws JSONException, JSONSchemaException {
        JSONObject subObject = properties.getJSONObject(key);
        if (subObject.has("$ref")) {
            return handleRef(subObject.getString("$ref"));
        }
        else {
            return new JSONSchema(subObject, store, fullSchemaId);
        }
    }

    public JSONSchema getItemsArray() throws JSONException, JSONSchemaException {
        // TODO: sometimes, items is a list of objects (or just an object), not a list of types
        JSONObject subObject = object.getJSONObject("items");
        if (subObject.has("$ref")) {
            return handleRef(subObject.getString("$ref"));
        }
        else {
            return new JSONSchema(subObject, store, fullSchemaId);
        }
    }

    public int getInt(String key) {
        return object.getInt(key);
    }

    public int getIntOr(String key, int defaultValue) {
        return object.optInt(key, defaultValue);
    }

    public double getDouble(String key) {
        return object.getDouble(key);
    }

    public double getDoubleOr(String key, double defaultValue) {
        return object.optDouble(key, defaultValue);
    }

    public Number getNumber(String key) {
        return object.getNumber(key);
    }

    public Number getNumberOr(String key, Number defaultValue) {
        return object.optNumber(key, defaultValue);
    }

    public String getString(String key) {
        return object.getString(key);
    }

    public String getStringOr(String key, String defaultValue) {
        return object.optString(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return object.getBoolean(key);
    }

    public boolean getBooleanOr(String key, boolean defaultValue) {
        return object.optBoolean(key, defaultValue);
    }

    @Override
    public String toString() {
        return object.toString();
    }
}