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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Lists the keys that are supported, and implements merging operations.
 * 
 * See the non-API documentation for more information on merging processes.
 * 
 * @author Gaëtan Staquet
 */
class MergeKeys {
    private static enum Operation {
        MINIMUM,
        MAXIMUM,
        PRODUCT,
        CONCATENATION,
        INTERSECTION,
        AND,
        MERGE_PROPERTIES,
        MERGE_ITEMS,
        CHECK_EQUALITY,
        NOT
    }

    private static final Map<String, Operation> keyToOperation = new LinkedHashMap<>();

    private static final Set<String> minKeys = new LinkedHashSet<>();
    private static final Set<String> maxKeys = new LinkedHashSet<>();
    private static final Map<String, String> minToMax = new LinkedHashMap<>();
    private static final Map<String, String> maxToMin = new LinkedHashMap<>();

    static {
        String[] mini = { "minItems", "minProperties", "minimum", "exclusiveMinimum", "minLength", "minContains" };
        String[] maxi = { "maxItems", "maxProperties", "maximum", "exclusiveMaximum", "maxLength", "maxContains" };
        String[] productKeys = { "multipleOf" };
        String[] andKeys = { "uniqueItems" };
        String[] concatKeys = { "required", "allOf", "anyOf", "oneOf" };
        String[] intersectionKeys = { "enum", "type" };
        String[] mergePropertiesKeys = { "properties", "patternProperties", "$defs", "definitions" };
        String[] mergeItemsKeys = { "items" };
        String[] checkEqualityKeys = { "$ref", "const", "additionalProperties" };
        String[] notKeys = { "not" };

        for (int i = 0; i < maxi.length; i++) {
            minToMax.put(mini[i], maxi[i]);
            maxToMin.put(maxi[i], mini[i]);
            minKeys.add(maxi[i]);
            maxKeys.add(mini[i]);
        }

        for (String key : minKeys) {
            keyToOperation.put(key, Operation.MINIMUM);
        }
        for (String key : maxKeys) {
            keyToOperation.put(key, Operation.MAXIMUM);
        }
        for (String key : productKeys) {
            keyToOperation.put(key, Operation.PRODUCT);
        }
        for (String key : andKeys) {
            keyToOperation.put(key, Operation.AND);
        }
        for (String key : concatKeys) {
            keyToOperation.put(key, Operation.CONCATENATION);
        }
        for (String key : intersectionKeys) {
            keyToOperation.put(key, Operation.INTERSECTION);
        }
        for (String key : mergePropertiesKeys) {
            keyToOperation.put(key, Operation.MERGE_PROPERTIES);
        }
        for (String key : mergeItemsKeys) {
            keyToOperation.put(key, Operation.MERGE_ITEMS);
        }
        for (String key : checkEqualityKeys) {
            keyToOperation.put(key, Operation.CHECK_EQUALITY);
        }
        for (String key : notKeys) {
            keyToOperation.put(key, Operation.NOT);
        }

    }

    public static Set<String> getKeys() {
        return keyToOperation.keySet();
    }

    private static Operation getOperation(String key) {
        return keyToOperation.get(key);
    }

    private static int getMinimum(Set<Object> values) {
        return values.stream().mapToInt(v -> (int) v).min().getAsInt();
    }

    private static int getMaximum(Set<Object> values) {
        return values.stream().mapToInt(v -> (int) v).max().getAsInt();
    }

    private static int getProduct(Set<Object> values) {
        return values.stream().mapToInt(v -> (int) v).reduce((a, b) -> a * b).getAsInt();
    }

    private static boolean getAnd(Set<Object> values) {
        return values.stream().map(v -> (boolean) v).reduce((a, b) -> a & b).get();
    }

    private static JSONArray getConcatenation(Set<Object> values) {
        Set<Object> union = new LinkedHashSet<>();
        Iterator<Object> iterator = values.iterator();
        JSONArray value = (JSONArray) iterator.next();
        union.addAll(value.toList());
        while (iterator.hasNext()) {
            value = (JSONArray) iterator.next();
            union.addAll(value.toList());
        }
        return new HashableJSONArray(union);
    }

