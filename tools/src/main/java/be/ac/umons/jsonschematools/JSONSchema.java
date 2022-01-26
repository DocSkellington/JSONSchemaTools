package be.ac.umons.jsonschematools;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A wrapper around a JSON document storing a schema.
 * 
 * @author Gaëtan Staquet
 */
public final class JSONSchema {
    private final JSONObject schema;
    private final JSONObject properties;
    private final Set<Type> types = new TreeSet<>();
    private final JSONSchemaStore store;
    private final int fullSchemaId;
    private final Object constValue;
    private final Object forbiddenValue;

    JSONSchema(final JSONObject object, final JSONSchemaStore store, final int fullSchemaId)
            throws JSONSchemaException {
        this.schema = object;
        this.store = store;
        this.fullSchemaId = fullSchemaId;

        boolean atLeastOne = false;
        if (object.has("type")) {
            addTypes(object.get("type"));
            atLeastOne = true;
        }
        if (object.has("enum")) {
            types.add(Type.ENUM);
            atLeastOne = true;
        }
        if (object.has("const")) {
            constValue = object.get("const");
            atLeastOne = true;
            types.add(getConstType());
        }
        else {
            constValue = null;
        }
        
        if (!atLeastOne) {
            types.addAll(EnumSet.allOf(Type.class));
        }

        if (object.has("not")) {
            JSONObject not = object.getJSONObject("not");
            if (not.has("type")) {
                removeTypes(not.get("type"));
            }

            if (not.has("enum")) {
                types.add(Type.ENUM);
            }

            if (not.has("const")) {
                forbiddenValue = not.get("const");
                if (!atLeastOne) {
                    types.retainAll(Collections.singleton(getForbiddenType()));
                }
            }
            else {
                forbiddenValue = null;
            }
        }
        else {
            // By transformation, we will not always obtain a meaningful "not" directly. It can be hidden inside an "anyOf"
            if (object.has("anyOf") && object.getJSONArray("anyOf").length() == 1 && object.getJSONArray("anyOf").getJSONObject(0).has("not")) {
                JSONObject not = object.getJSONArray("anyOf").getJSONObject(0).getJSONObject("not");
                if (not.has("type")) {
                    removeTypes(not.get("type"));
                }

                if (not.has("enum")) {
                    types.add(Type.ENUM);
                }

                if (not.has("const")) {
                    forbiddenValue = not.get("const");
                    if (!atLeastOne) {
                        types.retainAll(Collections.singleton(getForbiddenType()));
                    }
                }
                else {
                    forbiddenValue = null;
                }
            }
            else {
                forbiddenValue = null;
            }
        }

        if (isObject()) {
            if (object.has("properties")) {
                this.properties = object.getJSONObject("properties");
            }
            else {
                this.properties = new ComparableJSONObject();
            }
            JSONObject additionalProperties = getAdditionalProperties();

            // TODO: is it allowed to have "additionalProperties" inside "additionalProperties"
            if (!JSONSchemaStore.isTrueDocument(additionalProperties) && !JSONSchemaStore.isFalseDocument(additionalProperties)) {
                if (additionalProperties.has("properties")) {
                    JSONObject prop = additionalProperties.getJSONObject("properties");
                    for (final String key : prop.keySet()) {
                        if (!this.properties.has(key)) {
                            this.properties.put(key, prop.get(key));
                        }
                    }
                }
            }
        } else {
            this.properties = null;
        }
    }

    public JSONObject getSchema() {
        return schema;
    }

    private Type getType(Object object) {
        if (object instanceof Integer) {
            return Type.INTEGER;
        }
        else if (object instanceof Number) {
            return Type.NUMBER;
        }
        else if (object instanceof Boolean) {
            return Type.BOOLEAN;
        }
        else if (object instanceof String) {
            return Type.STRING;
        }
        else if (object instanceof JSONArray) {
            return Type.ARRAY;
        }
        else if (object instanceof JSONObject) {
            return Type.OBJECT;
        }
        else {
            return Type.NULL;
        }
    }

    private Type getForbiddenType() {
        return getType(forbiddenValue);
    }

    private Type getConstType() {
        return getType(constValue);
    }

