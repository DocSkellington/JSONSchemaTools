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
 * @author Gaëtan Staquet
 */
public final class JSONSchema {
    private final JSONObject object;
    private final JSONObject properties;
    private final Set<Type> types = new HashSet<>();
    private final JSONSchemaStore store;
    private final int fullSchemaId;

    JSONSchema(final JSONObject object, final JSONSchemaStore store, final int fullSchemaId)
            throws JSONSchemaException {
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
        } else {
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

    public boolean hasKey(String key) {
        return object.has(key);
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
                requiredProperties.put(key, getSubSchemaProperties(key));
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
                nonRequired.put(key, getSubSchemaProperties(key));
            }
        }

        return nonRequired;
    }

    private void addConstraintToSet(final Map<String, Set<Object>> constraints, final JSONObject schema,
            final Set<String> keys) {
        for (String key : keys) {
            if (schema.has(key)) {
                if (!constraints.containsKey(key)) {
                    constraints.put(key, new HashSet<>());
                }
                constraints.get(key).add(schema.get(key));
            }
        }
    }

    private JSONSchema transformConstraintsInSchema(final Map<String, Set<Object>> keyToValues) throws JSONSchemaException {
        // We will handle "not" afterwards. This is because we potentially need to merge
        // the constraints in "not" with the regular constraints
        final JSONObject constraints = new JSONObject();
        for (final Map.Entry<String, Set<Object>> entry : keyToValues.entrySet()) {
            final String key = entry.getKey();
            if (!key.equals("not")) {
                constraints.put(key, Keys.applyOperation(key, entry.getValue()));
            }
        }

        if (keyToValues.containsKey("not")) {
            final JSONObject not = (JSONObject) Keys.applyOperation("not", keyToValues.get("not"));
            for (final String key : not.keySet()) {
                if (constraints.has(key)) {
                    final Set<Object> values = Set.of(constraints.get(key), not.get(key));
                    constraints.put(key, Keys.applyOperation(key, values));
                } else {
                    constraints.put(key, not.get(key));
                }
            }
        }

        return new JSONSchema(constraints, store, fullSchemaId);
    }

    public JSONSchema getAllOf() throws JSONSchemaException {
        if (!object.has("allOf")) {
            return store.trueSchema();
        }
        final JSONArray allOf = object.getJSONArray("allOf");
        final Map<String, Set<Object>> keyToValues = new HashMap<>();
        for (int i = 0; i < allOf.length(); i++) {
            final JSONObject schema = allOf.getJSONObject(i);
            addConstraintToSet(keyToValues, schema, Keys.getKeys());
        }
        return transformConstraintsInSchema(keyToValues);
    }

    public List<JSONSchema> getAnyOf() throws JSONSchemaException {
        if (!object.has("anyOf")) {
            return Collections.singletonList(store.trueSchema());
        }
        final JSONArray anyOf = object.getJSONArray("anyOf");
        final List<JSONSchema> schemas = new ArrayList<>(anyOf.length());
        for (int i = 0 ; i < anyOf.length() ; i++) {
            final JSONObject subSchema = anyOf.getJSONObject(i);
            final Map<String, Set<Object>> keyToValues = new HashMap<>();
            addConstraintToSet(keyToValues, subSchema, Keys.getKeys());
            schemas.add(transformConstraintsInSchema(keyToValues));
        }
        return schemas;
    }

    public List<JSONSchema> getOneOf() throws JSONSchemaException {
        if (!object.has("oneOf")) {
            return Collections.singletonList(store.trueSchema());
        }
        final JSONArray oneOf = object.getJSONArray("oneOf");
        final List<JSONSchema> schemas = new ArrayList<>(oneOf.length());
        for (int i = 0 ; i < oneOf.length() ; i++) {
            final JSONObject subSchema = oneOf.getJSONObject(i);
            final Map<String, Set<Object>> keyToValues = new HashMap<>();
            addConstraintToSet(keyToValues, subSchema, Keys.getKeys());
            schemas.add(transformConstraintsInSchema(keyToValues));
        }
        
        final List<JSONSchema> combinations = new ArrayList<>(schemas.size());
        for (int i = 0 ; i < schemas.size() ; i++) {
            final JSONArray onePossibility = new JSONArray(schemas.size());
            final JSONSchema positive = schemas.get(i);
            onePossibility.put(positive.object);
            for (int j = 0 ; j < schemas.size() ; j++) {
                if (i == j) {
                    continue;
                }
                final JSONObject notSchema = new JSONObject();
                notSchema.put("not", schemas.get(j).object);
                onePossibility.put(notSchema);
            }
            final JSONObject allOf = new JSONObject();
            allOf.put("allOf", onePossibility);
            final JSONSchema schemaForPossibility = new JSONSchema(allOf, store, fullSchemaId);
            combinations.add(schemaForPossibility);
        }
        return combinations;
    }

    public JSONSchema merge(JSONSchema other) throws JSONSchemaException {
        if (other == null || other.object.isEmpty()) {
            return this;
        }
        Map<String, Set<Object>> keyToValues = new HashMap<>();
        for (String key : object.keySet()) {
            Object value = object.get(key);
            if (!keyToValues.containsKey(key)) {
                keyToValues.put(key, new HashSet<>());
            }
            keyToValues.get(key).add(value);
        }
        for (String key : other.object.keySet()) {
            Object value = other.object.get(key);
            if (!keyToValues.containsKey(key)) {
                keyToValues.put(key, new HashSet<>());
            }
            keyToValues.get(key).add(value);
        }

        JSONObject constraints = new JSONObject();
        for (Map.Entry<String, Set<Object>> entry : keyToValues.entrySet()) {
            constraints.put(entry.getKey(), Keys.applyOperation(entry.getKey(), entry.getValue()));
        }

        return new JSONSchema(constraints, store, fullSchemaId);
    }

    public JSONSchema mergeNot(JSONSchema other) throws JSONSchemaException {
        if (other == null || other.object.isEmpty()) {
            return this;
        }
        Set<String> keysToKeepInOther = Keys.keysToKeepInNot();
        Map<String, Set<Object>> keyToNotValues = new HashMap<>();
        for (String key : other.object.keySet()) {
            if (keysToKeepInOther.contains(key)) {
                Object value = other.object.get(key);
                if (!keyToNotValues.containsKey(key)) {
                    keyToNotValues.put(key, new HashSet<>());
                }
                keyToNotValues.get(key).add(value);
            }
        }
        final JSONObject not = new JSONObject();
        for (Map.Entry<String, Set<Object>> entry : keyToNotValues.entrySet()) {
            final Set<Object> values = new HashSet<>();
            final String key = entry.getKey();
            values.add(Keys.applyNot(key, entry.getValue()));
            if (object.has(key)) {
                values.add(object.get(key));
            }
            not.put(key, Keys.applyOperation(key, values));
        }

        Map<String, Set<Object>> keyToValues = new HashMap<>();
        for (String key : object.keySet()) {
            Object value = object.get(key);
            if (!keyToValues.containsKey(key)) {
                keyToValues.put(key, new HashSet<>());
            }
            keyToValues.get(key).add(value);
        }
        for (String key : not.keySet()) {
            if (keysToKeepInOther.contains(key)) {
                Object value = not.get(key);
                if (!keyToValues.containsKey(key)) {
                    keyToValues.put(key, new HashSet<>());
                }
                keyToValues.get(key).add(value);
            }
        }

        JSONObject constraints = new JSONObject();
        for (Map.Entry<String, Set<Object>> entry : keyToValues.entrySet()) {
            constraints.put(entry.getKey(), Keys.applyOperation(entry.getKey(), entry.getValue()));
        }
        return new JSONSchema(constraints, store, fullSchemaId);
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

    private JSONSchema handleRef(String reference) throws JSONException, JSONSchemaException {
        if (reference.charAt(0) == '#') {
            // Recursive reference
            String[] decompositions = reference.split("/");
            JSONSchema targetSchema = store.get(fullSchemaId);
            for (int i = 1; i < decompositions.length; i++) {
                String key = decompositions[i];
                targetSchema = targetSchema.getSubSchemaProperties(key);
            }
            return targetSchema;
        } else {
            // Reference to an other file
            try {
                return store.loadRelative(fullSchemaId, reference);
            } catch (FileNotFoundException e) {
                throw new JSONSchemaException("The schema referenced by " + reference
                        + " can not be found. Check that the file is present in our local machine as this implementation does not download files.");
            }
        }
    }

    public JSONSchema getSubSchemaProperties(String key) throws JSONException, JSONSchemaException {
        return getSubSchema(key, properties);
    }

    public JSONSchema getSubSchema(String key) throws JSONException, JSONSchemaException {
        return getSubSchema(key, object);
    }

    public JSONSchema getSubSchemaInAllOfDueToMerge(String key) throws JSONException, JSONSchemaException {
        JSONSchema allOf = getAllOf();
        if (object.getJSONArray("allOf").getJSONObject(0).has(Keys.dueToMergeKey)) {
            allOf.object.put(Keys.dueToMergeKey, new JSONObject());
        }
        if (allOf.hasKey("$ref")) {
            return handleRef(allOf.getString("$ref"));
        }
        return allOf;
    }

    private JSONSchema getSubSchema(String key, JSONObject object) throws JSONException, JSONSchemaException {
        JSONObject subObject = object.getJSONObject(key);
        if (subObject.has("$ref")) {
            return handleRef(subObject.getString("$ref"));
        } else {
            return new JSONSchema(subObject, store, fullSchemaId);
        }
    }

    public JSONSchema getItemsArray() throws JSONException, JSONSchemaException {
        // TODO: sometimes, items is a list of objects (or just an object), not a list
        // of types
        if (!object.has("items")) {
            return null;
        }
        JSONObject subObject = object.getJSONObject("items");
        if (subObject.has("$ref")) {
            return handleRef(subObject.getString("$ref"));
        } else {
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

    public int getSchemaId() {
        return fullSchemaId;
    }

    public JSONSchemaStore getStore() {
        return store;
    }

    @Override
    public String toString() {
        return object.toString(2);
    }

}
