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

package be.ac.umons.jsonschematools.generator.exploration.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.generator.exploration.ChoicesSequence;
import be.ac.umons.jsonschematools.generator.exploration.ExplorationGenerator;

/**
 * Generates an array by exploring all the possibilities in the schema.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultArrayHandler extends AHandler {

    private final int maxItems;

    public DefaultArrayHandler() {
        this(Integer.MAX_VALUE - 1);
    }

    public DefaultArrayHandler(int maxItems) {
        this.maxItems = maxItems;
    }

    @Override
    public Optional<Object> generate(final JSONSchema schema, final ExplorationGenerator generator,
            int maxDocumentDepth, boolean canGenerateInvalid, final ChoicesSequence choices)
            throws JSONSchemaException, JSONException {
        if (maxDocumentDepth == 0) {
            return Optional.empty();
        }

        final List<JSONArray> forbiddenValues = new ArrayList<>(
                schema.getForbiddenValuesFilteredByType(JSONArray.class));

        final int newMaxDocumentDepth;
        if (maxDocumentDepth == -1) {
            newMaxDocumentDepth = -1;
        } else {
            newMaxDocumentDepth = maxDocumentDepth - 1;
        }
        Optional<Object> value = generateArray(schema, generator, newMaxDocumentDepth, canGenerateInvalid, choices);
        if (!value.isPresent()) {
            return value;
        }
        JSONArray array = (JSONArray) value.get();
        if (!canGenerateInvalid) {
            for (Object forbidden : forbiddenValues) {
                if (array.similar(forbidden)) {
                    return Optional.empty();
                }
            }
        }
        return value;
    }

    private Optional<Object> generateArray(final JSONSchema schema, final ExplorationGenerator generator,
            int maxDocumentDepth, boolean canGenerateInvalid, final ChoicesSequence choices)
            throws JSONSchemaException, JSONException {
        final JSONArray array = new JSONArray();

        final int minItems, maxItems;
        final boolean ignoreMinItems, ignoreMaxItems;

        if (canGenerateInvalid) {
            Boolean booleanValue = choices.getNextBooleanValue();
            if (booleanValue == null) {
                return null;
            }
            ignoreMinItems = booleanValue;
            booleanValue = choices.getNextBooleanValue();
            if (booleanValue == null) {
                return null;
            }
            ignoreMaxItems = booleanValue;
        } else {
            ignoreMinItems = ignoreMaxItems = false;
        }

        if (ignoreMinItems) {
            minItems = 0;
        } else {
            minItems = schema.getIntOr("minItems", 0);
        }

        if (ignoreMaxItems) {
            maxItems = this.maxItems;
        } else {
            maxItems = schema.getIntOr("maxItems", this.maxItems);
        }

        if (!canGenerateInvalid && minItems > maxItems) {
            return Optional.empty();
        }

        final JSONArray constValue = schema.getConstValueIfType(JSONArray.class);
        if (constValue != null) {
            if (!canGenerateInvalid && !(minItems <= constValue.length() && constValue.length() <= maxItems)) {
                return Optional.empty();
            }
            return Optional.of(AbstractConstants.abstractConstValue(constValue));
        }

        List<JSONSchema> itemsSchemaList = null;
        itemsSchemaList = schema.getItemsArray();

        final Integer length = length(minItems, maxItems, choices);
        if (length == null) {
            return Optional.empty();
        }
        for (int i = 0; i < length; i++) {
            final Integer index = choices.getIndexNextExclusiveSelectionInList(itemsSchemaList.size());
            if (index == null) {
                return Optional.empty();
            }
            JSONSchema itemsSchema = itemsSchemaList.get(index);
            if (itemsSchema == null) {
                array.put(new JSONObject());
            } else {
                Optional<Object> value = generator.generateValueAccordingToConstraints(itemsSchema, maxDocumentDepth,
                        canGenerateInvalid, choices);
                if (!value.isPresent()) {
                    return Optional.empty();
                }
                if (value != ExplorationGenerator.EMPTY_VALUE_DUE_TO_MAX_DEPTH) {
                    array.put(value.get());
                }
            }
        }

        return Optional.of(array);
    }

}
