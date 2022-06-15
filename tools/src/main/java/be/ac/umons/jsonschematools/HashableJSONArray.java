package be.ac.umons.jsonschematools;

import java.util.Collection;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@code JSONArray} that overrides the {@link hashCode} function.
 * 
 * @author Gaëtan Staquet
 */
public class HashableJSONArray extends JSONArray {
    public HashableJSONArray() {
        super();
    }

    public HashableJSONArray(int length) {
        super(length);
    }

    public HashableJSONArray(Collection<?> values) {
        super(values);
    }

    public HashableJSONArray(JSONArray array) {
        super(array);
    }

    @Override
    public JSONObject getJSONObject(int index) throws JSONException {
        return new HashableJSONObject(super.getJSONObject(index));
    }

    @Override
    public JSONArray getJSONArray(int index) throws JSONException {
        return new HashableJSONArray(super.getJSONArray(index));
    }

    @Override
    public Object get(int index) throws JSONException {
        Object value = super.get(index);
        if (value instanceof JSONObject) {
            return new HashableJSONObject((JSONObject) value);
        } else if (value instanceof JSONArray) {
            return new HashableJSONArray((JSONArray) value);
        }
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.toList());
    }
}
