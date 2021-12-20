package be.ac.umons.jsonschematools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

class Keys {
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

    private static final Map<String, Operation> keyToOperation = new HashMap<>();

    private static final Set<String> minKeys = new HashSet<>();
    private static final Set<String> maxKeys = new HashSet<>();
    private static final Map<String, String> minToMax = new HashMap<>();
    private static final Map<String, String> maxToMin = new HashMap<>();

    public static void prepareKeys() {
        String[] mini = {"minItems", "minProperties", "minimum", "exclusiveMinimum", "minLength", "minContains"};
        String[] maxi = {"maxItems", "maxProperties", "maximum", "exclusiveMaximum", "maxLength", "maxContains"};
        String[] productKeys = {"multipleOf"};
        String[] andKeys = {"uniqueItems"};
        String[] concatKeys = {"required", "allOf", "anyOf", "oneOf"};
        String[] intersectionKeys = {"enum", "type"};
        String[] mergePropertiesKeys = {"properties", "$defs"};
        String[] mergeItemsKeys = {"items"};
        String[] checkEqualityKeys = {"$ref"};
        String[] notKeys = {"not"};
        // TODO: "const", "dependentRequired"
        // TODO: say that "pattern" is ignored

        for (int i = 0 ; i < maxi.length ; i++) {
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
        return Collections.unmodifiableSet(keyToOperation.keySet());
    }

    private static Operation getOperation(String key) {
        return keyToOperation.get(key);
    }

    private static int getMinimum(Set<Object> values) {
        return values.stream().mapToInt(v -> (int)v).min().getAsInt();
    }
    
    private static int getMaximum(Set<Object> values) {
        return values.stream().mapToInt(v -> (int)v).max().getAsInt();
    }

    private static int getProduct(Set<Object> values) {
        return values.stream().mapToInt(v -> (int)v).reduce((a, b) -> a * b).getAsInt();
    }

    private static boolean getAnd(Set<Object> values) {
        return values.stream().map(v -> (boolean)v).reduce((a, b) -> a & b).get();
    }

    private static JSONArray getConcatenation(Set<Object> values) {
        Set<Object> union = new HashSet<>();
        Iterator<Object> iterator = values.iterator();
        JSONArray value = (JSONArray)iterator.next();
        union.addAll(value.toList());
        while (iterator.hasNext()) {
            value = (JSONArray)iterator.next();
            union.addAll(value.toList());
        }
        return new JSONArray(union);
    }

    private static JSONArray getIntersection(Set<Object> values) {
        Set<Object> intersection = new HashSet<>();
        Iterator<Object> iterator = values.iterator();
        Object nextValue = iterator.next();
        if (nextValue instanceof String) {
            intersection.addAll(Collections.singleton(nextValue));
        }
        else if (nextValue instanceof JSONArray) {
            JSONArray value = (JSONArray) nextValue;
            intersection.addAll(value.toList());
        }
        while (iterator.hasNext()) {
            nextValue = iterator.next();
            if (nextValue instanceof String) {
                intersection.retainAll(Collections.singleton(nextValue));
            }
            else if (nextValue instanceof JSONArray) {
                JSONArray value = (JSONArray) nextValue;
                intersection.retainAll(value.toList());
            }
        }
        return new JSONArray(intersection);
    }

    public static JSONObject getMergeProperties(Set<Object> values) {
        // The idea is, for each key, to define a schema using allOf to merge all the values. The actual merging will be done at a later point
        Map<String, Set<JSONObject>> byKey = new HashMap<>();
        for (Object o : values) {
            JSONObject subSchema = (JSONObject)o;
            for (String key : subSchema.keySet()) {
                JSONObject value = subSchema.getJSONObject(key);
                if (!byKey.containsKey(key)) {
                    byKey.put(key, new HashSet<>());
                }
                byKey.get(key).add(value);
            }
        }
        JSONObject merge = new JSONObject();
        for (Map.Entry<String, Set<JSONObject>> entry : byKey.entrySet()) {
            JSONObject subSchema = new JSONObject();
            Set<JSONObject> objects = entry.getValue();
            if (objects.size() == 1) {
                subSchema = objects.iterator().next();
            }
            else if (objects.size() > 1) {
                subSchema.put("allOf", new JSONArray(objects));
            }
            merge.put(entry.getKey(), subSchema);
        }
        return merge;
    }

    public static JSONObject getMergeItems(Set<Object> values) {
        JSONObject merge = new JSONObject();
        merge.put("allOf", new JSONArray(values));
        return merge;
    }

    public static Object getCheckEquality(String key, Set<Object> values) throws JSONSchemaException {
        if (values.size() != 1) {
            throw new JSONSchemaException("JSON Schema: the values for the key " + key + " must be identical within a schema and the \"allOf\", \"anyOf\", \"oneOf\" keywords");
        }
        return values.iterator().next();
    }

    private static Set<String> keysToKeepInNot() {
        // TODO: more keywords?
        // TODO: oneOf
        Set<String> set = new HashSet<>(Set.of("items", "properties", "type", "not", "enum", "anyOf", "allOf"));
        set.addAll(minKeys);
        set.addAll(maxKeys);
        return set;
    }

    public static JSONObject applyNot(final String key, final Set<Object> values) throws JSONSchemaException {
        if (key.equals("properties")) {
            final JSONObject properties = (JSONObject) applyOperation(key, values);
            final JSONObject notProperties = new JSONObject();
            for (final String propertyKey : properties.keySet()) {
                final JSONObject notProperty = new JSONObject();
                notProperty.put("not", properties.get(propertyKey));
                notProperties.put(propertyKey, notProperty);
            }
            final JSONObject schema = new JSONObject();
            schema.put("properties", notProperties);
            return schema;
        }
        else if (key.equals("items")) {
            final JSONObject items = (JSONObject) applyOperation(key, values);
            final JSONObject schema = new JSONObject();
            schema.put("items", items);
            return schema;
        }
        else if (key.equals("type")) {
            final JSONArray types = (JSONArray) applyOperation(key, values);
            Set<String> allTypes = new HashSet<>(Set.of("string", "integer", "number", "object", "array", "boolean", "null"));
            for (int i = 0 ; i < types.length() ; i++) {
                String type = types.getString(i);
                allTypes.remove(type);
            }
            final JSONObject schema = new JSONObject();
            schema.put("type", new JSONArray(allTypes));
            return schema;
        }
        else if (key.equals("not")) {
            return (JSONObject) values.iterator().next();
        }
        else if (key.equals("allOf")) {
            final JSONArray allOf = (JSONArray) applyOperation(key, values);
            final JSONArray anyOf = new JSONArray(allOf.length());
            for (int i = 0 ; i < allOf.length() ; i++) {
                final JSONObject not = new JSONObject();
                not.put("not", allOf.get(i));
                anyOf.put(not);
            }
            final JSONObject schema = new JSONObject();
            schema.put("anyOf", anyOf);
            return schema;
        }
        else if (key.equals("anyOf")) {
            final JSONArray anyOf = (JSONArray) applyOperation(key, values);
            final JSONArray allOf = new JSONArray(anyOf.length());
            for (int i = 0 ; i < anyOf.length() ; i++) {
                final JSONObject not = new JSONObject();
                not.put("not", anyOf.get(i));
                allOf.put(not);
            }
            final JSONObject schema = new JSONObject();
            schema.put("allOf", allOf);
            return schema;
        }
        else if (key.equals("enum")) {
            JSONObject enumObject = new JSONObject();
            enumObject.put(key, new JSONArray(values));
            JSONObject notEnum = new JSONObject();
            notEnum.put("not", enumObject);
            return notEnum;
        }
        else if (minKeys.contains(key)) {
            final String maxKey = maxToMin.get(key);
            int value = (int) applyOperation(key, values);
            final JSONObject schema = new JSONObject();
            schema.put(maxKey, value + 1);
            return schema;
        }
        else if (maxKeys.contains(key)) {
            final String minKey = minToMax.get(key);
            int value = (int) applyOperation(key, values);
            final JSONObject schema = new JSONObject();
            schema.put(minKey, value - 1);
            return schema;
        }
        return null;
    }

    public static List<JSONObject> getNot(final Set<Object> values) throws JSONSchemaException {
        Set<String> keysToKeep = keysToKeepInNot();
        Map<String, Set<Object>> valuesByKey = new HashMap<>();
        for (Object object : values) {
            JSONObject schema = (JSONObject)object;
            for (String key : schema.keySet()) {
                if (keysToKeep.contains(key)) {
                    if (!valuesByKey.containsKey(key)) {
                        valuesByKey.put(key, new HashSet<>());
                    }
                    valuesByKey.get(key).add(schema.get(key));
                }
            }
        }

        if (valuesByKey.isEmpty()) {
            return Collections.singletonList(new JSONObject());
        }
        final List<JSONObject> disjunction = new ArrayList<>();
        for (final Map.Entry<String, Set<Object>> entry : valuesByKey.entrySet()) {
            final JSONObject not = new JSONObject();
            final JSONObject object = applyNot(entry.getKey(), entry.getValue());
            if (entry.getKey().equals("not")) {
                not.put("not", object);
            }
            else {
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

    public static String getKeyAfterNotOperation(String key) {
        switch(key) {
            case "not":
                return "anyOf";
            case "minItems":
                return "maxItems";
            case "maxItems":
                return "minItems";
            case "minProperties":
                return "maxProperties";
            case "maxProperties":
                return "minProperties";
            case "minimum":
                return "maximum";
            case "maximum":
                return "minimum";
            case "exclusiveMinimum":
                return "exclusiveMaximum";
            case "exclusiveMaximum":
                return "exclusiveMinimum";
            case "minLength":
                return "maxLength";
            case "maxLength":
                return "minLength";
            case "minContains":
                return "maxContains";
            case "maxContains":
                return "minContains";
            case "uniqueItems":
            case "properties":
            case "items":
            case "type":
                return key;
        }
        return null;
    }
}
