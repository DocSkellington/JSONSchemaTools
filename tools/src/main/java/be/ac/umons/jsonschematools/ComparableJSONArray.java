package be.ac.umons.jsonschematools;

import java.util.Collection;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A <code>JSONArray</code> that implements the {@link Comparable} interface.
 * 
 * The comparison of two arrays is based on their hash values. If
 * this array is compared with a boolean, a number, or a string, the other value
 * is always before this array (i.e., the compareTo returns a negative number).
 * If the array is compared with anything else, a {@code ClassCastException} is
 * thrown.
 * 
 * @author Gaëtan Staquet
 */
public class ComparableJSONArray extends JSONArray implements Comparable<Object> {
    public ComparableJSONArray() {
        super();
    }

    public ComparableJSONArray(int length) {
        super(length);
    }

    public ComparableJSONArray(Collection<?> values) {
        super(values);
    }

    public ComparableJSONArray(JSONArray array) {
        super(array);
    }

    @Override
    public JSONObject getJSONObject(int index) throws JSONException {
        return new ComparableJSONObject(super.getJSONObject(index));
    }

    @Override
    public JSONArray getJSONArray(int index) throws JSONException {
        return new ComparableJSONArray(super.getJSONArray(index));
    }

    @Override
    public Object get(int index) throws JSONException {
        Object value = super.get(index);
        if (value instanceof JSONObject) {
            return new ComparableJSONObject((JSONObject) value);
        } else if (value instanceof JSONArray) {
            return new ComparableJSONArray((JSONArray) value);
        }
        return value;
    }

    @Override
    public int compareTo(Object other) {
        if (other instanceof JSONArray) {
            final int thisHash = Objects.hash(this.toString());
            final int otherHash = Objects.hash(other.toString());

            return Integer.compare(thisHash, otherHash);
        } else if (other instanceof Number || other instanceof String || other instanceof Boolean) {
            return -1;
        }
        throw new ClassCastException("Impossible to compare a JSON array and " + other.getClass());
    }
}
