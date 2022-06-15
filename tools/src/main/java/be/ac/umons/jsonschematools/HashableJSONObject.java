package be.ac.umons.jsonschematools;

import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * A {@code JSONObject} that implements the {@code hashCode} function.
 * 
 * @author Gaëtan Staquet
 */
public class HashableJSONObject extends JSONObject {

    public HashableJSONObject() {
        super();
    }

    public HashableJSONObject(JSONTokener tokener) {
        super(tokener);
    }

    public HashableJSONObject(JSONObject object) {
        super(object.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(keySet());
    }

    @Override
    public HashableJSONObject getJSONObject(String key) throws JSONException {
        JSONObject object = super.getJSONObject(key);
        return new HashableJSONObject(object);
    }

    @Override
    public JSONArray getJSONArray(String key) throws JSONException {
        return new HashableJSONArray(super.getJSONArray(key));
    }

    @Override
    public Object get(String key) throws JSONException {
        Object value = super.get(key);
        if (value instanceof JSONObject) {
            return new HashableJSONObject((JSONObject) value);
        } else if (value instanceof JSONArray) {
            return new HashableJSONArray((JSONArray) value);
        }
        return value;
    }

}
