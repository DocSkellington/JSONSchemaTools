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

import java.util.Collection;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@code JSONArray} that overrides the {@code hashCode} function.
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
