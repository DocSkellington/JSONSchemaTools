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

import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * A {@code JSONObject} that overrides the {@code hashCode} function.
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
