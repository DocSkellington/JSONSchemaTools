/*
 * JSONSchemaTools - Generators and validator for JSON schema, with abstract values
 *
 * Copyright 2022 University of Mons, University of Antwerp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.ac.umons.jsonschematools;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A JSON schema stores the constraints a JSON document must satisfy to be valid.
 * 
 * This class implements tools to manipulate a JSON schema:
 * <ul>
 * <li>Get the types allowed for the current schema. See {@link Type}.</li>
 * <li>Get the value associated to a key.</li>
 * <li>Get a sub-schema.</li>
 * <li>Merge multiple sub-schemas into a single schema, with all the
 * constraints. See the non-API documentation for more information on merging
 * the constraints given by multiples instances of a key.</li>
 * <li>Automatically follow <code>$ref</code> when encountered.</li>
 * </ul>
 * 
 * @author Gaëtan Staquet
 */
public final class JSONSchema {
    private final JSONObject schema;
    private final JSONObject properties;
    private final Set<Type> types = new LinkedHashSet<>();
    private final JSONSchemaStore store;
    private final int fullSchemaId;
    private final Object constValue;
    private final Object forbiddenValue;
    private final JSONObject additionalProperties;

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
            types.clear();
            types.add(getConstType());
        } else {
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
                // We do not add or remove possible types, in this case.
                // Indeed, if the type of the forbidden value is not even considered, we do not
                // want to consider it.
            } else {
                forbiddenValue = null;
            }
        } else {
            // By transformation, we will not always obtain a meaningful "not" directly. It
            // can be hidden inside an "anyOf"
            if (object.has("anyOf") && object.getJSONArray("anyOf").length() == 1
                    && object.getJSONArray("anyOf").getJSONObject(0).has("not")) {
                JSONObject not = object.getJSONArray("anyOf").getJSONObject(0).getJSONObject("not");
                if (not.has("type")) {
                    removeTypes(not.get("type"));
                }

                if (not.has("enum")) {
                    types.add(Type.ENUM);
                }

                if (not.has("const")) {
                    forbiddenValue = not.get("const");
                } else {
                    forbiddenValue = null;
                }
            } else {
                forbiddenValue = null;
            }
        }

        if (isObject()) {
            if (object.has("properties")) {
                this.properties = object.getJSONObject("properties");
            } else {
                this.properties = new HashableJSONObject();
            }
            this.additionalProperties = getSchemaForAdditionalProperties();
            if (!JSONSchemaStore.isFalseDocument(additionalProperties)) {
                if (!JSONSchemaStore.isTrueDocument(additionalProperties)
                        || !store.shouldIgnoreTrueAdditionalProperties()) {
                    this.properties.put(AbstractConstants.stringConstant, additionalProperties);
                }
            }

            final JSONObject patternProperties = getPatternProperties();
            if (!JSONSchemaStore.isTrueDocument(patternProperties)
                    && !JSONSchemaStore.isFalseDocument(patternProperties)) {
                for (final String key : patternProperties.keySet()) {
                    if (!this.properties.has(key)) {
                        this.properties.put(key, patternProperties.get(key));
                    }
                }
            }
        } else {
            this.properties = null;
            this.additionalProperties = JSONSchemaStore.falseDocument();
        }
    }

    JSONObject getSchema() {
        return schema;
    }

    private Type getConstType() {
        if (constValue instanceof Integer) {
            return Type.INTEGER;
        } else if (constValue instanceof Number) {
            return Type.NUMBER;
        } else if (constValue instanceof Boolean) {
            return Type.BOOLEAN;
        } else if (constValue instanceof String) {
            return Type.STRING;
        } else if (constValue instanceof JSONArray) {
            return Type.ARRAY;
        } else if (constValue instanceof JSONObject) {
            return Type.OBJECT;
        } else {
            return Type.NULL;
        }
    }

    private JSONObject getSchemaForAdditionalProperties() throws JSONSchemaException {
        JSONObject schemaForAdditionalProperties;
        if (this.schema.has("additionalProperties")) {
            Object additionalProperties = schema.get("additionalProperties");
            if (additionalProperties instanceof Boolean) {
                boolean value = (Boolean) additionalProperties;
                if (value) {
                    schemaForAdditionalProperties = JSONSchemaStore.trueDocument();
                } else {
                    schemaForAdditionalProperties = JSONSchemaStore.falseDocument();
                }
            } else if (additionalProperties instanceof JSONObject) {
                JSONObject value = (JSONObject) additionalProperties;
                if (value.has("$ref")) {
                    schemaForAdditionalProperties = handleRef(value.getString("$ref")).schema;
                } else {
                    schemaForAdditionalProperties = value;
                }
            } else {
                throw new JSONSchemaException(
                        "Invalid schema: the value for \"additionalProperties\" must be a valid JSON Schema. Received: "
                                + additionalProperties);
            }
        } else {
            schemaForAdditionalProperties = JSONSchemaStore.trueDocument();
        }
        return schemaForAdditionalProperties;
    }

    private JSONObject getPatternProperties() throws JSONException, JSONSchemaException {
        if (this.schema.has("patternProperties")) {
            final JSONObject patternProperties = this.schema.getJSONObject("patternProperties");
            if (patternProperties.has("$ref")) {
                return handleRef(patternProperties.getString("$ref")).schema;
            } else {
                return patternProperties;
            }
        } else {
            return JSONSchemaStore.trueDocument();
        }
    }

    private void addTypes(Object types) throws JSONSchemaException {
        if (types instanceof JSONArray) {
            JSONArray arrayType = (JSONArray) types;
            for (Object t : arrayType) {
                if (t.getClass() != String.class) {
                    throw new JSONSchemaException("Elements in an array for key \"type\" must be strings");
                }
                addType((String) t);
            }
        } else if (types instanceof String) {
            addType((String) types);
        } else {
            throw new JSONSchemaException("The value for the key \"type\" must be an array or a string");
        }
    }

    private void addType(String type) {
        types.add(Type.valueOf(type.toUpperCase()));
    }

    private void removeTypes(Object types) throws JSONSchemaException {
        if (types instanceof JSONArray) {
            JSONArray arrayType = (JSONArray) types;
            for (Object t : arrayType) {
                if (t.getClass() != String.class) {
                    throw new JSONSchemaException("Elements in an array for key \"type\" must be strings");
                }
                removeType((String) t);
            }
        } else if (types instanceof String) {
            removeType((String) types);
        } else {
            throw new JSONSchemaException("The value for the key \"type\" must be an array or a string");
        }
    }

    private void removeType(String type) {
        types.remove(Type.valueOf(type.toUpperCase()));
    }

    private int depth(JSONObject schema) throws JSONSchemaException {
        if (JSONSchemaStore.isTrueDocument(schema)) {
            return 0;
        }
        if (schema.has("$ref")) {
            JSONSchema asSchema = new JSONSchema(schema, store, fullSchemaId);
            return asSchema.handleRef(schema.getString("$ref")).depth();
        }
        int depth = 0;
        for (String key : schema.keySet()) {
            if (key.equals("properties")) {
                JSONObject properties = schema.getJSONObject("properties");
                depth = Math.max(depth, depth(properties) + 1);
            }
            else if (key.equals("items")) {
                Object items = schema.get("items");
                if (items instanceof JSONObject) {
                    depth = Math.max(depth, depth((JSONObject) items) + 1);
                }
                else if (items instanceof JSONArray) {
                    for (Object item : (JSONArray) items) {
                        depth = Math.max(depth, depth((JSONObject) item) + 1);
                    }
                }
            }
            else if (key.equals("allOf")) {
                JSONArray allOf = schema.getJSONArray("allOf");
                for (Object all : (JSONArray) allOf) {
                    depth = Math.max(depth, depth((JSONObject) all));
                }
            }
            else if (key.equals("anyOf")) {
                JSONArray anyOf = schema.getJSONArray("anyOf");
                for (Object all : (JSONArray) anyOf) {
                    depth = Math.max(depth, depth((JSONObject) all));
                }
            }
            else if (key.equals("oneOf")) {
                JSONArray oneOf = schema.getJSONArray("oneOf");
                for (Object all : (JSONArray) oneOf) {
                    depth = Math.max(depth, depth((JSONObject) all));
                }
            }
            else if (key.equals("not")) {
                JSONObject not = schema.getJSONObject("not");
                depth = Math.max(depth, depth(not));
            }
            else {
                Object value = schema.get(key);
                if (value instanceof JSONObject) {
                    depth = Math.max(depth, depth((JSONObject)value));
                }
            }
        }

        return depth;
    }

    /**
     * Computes the depth of the schema, i.e., the maximal number of nested objects.
     * 
     * If the schema is recursive, the depth is infinite and this function does not return.
     * @return The depth of the schema
     * @throws JSONSchemaException
     */
    public int depth() throws JSONSchemaException {
        if (JSONSchemaStore.isTrueSchema(this)) {
            return 0;
        }
        return depth(schema);
    }

    /**
     * Returns the schema describing the additional properties of this schema.
     * 
     * If this schema does not describe an object, the returned schema is always false.
     * @return The schema describing the additional properties
     * @throws JSONSchemaException
     */
    public JSONSchema getAdditionalProperties() throws JSONSchemaException {
        return new JSONSchema(additionalProperties, store, fullSchemaId);
    }

    /**
     * Retrieves all the keys present in this schema.
     * 
     * If the schema contains a sub-schema (due to <code>properties</code>, for
     * instance), the answer will contain the keys from the sub-schema.
     * 
     * @return A set containing all the keys defined in this schema.
     * @throws JSONSchemaException
     */
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

        final Set<String> allKeys = new LinkedHashSet<>();
        final Set<String> seenPaths = new LinkedHashSet<>(); // Store "#/properties/object/value", and so on
        final Queue<InQueue> queue = new LinkedList<>();
        queue.add(new InQueue("#", schema));
        seenPaths.add("#");

        final BiConsumer<String, JSONObject> addObjectToQueueIfNotAlreadySeen = (path, prop) -> {
            if (!seenPaths.contains(path)) {
                seenPaths.add(path);
                queue.add(new InQueue(path, prop));
            }
        };
        final BiConsumer<String, JSONObject> addAllPropertiesInQueue = (path, properties) -> {
            for (final String key : properties.keySet()) {
                allKeys.add(key);
                final JSONObject prop = properties.getJSONObject(key);
                if (prop.has("$ref")) {
                    queue.add(new InQueue(path + "/" + key, prop));
                } else {
                    addObjectToQueueIfNotAlreadySeen.accept(path + "/" + key, prop);
                }
            }
        };

        while (!queue.isEmpty()) {
            final InQueue current = queue.poll();
            JSONObject document = current.object;
            if (document.has("$ref")) {
                final String ref = document.getString("$ref");
                if (seenPaths.contains(ref)) {
                    continue;
                }
                document = handleRef(ref).schema;
                seenPaths.add(ref);
            }
            if (document.has("properties")) {
                addAllPropertiesInQueue.accept(current.path + "/properties", document.getJSONObject("properties"));
            }
            if (document.has("additionalProperties")) {
                final Object additionalProperties = document.get("additionalProperties");
                if (additionalProperties instanceof JSONObject) {
                    allKeys.add(AbstractConstants.stringConstant);
                    queue.add(new InQueue(current.path + "/additionalProperties", (JSONObject) additionalProperties));
                } else if (additionalProperties instanceof Boolean && (boolean) additionalProperties) {
                    allKeys.add(AbstractConstants.stringConstant);
                }
            }
            if (document.has("patternProperties")) {
                addAllPropertiesInQueue.accept(current.path + "/patternProperties",
                        document.getJSONObject("patternProperties"));
            }
            if (document.has("items")) {
                final Object object = document.get("items");
                final JSONArray items;
                if (object instanceof JSONArray) {
                    items = (JSONArray) object;
                } else {
                    items = new HashableJSONArray();
                    items.put((JSONObject) object);
                }
                for (int i = 0; i < items.length(); i++) {
                    addObjectToQueueIfNotAlreadySeen.accept(current.path + "/items" + i, items.getJSONObject(i));
                }
            }
            for (final String key : Arrays.asList("allOf", "anyOf", "oneOf")) {
                if (document.has(key)) {
                    final JSONArray value = document.getJSONArray(key);
                    final String path = current.path + "/" + key;
                    for (final Object object : value) {
                        final JSONObject subSchema = (JSONObject) object;
                        queue.add(new InQueue(path, subSchema));
                    }
                }
            }
            if (document.has("not")) {
                final JSONObject not = document.getJSONObject("not");
                queue.add(new InQueue(current.path + "/not", not));
            }
        }

        return allKeys;
    }

    /**
     * Gets all the types that are allowed in this schema.
     * 
     * @return A set containing all allowed types.
     */
    public List<Type> getAllowedTypes() {
        return new ArrayList<>(types);
    }

    /**
     * Gets all the values that are forbidden by this schema.
     * 
     * For instance, the constraint <code>"not": {"const": 5}</code> imposes that 5
     * is a forbidden value.
     * 
     * @return A set with the forbidden values.
     */
    public Set<Object> getForbiddenValues() {
        Set<Object> forbiddenValues = new LinkedHashSet<>();
        if (schema.has("anyOf")) {
            JSONArray anyOf = schema.getJSONArray("anyOf");
            for (int i = 0; i < anyOf.length(); i++) {
                JSONObject subSchema = anyOf.getJSONObject(i);
                if (subSchema.has("not")) {
                    JSONObject not = subSchema.getJSONObject("not");
                    if (not.has("enum")) {
                        JSONArray enumArray = not.getJSONArray("enum");
                        for (int j = 0; j < enumArray.length(); j++) {
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

    /**
     * Gets all the values of the given type that are forbidden by this schema.
     * 
     * This is equivalent to calling {@link getForbiddenValues} and only keeping the
     * values with the correct type.
     * 
     * @param <T>  The type
     * @param type A class instance for T
     * @return A set with the filtered forbidden values.
     */
    public <T> Set<T> getForbiddenValuesFilteredByType(Class<T> type) {
        return getForbiddenValues().stream()
                .filter(v -> type.isInstance(v))
                .map(v -> type.cast(v))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the const value defined in this schema.
     * 
     * If the const value is not defined, returns null.
     * @return The const value, or null
     */
    public Object getConstValue() {
        return constValue;
    }

    /**
     * Returns the const value defined in this schema, if the value's type matches the provided type.
     * 
     * If the const value is not defined or is not an instance of the provided type, returns null.
     * @param <T> The required type
     * @param type The required class
     * @return The const value, or null
     */
    public <T> T getConstValueIfType(Class<T> type) {
        if (type.isInstance(constValue)) {
            return type.cast(constValue);
        }
        return null;
    }

    /**
     * Returns true if this schema allows the type ENUM.
     * 
     * @return True if this schema allows the type ENUM.
     */
    public boolean isEnum() {
        return getAllowedTypes().contains(Type.ENUM);
    }

    /**
     * Returns true if this schema allows the type OBJECT.
     * 
     * @return True if this schema allows the type OBJECT.
     */
    public boolean isObject() {
        return getAllowedTypes().contains(Type.OBJECT);
    }

    /**
     * Returns true if this schema allows the type ARRAY.
     * 
     * @return True if this schema allows the type ARRAY.
     */
    public boolean isArray() {
        return getAllowedTypes().contains(Type.ARRAY);
    }

    /**
     * Returns true if this schema allows the type INTEGER.
     * 
     * @return True if this schema allows the type INTEGER.
     */
    public boolean isInteger() {
        return getAllowedTypes().contains(Type.INTEGER);
    }

    /**
     * Returns true if this schema allows the type NUMBER.
     * 
     * @return True if this schema allows the type NUMBER.
     */
    public boolean isNumber() {
        return getAllowedTypes().contains(Type.NUMBER);
    }

    /**
     * Returns true if this schema allows the type BOOLEAN.
     * 
     * @return True if this schema allows the type BOOLEAN.
     */
    public boolean isBoolean() {
        return getAllowedTypes().contains(Type.BOOLEAN);
    }

    /**
     * Returns true if this schema allows the type STRING.
     * 
     * @return True if this schema allows the type STRING.
     */
    public boolean isString() {
        return getAllowedTypes().contains(Type.STRING);
    }

    /**
     * Returns true if this schema allows the type NULL.
     * 
     * @return True if this schema allows the type NULL.
     */
    public boolean isNull() {
        return getAllowedTypes().contains(Type.NULL);
    }

    private boolean needsFurtherUnfoldingNot(JSONObject not) {
        if (not.has("enum") && not.has("const") && not.length() == 2) {
            return false;
        } else if (not.length() == 1 && not.has("const")) {
            return false;
        } else if (not.length() == 1 && not.has("enum")) {
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
            } else {
                return true;
            }
        } catch (JSONException e) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether there are still some constraints in some sub-schemas that must
     * be merged with the whole schema.
     * 
     * When merging constraints from sub-schema defined in <code>not</code>,
     * <code>allOf</code>, <code>anyOf</code>, or <code>oneOf</code>, it may happen
     * that we end up with these keys again. In this case, we have to repeat the
     * merging process to remove these keys.
     * 
     * Note that the function may return <code>false</code> even if one of the above
     * keys is still present, if the constraints inside the sub-schema can not be
     * simplified by merging. These occurences must be handled explicitly by the
     * generator or validator.
     * 
     * @return <code>true</code> if there are constraints in sub-schemas that can be
     *         merged with this schema.
     */
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

    /**
     * Gets the keys that are required in this schema's properties.
     * 
     * @return A set with the keys that are required
     * @throws JSONSchemaException If this schema does not allow the type OBJECT.
     */
    public Set<String> getRequiredPropertiesKeys() throws JSONSchemaException {
        if (!isObject()) {
            return Collections.emptySet();
        }

        if (schema.has("required")) {
            Set<String> keys = new LinkedHashSet<>();
            JSONArray required = schema.getJSONArray("required");
            for (int i = 0; i < required.length(); i++) {
                keys.add(required.getString(i));
            }
            return keys;
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Gets the pairs key-value that are required by this schema.
     * 
     * @return A map with the required keys, and their corresponding sub-schema.
     * @throws JSONSchemaException If this schema does not allow the type OBJECT.
     */
    public Map<String, JSONSchema> getRequiredProperties() throws JSONSchemaException {
        if (!isObject()) {
            return Collections.emptyMap();
        }

        if (schema.has("required")) {
            Map<String, JSONSchema> requiredProperties = new LinkedHashMap<>();
            JSONArray required = schema.getJSONArray("required");
            for (int i = 0; i < required.length(); i++) {
                String key = required.getString(i);
                requiredProperties.put(key, getSubSchemaProperties(key));
            }
            return requiredProperties;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Gets the pairs key-value that are not required by this schema.
     * 
     * The map contains the keys for <code>additionalProperties</code> and
     * <code>patternProperties</code>, if they are allowed.
     * 
     * @return A map with the non-required keys, and their corresponding sub-schema.
     * @throws JSONSchemaException If this schema does not allow the type OBJECT.
     */
    public Map<String, JSONSchema> getNonRequiredProperties() throws JSONSchemaException {
        if (!isObject()) {
            return Collections.emptyMap();
        }

        Set<String> requiredKeys = getRequiredPropertiesKeys();
        Map<String, JSONSchema> nonRequired = new LinkedHashMap<>();
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
                    constraints.put(key, new LinkedHashSet<>());
                }
                constraints.get(key).add(schema.get(key));
            }
        }
    }

    private JSONSchema transformConstraintsInSchema(final Map<String, Set<Object>> keyToValues)
            throws JSONSchemaException {
        // We will handle "not" afterwards. This is because we potentially need to merge
        // the constraints in "not" with the regular constraints
        final JSONObject constraints = new HashableJSONObject();
        for (final Map.Entry<String, Set<Object>> entry : keyToValues.entrySet()) {
            final String key = entry.getKey();
            if (!key.equals("not")) {
                Object valueAfterOperation = MergeKeys.applyOperation(key, entry.getValue());
                constraints.put(key, valueAfterOperation);
            }
        }

        handleNotInMerge(constraints, keyToValues);
        return new JSONSchema(constraints, store, fullSchemaId);
    }

    /**
     * Returns a copy of this schema without the sub-schemas for <code>allOf</code>,
     * <code>anyOf</code>, <code>oneOf</code>, and <code>not</code>.
     * 
     * @return A partial copy of this schema
     * @throws JSONSchemaException If it is not possible to construct a copy of this
     *                             schema.
     */
    public JSONSchema dropAllOfAnyOfOneOfAndNot() throws JSONSchemaException {
        JSONObject newSchema = new HashableJSONObject();
        for (String key : schema.keySet()) {
            if (key.equals("allOf") || key.equals("anyOf") || key.equals("oneOf") || key.equals("not")) {
                continue;
            }
            newSchema.put(key, schema.get(key));
        }
        return new JSONSchema(newSchema, store, fullSchemaId);
    }

    /**
     * Gets a single schema obtained by merging together all the sub-schemas in the
     * <code>allOf</code> array.
     * 
     * If there is no <code>allOf</code> key, the true schema is returned.
     * 
     * @return A single schema containing all the constraints given in
     *         <code>allOf</code>.
     * @throws JSONSchemaException
     */
    public JSONSchema getAllOf() throws JSONSchemaException {
        if (!schema.has("allOf")) {
            return store.trueSchema();
        }
        final JSONArray allOf = schema.getJSONArray("allOf");
        final Map<String, Set<Object>> keyToValues = new LinkedHashMap<>();
        for (int i = 0; i < allOf.length(); i++) {
            JSONObject schema = allOf.getJSONObject(i);
            if (schema.has("$ref")) {
                schema = handleRef(schema.getString("$ref")).schema;
            }
            if (JSONSchemaStore.isFalseDocument(schema)) {
                return store.falseSchema();
            }
            addConstraintToSet(keyToValues, schema, MergeKeys.getKeys());
        }
        return transformConstraintsInSchema(keyToValues);
    }

    /**
     * Gets a list containing one schema by element in the <code>anyOf</code> array.
     * 
     * If there is no <code>anyOf</code> key, a singleton list containing the true
     * schema is returned.
     * 
     * @return A list of schemas, one by element in the <code>allOf</code> array.
     * @throws JSONSchemaException
     */
    public List<JSONSchema> getAnyOf() throws JSONSchemaException {
        if (!schema.has("anyOf")) {
            return Collections.singletonList(store.trueSchema());
        }
        final JSONArray anyOf = schema.getJSONArray("anyOf");
        final List<JSONSchema> schemas = new ArrayList<>(anyOf.length());
        for (int i = 0; i < anyOf.length(); i++) {
            final JSONObject subSchema = anyOf.getJSONObject(i);
            schemas.add(new JSONSchema(subSchema, store, fullSchemaId));
        }
        return schemas;
    }

    /**
     * Gets a list containing one possible combination of the elements in the
     * <code>oneOf</code> array, by applying the XOR operation.
     * 
     * If <code>oneOf</code> contains the elements <code>A, B, C</code>, then this
     * function produces three schemas:
     * <ul>
     * <li><code>{A, "not": B, "not": C}</code></li>
     * <li><code>{"not": A, B, "not": C}</code></li>
     * <li><code>{"not": A, "not": B, C}</code></li>
     * </ul>
     * 
     * That is, the function applies the definition of XOR on the elements.
     * 
     * If there is no <code>oneOf</code> key, returns a singleton list containing
     * the true schema.
     * 
     * @return A list of all possible combinations.
     * @throws JSONSchemaException
     */
    public List<JSONSchema> getOneOf() throws JSONSchemaException {
        if (!schema.has("oneOf")) {
            return Collections.singletonList(store.trueSchema());
        }
        final JSONArray oneOf = schema.getJSONArray("oneOf");
        final List<JSONSchema> schemas = new ArrayList<>(oneOf.length());
        for (int i = 0; i < oneOf.length(); i++) {
            final JSONObject subSchema = oneOf.getJSONObject(i);
            schemas.add(new JSONSchema(subSchema, store, fullSchemaId));
        }

        final List<JSONSchema> combinations = new ArrayList<>(schemas.size());
        for (int i = 0; i < schemas.size(); i++) {
            final JSONArray onePossibility = new HashableJSONArray(schemas.size());
            final JSONSchema positive = schemas.get(i);
            onePossibility.put(positive.schema);
            for (int j = 0; j < schemas.size(); j++) {
                if (i == j) {
                    continue;
                }
                final JSONObject notSchema = new HashableJSONObject();
                notSchema.put("not", schemas.get(j).schema);
                onePossibility.put(notSchema);
            }
            final JSONObject allOf = new HashableJSONObject();
            allOf.put("allOf", onePossibility);
            final JSONSchema schemaForPossibility = new JSONSchema(allOf, store, fullSchemaId);
            combinations.add(schemaForPossibility);
        }
        return combinations;
    }

    private void handleNotInMerge(JSONObject constraints, Map<String, Set<Object>> keyToValues)
            throws JSONSchemaException {
        if (keyToValues.containsKey("not")) {
            List<?> valueAfterOperation = (List<?>) MergeKeys.applyOperation("not", keyToValues.get("not"));
            JSONArray notArray = new HashableJSONArray(valueAfterOperation);
            if (constraints.has("anyOf")) {
                JSONObject alreadyAnyOf = new HashableJSONObject();
                alreadyAnyOf.put("anyOf", constraints.getJSONArray("anyOf"));
                JSONObject fromNot = new HashableJSONObject();
                fromNot.put("anyOf", notArray);
                JSONArray allOf = new HashableJSONArray();
                allOf.put(alreadyAnyOf);
                allOf.put(fromNot);

                if (constraints.has("allOf")) {
                    constraints.append("allOf", alreadyAnyOf);
                    constraints.append("allOf", fromNot);
                } else {
                    constraints.put("allOf", allOf);
                }

                constraints.remove("anyOf");
            } else {
                constraints.put("anyOf", notArray);
            }
        }
    }

    /**
     * Merge this schema with an other schema.
     * 
     * The keys of the final schema is the union of the keys from both schemas.
     * Keys that are defined in both schemas are merged (see the non-API
     * documentation to more information).
     * 
     * @param other The other schema
     * @return A single schema obtained by merging the two schemas
     * @throws JSONSchemaException If it is not possible to merge the schemas.
     */
    public JSONSchema merge(JSONSchema other) throws JSONSchemaException {
        if (other == null || other.schema.isEmpty()) {
            return this;
        }
        Map<String, Set<Object>> keyToValues = new LinkedHashMap<>();
        for (String key : schema.keySet()) {
            Object value = schema.get(key);
            if (!keyToValues.containsKey(key)) {
                keyToValues.put(key, new LinkedHashSet<>());
            }
            keyToValues.get(key).add(value);
        }
        for (String key : other.schema.keySet()) {
            Object value = other.schema.get(key);
            if (!keyToValues.containsKey(key)) {
                keyToValues.put(key, new LinkedHashSet<>());
            }
            keyToValues.get(key).add(value);
        }

        JSONObject constraints = new HashableJSONObject();
        for (Map.Entry<String, Set<Object>> entry : keyToValues.entrySet()) {
            final String key = entry.getKey();
            if (!key.equals("not")) {
                Object valueAfterOperation = MergeKeys.applyOperation(key, entry.getValue());
                constraints.put(key, valueAfterOperation);
            }
        }

        handleNotInMerge(constraints, keyToValues);
        return new JSONSchema(constraints, store, fullSchemaId);
    }

    /**
     * Get the contents of the key <code>not</code>, without any modifications.
     * 
     * @return The sub-schema contained in the key <code>not</code>.
     * @throws JSONSchemaException If it is not possible to construct the
     *                             sub-schema.
     */
    public JSONSchema getRawNot() throws JSONSchemaException {
        if (schema.has("not")) {
            return getSubSchema("not");
        } else {
            return store.falseSchema();
        }
    }

    /**
     * Gets a list of sub-schemas, after propagating the <code>not</code> inside
     * each sub-schema.
     * 
     * That is, if this schema contains <code>"not": {A, B, C}</code>, this function
     * returns a list with three elements. Each element is obtained by applying the
     * <code>not</code> inside A, B, and C, respectively.
     * 
     * That is, it transforms a <code>NOT AND</code> into an <code>OR NOT</code>.
     * 
     * If there is no <code>not</code> key, returns a singleton list with the true
     * schema.
     * 
     * @return
     * @throws JSONSchemaException
     */
    public List<JSONSchema> getNot() throws JSONSchemaException {
        if (schema.has("not")) {
            final JSONObject not = schema.getJSONObject("not");
            final JSONSchema actualSchema;
            if (not.has("$ref")) {
                actualSchema = handleRef(not.getString("$ref"));
            } else {
                actualSchema = new JSONSchema(not, store, fullSchemaId);
            }
            final List<JSONSchema> schemas = new ArrayList<>(actualSchema.schema.length());

            for (final String key : actualSchema.schema.keySet()) {
                final Object value = actualSchema.schema.get(key);
                final JSONObject notValue = MergeKeys.applyNot(key, Collections.singleton(value));
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

    /**
     * Gets the sub-schema for the property <code>key</code>.
     * 
     * @param key The key inside the properties.
     * @return The sub-schema.
     * @throws JSONException       If the <code>properties</code> sub-schema does
     *                             not contain <code>key</code>.
     * @throws JSONSchemaException If it is not possible to construct the
     *                             sub-schema.
     */
    public JSONSchema getSubSchemaProperties(String key) throws JSONException, JSONSchemaException {
        return getSubSchema(key, properties);
    }

    /**
     * Gets the sub-schema for the key <code>key</code>.
     * 
     * @param key The key.
     * @return The sub-schema.
     * @throws JSONException       If this schema does not contain <code>key</code>.
     * @throws JSONSchemaException If it is not possible to construct the
     *                             sub-schema.
     */
    public JSONSchema getSubSchema(String key) throws JSONException, JSONSchemaException {
        return getSubSchema(key, schema);
    }

    private JSONSchema getSubSchema(String key, JSONObject object) throws JSONException, JSONSchemaException {
        JSONObject subObject = new HashableJSONObject(object.getJSONObject(key));
        if (subObject.has("$ref")) {
            return handleRef(subObject.getString("$ref"));
        } else {
            return new JSONSchema(subObject, store, fullSchemaId);
        }
    }

    /**
     * Gets the sub-schemas for the items in an array.
     * 
     * @return A list with the sub-schemas defining the items in an array.
     * @throws JSONSchemaException If it is not possible to construct one of the
     *                             sub-schemas.
     */
    public List<JSONSchema> getItemsArray() throws JSONSchemaException {
        if (!schema.has("items")) {
            return Collections.singletonList(store.trueSchema());
        }
        List<JSONSchema> list = new ArrayList<>();
        Object items = schema.get("items");
        JSONArray array;
        if (items instanceof JSONArray) {
            array = (JSONArray) items;
        } else if (items instanceof JSONObject) {
            array = new HashableJSONArray();
            array.put((JSONObject) items);
        } else {
            throw new JSONSchemaException("Invalid type for \"items\" in schema " + this);
        }

        for (int i = 0; i < array.length(); i++) {
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

    int getSchemaId() {
        return fullSchemaId;
    }

    /**
     * Returns the {@link JSONSchemaStore} holding this schema.
     * @return The schema store
     */
    public JSONSchemaStore getStore() {
        return store;
    }

    @Override
    public String toString() {
        return schema.toString(2);
    }

}
