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

package be.ac.umons.jsonschematools.validator.handlers;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.validator.Validator;

/**
 * A handler to validate abstract objects.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultObjectHandler implements Handler {

    @Override
    public boolean validate(final Validator validator, final JSONSchema schema, final Object object)
            throws JSONSchemaException {
        if (!(object instanceof JSONObject)) {
            return false;
        }

        final JSONObject document = (JSONObject) object;
        final int minProperties = schema.getIntOr("minProperties", 0);
        final int maxProperties = schema.getIntOr("maxProperties", Integer.MAX_VALUE);

        if (!(minProperties <= document.length() && document.length() <= maxProperties)) {
            return false;
        }

        Set<String> requiredKeys = schema.getRequiredPropertiesKeys();
        if (!document.keySet().containsAll(requiredKeys)) {
            return false;
        }

        for (String key : document.keySet()) {
            JSONSchema schemaForKey;
            final Object objectForKey = document.get(key);

            try {
                schemaForKey = schema.getSubSchemaProperties(key);
            } catch (JSONException e) {
                schemaForKey = schema.getAdditionalProperties();
            }

            if (!validator.validateValue(schemaForKey, objectForKey)) {
                return false;
            }
        }

        Set<JSONObject> forbiddenValues = schema.getForbiddenValuesFilteredByType(JSONObject.class);
        if (forbiddenValues.stream().filter(v -> document.similar(v)).count() != 0) {
            return false;
        }

        if (schema.getConstValue() != null) {
            JSONObject abstracted = (JSONObject) AbstractConstants.abstractConstValue(schema.getConstValue());
            if (!document.similar(abstracted)) {
                return false;
            }
        }

        return true;
    }

}
