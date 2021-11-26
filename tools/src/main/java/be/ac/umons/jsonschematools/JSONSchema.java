package be.ac.umons.jsonschematools;

import java.util.ArrayList;
import java.util.Collections;
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
 * @author Gaëtan Staquet
 */
public final class JSONSchema {
    private final JSONObject object;
    private final JSONObject properties;
    private final Set<Type> types = new HashSet<>();

    JSONSchema(JSONObject object) throws JSONSchemaException {
        this.object = object;

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

    public Set<Type> getTypes() {
        return types;
    }

    public List<Type> getListTypes() {
        return new ArrayList<>(types);
    }

    public boolean isEnum() {
        return getTypes().contains(Type.ENUM);
    }

    public boolean isObject() {
        return getTypes().contains(Type.OBJECT);
    }

    public boolean isArray() {
        return getTypes().contains(Type.ARRAY);
    }

    public boolean isInteger() {
        return getTypes().contains(Type.INTEGER);
    }

    public boolean isNumber() {
        return getTypes().contains(Type.NUMBER);
    }

    public boolean isBoolean() {
        return getTypes().contains(Type.BOOLEAN);
    }

    public boolean isString() {
        return getTypes().contains(Type.STRING);
    }

    public boolean isNull() {
        return getTypes().contains(Type.NULL);
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

    public Set<String> getAllKeys() {
        // TODO
        return null;
    }

    public Set<Type> getAllValueTypes() {
        // TODO
        return null;
    }

    private JSONSchema getSubSchema(String key) throws JSONException, JSONSchemaException {
        return new JSONSchema(properties.getJSONObject(key));
    }

    public JSONSchema getItemsArray() throws JSONException, JSONSchemaException {
        return new JSONSchema(object.getJSONObject("items"));
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
}
