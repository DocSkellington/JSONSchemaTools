package be.ac.umons.jsonschematools;

import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Constants used for our abstractions for strings, integers, numbers, and enum
 * values.
 * 
 * Strings are always equal to "\S", ignoring the requirements of the schema. In
 * a similar way, integers are always "\I", numbers "\D", and enum values "\E".
 * 
 * @author Gaëtan Staquet
 */
public class AbstractConstants {
    public static String stringConstant = "\\S";
    public static String integerConstant = "\\I";
    public static String numberConstant = "\\D";
    public static String enumConstant = "\\E";

    /**
     * Abstract a const value given in a schema.
     * 
     * That is, it recursively replaces the strings by "\S", the integers by "\I",
     * and so on. See {@link AbstractConstants} for the abstracted values.
     * 
     * @param object The object to abstract
     * @return An abstracted version of the object, or <code>null</code> if the
     *         object can not be abstracted.
     */
    public static Object abstractConstValue(Object object) {
        if (object instanceof String) {
            return AbstractConstants.stringConstant;
        } else if (object instanceof Integer) {
            return AbstractConstants.integerConstant;
        } else if (object instanceof Number) {
            return AbstractConstants.numberConstant;
        } else if (object instanceof Boolean) {
            return object;
        } else if (object instanceof JSONArray) {
            final JSONArray array = (JSONArray) object;
            final JSONArray newArray = new JSONArray(array.length());
            for (Object inArray : array) {
                newArray.put(abstractConstValue(inArray));
            }
            return newArray;
        } else if (object instanceof JSONObject) {
            final JSONObject original = (JSONObject) object;
            final JSONObject newObject = new JSONObject();
            for (String key : original.keySet()) {
                newObject.put(key, abstractConstValue(original.get(key)));
            }
            return newObject;
        } else if (Objects.equals(object, JSONObject.NULL)) {
            return object;
        }
        return null;
    }
}
