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

import org.json.JSONException;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.generator.exploration.ChoicesSequence;
import be.ac.umons.jsonschematools.generator.exploration.ExplorationGenerator;

/**
 * Generates a boolean by exploring all the possibilities in the schema.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultBooleanHandler extends AHandler {

    private static final List<Boolean> booleanValues = List.of(true, false);

    @Override
    public Optional<Object> generate(final JSONSchema schema, final ExplorationGenerator generator,
            int maxDocumentDepth, boolean generateInvalid, final ChoicesSequence choices)
            throws JSONSchemaException, JSONException {
        final List<Boolean> forbiddenValues = new ArrayList<>(schema.getForbiddenValuesFilteredByType(Boolean.class));

        if (forbiddenValues.contains(true) && forbiddenValues.contains(false)) {
            if (generateInvalid) {
                return generateBoolean(choices);
            }
            return Optional.empty();
        }

        final Boolean constValue = schema.getConstValueIfType(Boolean.class);
        if (constValue != null) {
            if (forbiddenValues.contains(constValue)) {
                if (generateInvalid) {
                    return Optional.of(constValue);
                }
                return Optional.empty();
            }

            if (generateInvalid) {
                return Optional.of(!constValue);
            }
            return Optional.of(constValue);
        }

        if (forbiddenValues.contains(true)) {
            if (generateInvalid) {
                return Optional.of(true);
            }
            return Optional.of(false);
        } else if (forbiddenValues.contains(false)) {
            if (generateInvalid) {
                return Optional.of(false);
            }
            return Optional.of(true);
        }

        if (generateInvalid) {
            return Optional.empty();
        }

        return generateBoolean(choices);
    }

    private Optional<Object> generateBoolean(final ChoicesSequence choices) {
        Integer index = choices.getIndexNextExclusiveSelectionInList(booleanValues.size());
        if (index == null) {
            return Optional.empty();
        }
        return Optional.of(booleanValues.get(index));
    }

}