    private static JSONArray getIntersection(Set<Object> values) {
        Set<Object> intersection = new LinkedHashSet<>();
        Iterator<Object> iterator = values.iterator();
        Object nextValue = iterator.next();
        if (nextValue instanceof String) {
            intersection.addAll(Collections.singleton(nextValue));
        } else if (nextValue instanceof JSONArray) {
            JSONArray value = (JSONArray) nextValue;
            intersection.addAll(value.toList());
        }
        while (iterator.hasNext()) {
            nextValue = iterator.next();
            if (nextValue instanceof String) {
                intersection.retainAll(Collections.singleton(nextValue));
            } else if (nextValue instanceof JSONArray) {
                JSONArray value = (JSONArray) nextValue;
                intersection.retainAll(value.toList());
            }
        }
        return new HashableJSONArray(intersection);
    }

    public static JSONObject getMergeProperties(Set<Object> values) {
        // The idea is, for each key, to define a schema using allOf to merge all the
        // values. The actual merging will be done at a later point
        Map<String, Set<JSONObject>> byKey = new LinkedHashMap<>();
        for (Object o : values) {
            JSONObject subSchema = (JSONObject) o;
            for (String key : subSchema.keySet()) {
                JSONObject value = subSchema.getJSONObject(key);
                if (!byKey.containsKey(key)) {
                    byKey.put(key, new LinkedHashSet<>());
                }
                byKey.get(key).add(value);
            }
        }
        JSONObject merge = new HashableJSONObject();
        for (Map.Entry<String, Set<JSONObject>> entry : byKey.entrySet()) {
            JSONObject subSchema = new HashableJSONObject();
            Set<JSONObject> objects = entry.getValue();
            if (objects.size() == 1) {
                subSchema = objects.iterator().next();
            } else if (objects.size() > 1) {
                subSchema.put("allOf", new HashableJSONArray(objects));
            }
            merge.put(entry.getKey(), subSchema);
        }
        return merge;
    }

    private static JSONObject getMergeItems(Set<Object> values) {
        JSONObject merge = new HashableJSONObject();
        merge.put("allOf", new HashableJSONArray(values));
        return merge;
    }

    private static Object getCheckEquality(String key, Set<Object> values) throws JSONSchemaException {
        if (values.size() != 1) {
            throw new JSONSchemaException("JSON Schema: the values for the key " + key
                    + " must be identical within a schema and the \"allOf\", \"anyOf\", \"oneOf\" keywords");
        }
        return values.iterator().next();
    }

    private static Set<String> keysToKeepInNot() {
        // TODO: more keywords?
        // TODO: oneOf
        Set<String> set = new LinkedHashSet<>(
                Set.of("items", "properties", "type", "not", "enum", "const", "anyOf", "allOf"));
        set.addAll(minKeys);
        set.addAll(maxKeys);
        return set;
    }

