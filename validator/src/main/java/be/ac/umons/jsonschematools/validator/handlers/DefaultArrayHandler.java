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

import org.json.JSONArray;
import org.json.JSONException;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.validator.Validator;

/**
 * A handler to validate abstract arrays.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultArrayHandler implements Handler {

    @Override
    public boolean validate(Validator validator, final JSONSchema schema, final Object object)
            throws JSONException, JSONSchemaException {
        if (!(object instanceof JSONArray)) {
            return false;
        }
        JSONArray array = (JSONArray) object;

        int minItem = schema.getIntOr("minItems", 0);
        int maxItem = schema.getIntOr("maxItems", Integer.MAX_VALUE);

        if (array.length() < minItem || array.length() > maxItem) {
            return false;
        }

        JSONSchema itemsArraySchema = schema.getItemsSchema();
        for (Object item : array) {
            if (!validator.validateValue(itemsArraySchema, item)) {
                return false;
            }
        }

        Set<JSONArray> forbiddenValues = schema.getForbiddenValuesFilteredByType(JSONArray.class);
        if (forbiddenValues.stream().filter(v -> array.similar(v)).count() != 0) {
            return false;
        }

        if (schema.getConstValue() != null) {
            JSONArray abstracted = (JSONArray) AbstractConstants.abstractConstValue(schema.getConstValue());
            if (!array.similar(abstracted)) {
                return false;
            }
        }

        return true;
    }

}
