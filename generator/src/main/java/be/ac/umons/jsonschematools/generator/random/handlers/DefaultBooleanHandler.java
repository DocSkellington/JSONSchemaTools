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

import java.util.Random;
import java.util.Set;

import org.json.JSONException;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.generator.random.GeneratorException;
import be.ac.umons.jsonschematools.generator.random.RandomGenerator;

/**
 * A boolean handler that returns true or false, according to the given schema.
 * 
 * If the schema does not force one value, the produced boolean is selected at
 * random.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultBooleanHandler implements IHandler {

    @Override
    public Object generate(RandomGenerator generator, JSONSchema schema, int maxTreeSize, boolean canGenerateInvalid,
            Random rand) throws JSONSchemaException, GeneratorException, JSONException {
        Set<Boolean> forbiddenValues = schema.getForbiddenValuesFilteredByType(Boolean.class);

        final Object constValue = schema.getConstValueIfType(Boolean.class);
        boolean generateInvalid = generateInvalid(canGenerateInvalid, rand);
        if (constValue != null) {
            if (forbiddenValues.contains(constValue)) {
                if (generateInvalid) {
                    return constValue;
                }
                throw new GeneratorException(
                        "Impossible to generate a boolean as the value set by \"const\" is forbidden " + schema);
            }
            if (generateInvalid) {
                return !(boolean) constValue;
            } else {
                return constValue;
            }
        }
        if (forbiddenValues.contains(true) && forbiddenValues.contains(false)) {
            if (generateInvalid) {
                return rand.nextBoolean();
            }
            throw new GeneratorException(
                    "Impossible to generate a boolean as both true and false are forbidden " + schema);
        } else if (forbiddenValues.contains(true)) {
            if (generateInvalid) {
                return true;
            }
            return false;
        } else if (forbiddenValues.contains(false)) {
            if (generateInvalid) {
                return false;
            }
            return true;
        }
        return rand.nextBoolean();
    }

}
