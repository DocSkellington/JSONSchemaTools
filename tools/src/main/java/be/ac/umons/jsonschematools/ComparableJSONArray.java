package be.ac.umons.jsonschematools;

import java.util.Collection;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ComparableJSONArray extends JSONArray implements Comparable<ComparableJSONArray> {
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
            return new ComparableJSONObject((JSONObject)value);
        }
        else if (value instanceof JSONArray) {
            return new ComparableJSONArray((JSONArray)value);
        }
        return value;
    }

    @Override
    public int compareTo(ComparableJSONArray other) {
        final int thisHash = Objects.hash(this.toString());
        final int otherHash = Objects.hash(other.toString());

        return Integer.compare(thisHash, otherHash);
    }
}
