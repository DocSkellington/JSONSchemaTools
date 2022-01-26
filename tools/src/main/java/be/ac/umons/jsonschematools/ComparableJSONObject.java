package be.ac.umons.jsonschematools;

import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ComparableJSONObject extends JSONObject implements Comparable<JSONObject> {

    public ComparableJSONObject() {
        super();
    }

    public ComparableJSONObject(JSONTokener tokener) {
        super(tokener);
    }

    public ComparableJSONObject(JSONObject object) {
        super(object.toString());
    }

    @Override
    public int compareTo(JSONObject other) {
        int thisKeys = Objects.hash(this.toMap());
        int otherKeys = Objects.hash(other.toMap());
        
        return Integer.compare(thisKeys, otherKeys);
    }

    @Override
    public ComparableJSONObject getJSONObject(String key) throws JSONException {
        JSONObject object = super.getJSONObject(key);
        return new ComparableJSONObject(object);
    }

    @Override
    public JSONArray getJSONArray(String key) throws JSONException {
        return new ComparableJSONArray(super.getJSONArray(key));
    }

    @Override
    public Object get(String key) throws JSONException {
        Object value = super.get(key);
        if (value instanceof JSONObject) {
            return new ComparableJSONObject((JSONObject)value);
        }
        else if (value instanceof JSONArray) {
            return new ComparableJSONArray((JSONArray)value);
        }
        return value;
    }
    
}