    private JSONObject getAdditionalProperties() throws JSONSchemaException {
        JSONObject schemaForAdditionalProperties;
        if (this.schema.has("additionalProperties")) {
            Object additionalProperties = schema.get("additionalProperties");
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
                    schemaForAdditionalProperties = handleRef(value.getString("$ref")).schema;
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

    private void addTypes(Object types) throws JSONSchemaException {
        if (types instanceof JSONArray) {
            JSONArray arrayType = (JSONArray)types;
            for (Object t : arrayType) {
                if (t.getClass() != String.class) {
                    throw new JSONSchemaException("Elements in an array for key \"type\" must be strings");
                }
                addType((String) t);
            }
        }
        else if (types instanceof String) {
            addType((String)types);
        }
        else {
            throw new JSONSchemaException("The value for the key \"type\" must be an array or a string");
        }
    }

    private void addType(String type) {
        types.add(Type.valueOf(type.toUpperCase()));
    }

    private void removeTypes(Object types) throws JSONSchemaException {
        if (types instanceof JSONArray) {
            JSONArray arrayType = (JSONArray)types;
            for (Object t : arrayType) {
                if (t.getClass() != String.class) {
                    throw new JSONSchemaException("Elements in an array for key \"type\" must be strings");
                }
                removeType((String) t);
            }
        }
        else if (types instanceof String) {
            removeType((String)types);
        }
        else {
            throw new JSONSchemaException("The value for the key \"type\" must be an array or a string");
        }
    }

    private void removeType(String type) {
        types.remove(Type.valueOf(type.toUpperCase()));
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

        final Set<String> allKeys = new TreeSet<>();
        final Set<String> seenPaths = new TreeSet<>(); // Store "#/properties/object/value", and so on
        final Queue<InQueue> queue = new LinkedList<>();
        queue.add(new InQueue("#", schema));
        seenPaths.add("#");

        while (!queue.isEmpty()) {
            final InQueue current = queue.poll();
            JSONObject document = (JSONObject) current.object;
            if (document.has("$ref")) {
                final String ref = document.getString("$ref");
                if (seenPaths.contains(ref)) {
                    continue;
                }
                document = handleRef(ref).schema;
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
                final Object object = document.get("items");
                final JSONArray items;
                if (object instanceof JSONArray) {
                    items = (JSONArray)object;
                }
                else {
                    items = new ComparableJSONArray();
                    items.put((JSONObject)object);
                }
                for (int i = 0 ; i < items.length() ; i++) {
                    final String path = current.path + "/items" + i;
                    if (!seenPaths.contains(path)) {
                        seenPaths.add(path);
                        queue.add(new InQueue(path, items.getJSONObject(i)));
                    }
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
        Set<Object> forbiddenValues = new TreeSet<>();
        if (schema.has("anyOf")) {
            JSONArray anyOf = schema.getJSONArray("anyOf");
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

        if (forbiddenValue != null) {
            forbiddenValues.add(forbiddenValue);
        }
        return forbiddenValues;
    }

    public Object getConstValue() {
        return constValue;
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
        return schema.has(key);
    }

    private boolean needsFurtherUnfoldingNot(JSONObject not) {
        if (not.has("enum") && not.has("const") && not.length() == 2) {
            return false;
        }
        else if (not.length() == 1 && not.has("const")) {
            return false;
        }
        else if (not.length() == 1 && not.has("enum")) {
            return false;
        }
        return true;
    }

    private boolean needsFurtherUnfolding(JSONArray array) {
        if (array.length() != 1) {
            return true;
        }
        try {
            JSONObject subSchema = array.getJSONObject(0);
            if (subSchema.has("not")) {
                JSONObject not = subSchema.getJSONObject("not");
                if (needsFurtherUnfoldingNot(not)) {
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
        return false;
    }

    public boolean needsFurtherUnfolding() {
        if (schema.has("not")) {
            JSONObject not = schema.getJSONObject("not");
            if (needsFurtherUnfoldingNot(not)) {
                return true;
            }
        }

        if (schema.has("allOf") && needsFurtherUnfolding(schema.getJSONArray("allOf"))) {
            return true;
        }
        if (schema.has("anyOf") && needsFurtherUnfolding(schema.getJSONArray("anyOf"))) {
            return true;
        }
        if (schema.has("oneOf") && needsFurtherUnfolding(schema.getJSONArray("oneOf"))) {
            return true;
        }

        return false;
    }

    public Set<String> getRequiredPropertiesKeys() throws JSONSchemaException {
        if (!isObject()) {
            throw new JSONSchemaException("Required properties are only defined for objects");
        }

        if (schema.has("required")) {
            Set<String> keys = new TreeSet<>();
            JSONArray required = schema.getJSONArray("required");
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

        if (schema.has("required")) {
            Map<String, JSONSchema> requiredProperties = new TreeMap<>();
            JSONArray required = schema.getJSONArray("required");
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

        Set<String> requiredKeys = new TreeSet<>();
        if (schema.has("required")) {
            for (Object value : schema.getJSONArray("required")) {
                if (value.getClass() != String.class) {
                    throw new JSONSchemaException("Values in the \"required\" field must be strings");
                }
                String key = (String) value;
                requiredKeys.add(key);
            }
        }

        Map<String, JSONSchema> nonRequired = new TreeMap<>();
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
                    constraints.put(key, new TreeSet<>());
                }
                constraints.get(key).add(schema.get(key));
            }
        }
    }

    private JSONSchema transformConstraintsInSchema(final Map<String, Set<Object>> keyToValues)
            throws JSONSchemaException {
        // We will handle "not" afterwards. This is because we potentially need to merge
        // the constraints in "not" with the regular constraints
        final JSONObject constraints = new ComparableJSONObject();
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
        JSONObject newSchema = new ComparableJSONObject();
        for (String key : schema.keySet()) {
            if (key.equals("allOf") || key.equals("anyOf") || key.equals("oneOf") || key.equals("not")) {
                continue;
            }
            newSchema.put(key, schema.get(key));
        }
        return new JSONSchema(newSchema, store, fullSchemaId);
    }

    public JSONSchema getAllOf() throws JSONSchemaException {
        if (!schema.has("allOf")) {
            return store.trueSchema();
        }
        final JSONArray allOf = schema.getJSONArray("allOf");
        final Map<String, Set<Object>> keyToValues = new TreeMap<>();
        for (int i = 0; i < allOf.length(); i++) {
            JSONObject schema = allOf.getJSONObject(i);
            if (schema.has("$ref")) {
                schema = handleRef(schema.getString("$ref")).schema;
            }
            if (JSONSchemaStore.isFalseDocument(schema)) {
                return store.falseSchema();
            }
            addConstraintToSet(keyToValues, schema, Keys.getKeys());
        }
        return transformConstraintsInSchema(keyToValues);
    }

    public List<JSONSchema> getAnyOf() throws JSONSchemaException {
        if (!schema.has("anyOf")) {
            return Collections.singletonList(store.trueSchema());
        }
        final JSONArray anyOf = schema.getJSONArray("anyOf");
        final List<JSONSchema> schemas = new ArrayList<>(anyOf.length());
        for (int i = 0; i < anyOf.length(); i++) {
            final JSONObject subSchema = anyOf.getJSONObject(i);
            final Map<String, Set<Object>> keyToValues = new TreeMap<>();
            addConstraintToSet(keyToValues, subSchema, Keys.getKeys());
            schemas.add(transformConstraintsInSchema(keyToValues));
        }
        return schemas;
    }

    public List<JSONSchema> getOneOf() throws JSONSchemaException {
        if (!schema.has("oneOf")) {
            return Collections.singletonList(store.trueSchema());
        }
        final JSONArray oneOf = schema.getJSONArray("oneOf");
        final List<JSONSchema> schemas = new ArrayList<>(oneOf.length());
        for (int i = 0; i < oneOf.length(); i++) {
            final JSONObject subSchema = oneOf.getJSONObject(i);
            final Map<String, Set<Object>> keyToValues = new TreeMap<>();
            addConstraintToSet(keyToValues, subSchema, Keys.getKeys());
            schemas.add(transformConstraintsInSchema(keyToValues));
        }

        final List<JSONSchema> combinations = new ArrayList<>(schemas.size());
        for (int i = 0; i < schemas.size(); i++) {
            final JSONArray onePossibility = new ComparableJSONArray(schemas.size());
            final JSONSchema positive = schemas.get(i);
            onePossibility.put(positive.schema);
            for (int j = 0; j < schemas.size(); j++) {
                if (i == j) {
                    continue;
                }
                final JSONObject notSchema = new ComparableJSONObject();
                notSchema.put("not", schemas.get(j).schema);
                onePossibility.put(notSchema);
            }
            final JSONObject allOf = new ComparableJSONObject();
            allOf.put("allOf", onePossibility);
            final JSONSchema schemaForPossibility = new JSONSchema(allOf, store, fullSchemaId);
            combinations.add(schemaForPossibility);
        }
        return combinations;
    }

    private void handleNotInMerge(JSONObject constraints, Map<String, Set<Object>> keyToValues) throws JSONSchemaException {
        if (keyToValues.containsKey("not")) {
            List<?> valueAfterOperation = (List<?>) Keys.applyOperation("not", keyToValues.get("not"));
            JSONArray notArray = new ComparableJSONArray(valueAfterOperation);
            if (constraints.has("anyOf")) {
                JSONObject alreadyAnyOf = new ComparableJSONObject();
                alreadyAnyOf.put("anyOf", constraints.getJSONArray("anyOf"));
                JSONObject fromNot = new ComparableJSONObject();
                fromNot.put("anyOf", notArray);
                JSONArray allOf = new ComparableJSONArray();
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
        if (other == null || other.schema.isEmpty()) {
            return this;
        }
        Map<String, Set<Object>> keyToValues = new TreeMap<>();
        for (String key : schema.keySet()) {
            Object value = schema.get(key);
            if (!keyToValues.containsKey(key)) {
                keyToValues.put(key, new TreeSet<>());
            }
            keyToValues.get(key).add(value);
        }
        for (String key : other.schema.keySet()) {
            Object value = other.schema.get(key);
            if (!keyToValues.containsKey(key)) {
                keyToValues.put(key, new TreeSet<>());
            }
            keyToValues.get(key).add(value);
        }

        JSONObject constraints = new ComparableJSONObject();
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

    public JSONSchema getRawNot() throws JSONSchemaException {
        if (schema.has("not")) {
            return getSubSchema("not");
        }
        else {
            return store.falseSchema();
        }
    }

    public List<JSONSchema> getNot() throws JSONSchemaException {
        if (schema.has("not")) {
            final JSONObject not = schema.getJSONObject("not");
            JSONSchema actualSchema;
            if (not.has("$ref")) {
                actualSchema = handleRef(not.getString("$ref"));
            }
            else {
                actualSchema = new JSONSchema(not, store, fullSchemaId);
            }
            final List<JSONSchema> schemas = new ArrayList<>(actualSchema.schema.length());

            for (final String key : actualSchema.schema.keySet()) {
                final Object value = actualSchema.schema.get(key);
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
        return getSubSchema(key, schema);
    }

    private JSONSchema getSubSchema(String key, JSONObject object) throws JSONException, JSONSchemaException {
        JSONObject subObject = new ComparableJSONObject(object.getJSONObject(key));
        if (subObject.has("$ref")) {
            return handleRef(subObject.getString("$ref"));
        } else {
            return new JSONSchema(subObject, store, fullSchemaId);
        }
    }

    public List<JSONSchema> getItemsArray() throws JSONException, JSONSchemaException {
        if (!schema.has("items")) {
            return Collections.singletonList(store.trueSchema());
        }
        List<JSONSchema> list = new ArrayList<>();
        Object items = schema.get("items");
        JSONArray array;
        if (items instanceof JSONArray) {
            array = (JSONArray)items;
        }
        else if (items instanceof JSONObject) {
            array = new ComparableJSONArray();
            array.put((JSONObject)items);
        }
        else {
            throw new JSONSchemaException("Invalid type for \"items\" in schema " + this);
        }

        for (int i = 0 ; i < array.length() ; i++) {
            JSONObject subObject = array.getJSONObject(i);
            JSONSchema subSchema;
            if (subObject.has("$ref")) {
                subSchema = handleRef(subObject.getString("$ref"));
            } else {
                subSchema = new JSONSchema(subObject, store, fullSchemaId);
            }
            list.add(subSchema);
        }
        if (list.size() == 0) {
            return Collections.singletonList(store.trueSchema());
        }
        return list;
    }

    public int getInt(String key) {
        return schema.getInt(key);
    }

    public int getIntOr(String key, int defaultValue) {
        return schema.optInt(key, defaultValue);
    }

    public double getDouble(String key) {
        return schema.getDouble(key);
    }

    public double getDoubleOr(String key, double defaultValue) {
        return schema.optDouble(key, defaultValue);
    }

    public Number getNumber(String key) {
        return schema.getNumber(key);
    }

    public Number getNumberOr(String key, Number defaultValue) {
        return schema.optNumber(key, defaultValue);
    }

    public String getString(String key) {
        return schema.getString(key);
    }

    public String getStringOr(String key, String defaultValue) {
        return schema.optString(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return schema.getBoolean(key);
    }

    public boolean getBooleanOr(String key, boolean defaultValue) {
        return schema.optBoolean(key, defaultValue);
    }

    public int getSchemaId() {
        return fullSchemaId;
    }

    public JSONSchemaStore getStore() {
        return store;
    }

    @Override
    public String toString() {
        return schema.toString(2);
    }

}
