package be.ac.umons.jsonschematools;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
            if (object.has("properties")) {
                this.properties = object.getJSONObject("properties");
            }
            else {
                this.properties = new JSONObject();
            }
            JSONObject additionalProperties = getAdditionalProperties();

            if (!JSONSchemaStore.isTrueDocument(additionalProperties) && !JSONSchemaStore.isFalseDocument(additionalProperties)) {
                for (final String key : additionalProperties.keySet()) {
                    if (!this.properties.has(key)) {
                        this.properties.put(key, additionalProperties.getJSONObject(key));
                    }
                }
            }
        } else {
            this.properties = null;
        }
    }

    private JSONObject getAdditionalProperties() throws JSONSchemaException {
        JSONObject schemaForAdditionalProperties;
        if (this.object.has("additionalProperties")) {
            Object additionalProperties = object.get("additionalProperties");
            if (additionalProperties instanceof Boolean) {
                boolean value = (Boolean)additionalProperties;
                if (value) {
                    schemaForAdditionalProperties = JSONSchemaStore.trueDocument();
                }
                else {
                    schemaForAdditionalProperties = JSONSchemaStore.falseDocument();
                }
            }
            else if (additionalProperties instanceof JSONObject) {
                JSONObject value = (JSONObject)additionalProperties;
                if (value.has("$ref")) {
                    schemaForAdditionalProperties = handleRef(value.getString("$ref")).object;
                }
                else {
                    schemaForAdditionalProperties = value;
                }
            }
            else {
                throw new JSONSchemaException("Invalid schema: the value for \"additionalProperties\" must be a valid JSON Schema. Received: "+ additionalProperties);
            }
        }
        else {
            schemaForAdditionalProperties = JSONSchemaStore.trueDocument();
        }
        return schemaForAdditionalProperties;
    }

    private void addType(String type) {
        types.add(Type.valueOf(type.toUpperCase()));
    }

    public Set<String> getAllKeysDefinedInSchema() throws JSONSchemaException {
        class InQueue {
            public final String path;
            public final JSONObject object;

            public InQueue(final String path, final JSONObject object) {
                this.path = path;
                this.object = object;
            }
        }

        if (!isObject() && !isArray()) {
            return Collections.emptySet();
        }

        final Set<String> allKeys = new HashSet<>();
        final Set<String> seenPaths = new HashSet<>(); // Store "#/properties/object/value", and so on
        final Queue<InQueue> queue = new LinkedList<>();
        queue.add(new InQueue("#", object));
        seenPaths.add("#");

        while (!queue.isEmpty()) {
            final InQueue current = queue.poll();
            JSONObject document = (JSONObject) current.object;
            if (document.has("$ref")) {
                final String ref = document.getString("$ref");
                if (seenPaths.contains(ref)) {
                    continue;
                }
                document = handleRef(ref).object;
                seenPaths.add(ref);
            }
            if (document.has("properties")) {
                final JSONObject properties = document.getJSONObject("properties");
                for (final String key : properties.keySet()) {
                    JSONObject prop = properties.getJSONObject(key);
                    allKeys.add(key);
                    if (prop.has("$ref")) {
                        queue.add(new InQueue(current.path + "/properties", prop));
                        continue;
                    }
                    else {
                        final String path = current.path + "/properties/" + key;
                        if (!seenPaths.contains(path)) {
                            seenPaths.add(path);
                            queue.add(new InQueue(path, prop));
                        }
                    }
                }
            }
            if (document.has("additionalProperties")) {
                final Object additionalProperties = document.get("additionalProperties");
                if (additionalProperties instanceof JSONObject) {
                    final JSONObject addProperties = (JSONObject)additionalProperties;
                    if (addProperties.has("$ref")) {
                        queue.add(new InQueue(current.path + "/additionalProperties", addProperties));
                    }
                    else {
                        for (final String key : addProperties.keySet()) {
                            JSONObject prop = addProperties.getJSONObject(key);
                            allKeys.add(key);
                            if (prop.has("$ref")) {
                                queue.add(new InQueue(current.path + "/additionalProperties/" + key, prop));
                            }
                            else {
                                final String path = current.path + "/additionalProperties/" + key;
                                if (!seenPaths.contains(path)) {
                                    seenPaths.add(path);
                                    queue.add(new InQueue(path, prop));
                                }
                            }
                        }
                    }
                }
            }
            if (document.has("items")) {
                final JSONObject items = document.getJSONObject("items");
                final String path = current.path + "/items";
                if (!seenPaths.contains(path)) {
                    seenPaths.add(path);
                    queue.add(new InQueue(path, items));
                }
            }
            if (document.has("allOf")) {
                final JSONArray allOf = document.getJSONArray("allOf");
                final String path = current.path + "/allOf";
                for (final Object object : allOf) {
                    final JSONObject subSchema = (JSONObject)object;
                    queue.add(new InQueue(path, subSchema));
                }
            }
            if (document.has("anyOf")) {
                final JSONArray anyOf = document.getJSONArray("anyOf");
                final String path = current.path + "/anyOf";
                for (final Object object : anyOf) {
                    final JSONObject subSchema = (JSONObject)object;
                    queue.add(new InQueue(path, subSchema));
                }
            }
            if (document.has("oneOf")) {
                final JSONArray oneOf = document.getJSONArray("oneOf");
                final String path = current.path + "/oneOf";
                for (final Object object : oneOf) {
                    final JSONObject subSchema = (JSONObject)object;
                    queue.add(new InQueue(path, subSchema));
                }
            }
            if (document.has("not")) {
                final JSONObject not = document.getJSONObject("not");
                queue.add(new InQueue(current.path + "/not", not));
            }
        }

        return allKeys;
    }

    public Set<Type> getAllowedTypes() {
        return types;
    }

    public Set<Object> getForbiddenValues() {
        Set<Object> forbiddenValues = new HashSet<>();
        if (object.has("anyOf")) {
            JSONArray anyOf = object.getJSONArray("anyOf");
            for (int i = 0 ; i < anyOf.length() ; i++) {
                JSONObject subSchema = anyOf.getJSONObject(i);
                if (subSchema.has("not")) {
                    JSONObject not = subSchema.getJSONObject("not");
                    if (not.has("enum")) {
                        JSONArray enumArray = not.getJSONArray("enum");
                        for (int j = 0 ; j < enumArray.length() ; j++) {
                            forbiddenValues.add(enumArray.get(j));
                        }
                    }
                }
            }
        }
        return forbiddenValues;
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

    public boolean needsFurtherUnfolding() {
        if (object.has("not")) {
            JSONObject not = object.getJSONObject("not");
            if (not.length() == 1 && not.has("enum")) {
                return false;
            }
            return true;
        }

        JSONArray array = null;
        if (object.has("allOf")) {
            array = object.getJSONArray("allOf");
        }
        else if (object.has("anyOf")) {
            array = object.getJSONArray("anyOf");
        }
        else if (object.has("oneOf")) {
            array = object.getJSONArray("oneOf");
        }
        else {
            return false;
        }
        if (array.length() != 1) {
            return true;
        }
        try {
            JSONObject subSchema = array.getJSONObject(0);
            if (subSchema.has("not")) {
                JSONObject not = subSchema.getJSONObject("not");
                if (not.length() == 1 && not.has("enum")) {
                    return false;
                }
                else {
                    return true;
                }
            }
            else {
                return true;
            }
        }
        catch (JSONException e) {
            return true;
        }
    }

    public Set<String> getRequiredPropertiesKeys() throws JSONSchemaException {
        if (!isObject()) {
            throw new JSONSchemaException("Required properties are only defined for objects");
        }

        if (object.has("required")) {
            Set<String> keys = new HashSet<>();
            JSONArray required = object.getJSONArray("required");
            for (int i = 0 ; i < required.length() ; i++) {
                keys.add(required.getString(i));
            }
            return keys;
        }
        else {
            return Collections.emptySet();
        }
    }

    public Map<String, JSONSchema> getRequiredProperties() throws JSONSchemaException {
        if (!isObject()) {
            throw new JSONSchemaException("Required properties are only defined for objects");
        }

        if (object.has("required")) {
            Map<String, JSONSchema> requiredProperties = new HashMap<>();
            JSONArray required = object.getJSONArray("required");
            for (Object value : required) {
                if (!(value instanceof String)) {
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

    private JSONSchema transformConstraintsInSchema(final Map<String, Set<Object>> keyToValues)
            throws JSONSchemaException {
        // We will handle "not" afterwards. This is because we potentially need to merge
        // the constraints in "not" with the regular constraints
        final JSONObject constraints = new JSONObject();
        for (final Map.Entry<String, Set<Object>> entry : keyToValues.entrySet()) {
            final String key = entry.getKey();
            if (!key.equals("not")) {
                Object valueAfterOperation = Keys.applyOperation(key, entry.getValue());
                constraints.put(key, valueAfterOperation);
            }
        }

        handleNotInMerge(constraints, keyToValues);
        return new JSONSchema(constraints, store, fullSchemaId);
    }

    public JSONSchema dropAllOfAnyOfOneOfAndNot() throws JSONSchemaException {
        JSONObject newSchema = new JSONObject();
        for (String key : object.keySet()) {
            if (key.equals("allOf") || key.equals("anyOf") || key.equals("oneOf") || key.equals("not")) {
                continue;
            }
            newSchema.put(key, object.get(key));
        }
        return new JSONSchema(newSchema, store, fullSchemaId);
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
        for (int i = 0; i < anyOf.length(); i++) {
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
        for (int i = 0; i < oneOf.length(); i++) {
            final JSONObject subSchema = oneOf.getJSONObject(i);
            final Map<String, Set<Object>> keyToValues = new HashMap<>();
            addConstraintToSet(keyToValues, subSchema, Keys.getKeys());
            schemas.add(transformConstraintsInSchema(keyToValues));
        }

        final List<JSONSchema> combinations = new ArrayList<>(schemas.size());
        for (int i = 0; i < schemas.size(); i++) {
            final JSONArray onePossibility = new JSONArray(schemas.size());
            final JSONSchema positive = schemas.get(i);
            onePossibility.put(positive.object);
            for (int j = 0; j < schemas.size(); j++) {
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

    private void handleNotInMerge(JSONObject constraints, Map<String, Set<Object>> keyToValues) throws JSONSchemaException {
        if (keyToValues.containsKey("not")) {
            List<?> valueAfterOperation = (List<?>) Keys.applyOperation("not", keyToValues.get("not"));
            JSONArray notArray = new JSONArray(valueAfterOperation);
            if (constraints.has("anyOf")) {
                JSONObject alreadyAnyOf = new JSONObject();
                alreadyAnyOf.put("anyOf", constraints.getJSONArray("anyOf"));
                JSONObject fromNot = new JSONObject();
                fromNot.put("anyOf", notArray);
                JSONArray allOf = new JSONArray();
                allOf.put(alreadyAnyOf);
                allOf.put(fromNot);
                
                if (constraints.has("allOf")) {
                    constraints.append("allOf", alreadyAnyOf);
                    constraints.append("allOf", fromNot);
                }
                else {
                    constraints.put("allOf", allOf);
                }
                
                constraints.remove("anyOf");
            }
            else {
                constraints.put("anyOf", notArray);
            }
        }
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
            final String key = entry.getKey();
            if (!key.equals("not")) {
                Object valueAfterOperation = Keys.applyOperation(key, entry.getValue());
                constraints.put(key, valueAfterOperation);
            }
        }

        handleNotInMerge(constraints, keyToValues);
        return new JSONSchema(constraints, store, fullSchemaId);
    }

    public List<JSONSchema> getNot() throws JSONSchemaException {
        if (object.has("not")) {
            final JSONObject not = object.getJSONObject("not");
            JSONSchema actualSchema;
            if (not.has("$ref")) {
                actualSchema = handleRef(not.getString("$ref"));
            }
            else {
                actualSchema = new JSONSchema(not, store, fullSchemaId);
            }
            final List<JSONSchema> schemas = new ArrayList<>(actualSchema.object.length());

            for (final String key : actualSchema.object.keySet()) {
                final Object value = actualSchema.object.get(key);
                final JSONObject notValue = Keys.applyNot(key, Collections.singleton(value));
                schemas.add(new JSONSchema(notValue, store, fullSchemaId));
            }
            return schemas;
        }
        return Collections.singletonList(store.trueSchema());
    }

    private JSONSchema handleRef(String reference) throws JSONException, JSONSchemaException {
        if (reference.charAt(0) == '#') {
            // Recursive reference
            String[] decompositions = reference.split("/");
            JSONSchema targetSchema = store.get(fullSchemaId);
            for (int i = 1; i < decompositions.length; i++) {
                String key = decompositions[i];
                targetSchema = targetSchema.getSubSchema(key);
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