    public static JSONObject applyNot(final String key, final Set<Object> values) throws JSONSchemaException {
        if (key.equals("properties")) {
            final JSONObject properties = (JSONObject) applyOperation(key, values);
            final JSONObject notProperties = new HashableJSONObject();
            for (final String propertyKey : properties.keySet()) {
                final JSONObject notProperty = new HashableJSONObject();
                notProperty.put("not", properties.get(propertyKey));
                notProperties.put(propertyKey, notProperty);
            }
            final JSONObject schema = new HashableJSONObject();
            schema.put("properties", notProperties);
            return schema;
        } else if (key.equals("items")) {
            final JSONObject items = (JSONObject) applyOperation(key, values);
            final JSONObject schema = new HashableJSONObject();
            schema.put("items", items);
            return schema;
        } else if (key.equals("type")) {
            final JSONArray types = (JSONArray) applyOperation(key, values);
            Set<String> allTypes = new LinkedHashSet<>(
                    Set.of("string", "integer", "number", "object", "array", "boolean", "null"));
            for (int i = 0; i < types.length(); i++) {
                String type = types.getString(i);
                allTypes.remove(type);
            }
            final JSONObject schema = new HashableJSONObject();
            schema.put("type", new HashableJSONArray(allTypes));
            return schema;
        } else if (key.equals("not")) {
            return (JSONObject) values.iterator().next();
        } else if (key.equals("allOf")) {
            final JSONArray allOf = (JSONArray) applyOperation(key, values);
            final JSONArray anyOf = new HashableJSONArray(allOf.length());
            for (int i = 0; i < allOf.length(); i++) {
                final JSONObject not = new HashableJSONObject();
                not.put("not", allOf.get(i));
                anyOf.put(not);
            }
            final JSONObject schema = new HashableJSONObject();
            schema.put("anyOf", anyOf);
            return schema;
        } else if (key.equals("anyOf")) {
            final JSONArray anyOf = (JSONArray) applyOperation(key, values);
            final JSONArray allOf = new HashableJSONArray(anyOf.length());
            for (int i = 0; i < anyOf.length(); i++) {
                final JSONObject not = new HashableJSONObject();
                not.put("not", anyOf.get(i));
                allOf.put(not);
            }
            final JSONObject schema = new HashableJSONObject();
            schema.put("allOf", allOf);
            return schema;
        } else if (key.equals("enum")) {
            JSONObject enumObject = new HashableJSONObject();
            enumObject.put(key, new HashableJSONArray(values));
            JSONObject notEnum = new HashableJSONObject();
            notEnum.put("not", enumObject);
            return notEnum;
        } else if (key.equals("const")) {
            JSONObject constObject = new HashableJSONObject();
            constObject.put(key, values.iterator().next());
            JSONObject notConst = new HashableJSONObject();
            notConst.put("not", constObject);
            return notConst;
        } else if (minKeys.contains(key)) {
            final String maxKey = maxToMin.get(key);
            int value = (int) applyOperation(key, values);
            final JSONObject schema = new HashableJSONObject();
            schema.put(maxKey, value + 1);
            return schema;
        } else if (maxKeys.contains(key)) {
            final String minKey = minToMax.get(key);
            int value = (int) applyOperation(key, values);
            final JSONObject schema = new HashableJSONObject();
            schema.put(minKey, value - 1);
            return schema;
        }
        return null;
    }

    private static List<JSONObject> getNot(final Set<Object> values) throws JSONSchemaException {
        final Set<String> keysToKeep = keysToKeepInNot();
        final Map<String, Set<Object>> valuesByKey = new LinkedHashMap<>();
        for (Object object : values) {
            final JSONObject schema = (JSONObject) object;
            for (String key : schema.keySet()) {
                if (keysToKeep.contains(key)) {
                    if (!valuesByKey.containsKey(key)) {
                        valuesByKey.put(key, new LinkedHashSet<>());
                    }
                    valuesByKey.get(key).add(schema.get(key));
                }
            }
        }

        if (valuesByKey.isEmpty()) {
            return Collections.singletonList(new HashableJSONObject());
        }
        final List<JSONObject> disjunction = new ArrayList<>();
        for (final Map.Entry<String, Set<Object>> entry : valuesByKey.entrySet()) {
            final JSONObject not = new HashableJSONObject();
            final JSONObject object = applyNot(entry.getKey(), entry.getValue());
            if (entry.getKey().equals("not")) {
                not.put("not", object);
            } else {
                final String key = object.keys().next();
                not.put(key, object.get(key));
            }
            disjunction.add(not);
        }
        return disjunction;
    }

    public static Object applyOperation(String key, Set<Object> values) throws JSONSchemaException {
        final Operation operation = getOperation(key);
        if (operation == null) {
            return null;
        }
        switch (operation) {
            case AND:
                return getAnd(values);
            case CONCATENATION:
                return getConcatenation(values);
            case INTERSECTION:
                return getIntersection(values);
            case MAXIMUM:
                return getMaximum(values);
            case MERGE_PROPERTIES:
                return getMergeProperties(values);
            case MERGE_ITEMS:
                return getMergeItems(values);
            case MINIMUM:
                return getMinimum(values);
            case PRODUCT:
                return getProduct(values);
            case CHECK_EQUALITY:
                return getCheckEquality(key, values);
            case NOT:
                return getNot(values);
            default:
                return null;
        }
    }
}
