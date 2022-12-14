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

package be.ac.umons.jsonschematools.generator.random.handlers;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Type;
import be.ac.umons.jsonschematools.generator.random.GeneratorException;
import be.ac.umons.jsonschematools.generator.random.RandomGenerator;

/**
 * An array handler that returns an array in which elements are abstracted.
 * 
 * It does not support every keyword that can be used in a schema.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultArrayHandler implements IHandler {

    private final int maxItems;

    public DefaultArrayHandler() {
        this(Integer.MAX_VALUE - 1);
    }

    public DefaultArrayHandler(int maxItems) {
        this.maxItems = maxItems;
    }

    @Override
    public JSONArray generate(RandomGenerator generator, JSONSchema schema, int maxTreeSize, boolean canGenerateInvalid,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        if (maxTreeSize == 0) {
            return new JSONArray();
        }
        final Set<JSONArray> forbiddenValues = schema.getForbiddenValuesFilteredByType(JSONArray.class);
        final boolean generateInvalid = generateInvalid(canGenerateInvalid, rand);

        if (generateInvalid && !forbiddenValues.isEmpty()) {
            return (JSONArray) new ArrayList<>(forbiddenValues).get(rand.nextInt(forbiddenValues.size()));
        }

        JSONArray array = generateArray(generator, schema, maxTreeSize, rand, generateInvalid);

        for (int i = 0; i < 1000; i++) {
            boolean correct = true;
            if (!generateInvalid) {
                for (Object forbidden : forbiddenValues) {
                    if (array.similar(forbidden)) {
                        correct = false;
                        break;
                    }
                }
            }
            if (correct) {
                return array;
            } else {
                array = generateArray(generator, schema, maxTreeSize, rand, generateInvalid);
            }
        }

        throw new GeneratorException("Impossible to generate an array in 1000 tries " + schema);
    }

    private JSONArray generateArray(RandomGenerator generator, JSONSchema schema, int maxTreeSize,
            Random rand, boolean generateInvalid) throws JSONSchemaException, GeneratorException, JSONException {
        final JSONArray array = new JSONArray();

        final int minItems, maxItems;
        if (generateInvalid && rand.nextBoolean()) {
            minItems = 0;
        } else {
            minItems = schema.getIntOr("minItems", 0);
        }
        if (generateInvalid && rand.nextBoolean()) {
            maxItems = this.maxItems;
        } else {
            maxItems = schema.getIntOr("maxItems", this.maxItems);
        }

        if (!generateInvalid && minItems > maxItems) {
            throw new GeneratorException("Array: minItems can not be strictly greater than maxItems");
        }

        final JSONArray constValue = schema.getConstValueIfType(JSONArray.class);
        if (constValue != null) {
            if (!(minItems <= constValue.length() && constValue.length() <= maxItems)) {
                if (generateInvalid) {
                    return constValue;
                } else {
                    throw new GeneratorException("Impossible to generate an array for schema " + schema
                            + " since the const value is incorrect, with regards to minItems, or maxItems");
                }
            }
            return (JSONArray) AbstractConstants.abstractConstValue(constValue);
        }

        final JSONSchema itemsSchema = schema.getItemsSchema();

        int size = rand.nextInt(maxItems - minItems + 1) + minItems;
        if (itemsSchema == null) {
            for (int i = 0; i < size; i++) {
                array.put(new JSONObject());
            }
        } else {
            for (int i = 0; i < size; i++) {
                Object value = generator.generateAccordingToConstraints(itemsSchema, maxTreeSize - 1, generateInvalid,
                        rand);
                if (!Objects.equals(value, Type.NULL)) {
                    array.put(value);
                }
            }
        }

        return array;
    }
}
