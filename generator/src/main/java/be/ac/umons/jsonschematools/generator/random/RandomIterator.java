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

package be.ac.umons.jsonschematools.generator.random;

import java.util.Iterator;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;

/**
 * An iterator over the documents produced by a {@link RandomGenerator}.
 * 
 * @author Gaëtan Staquet
 */
public class RandomIterator implements Iterator<JSONObject> {

    private final JSONSchema schema;
    private final RandomGenerator generator;
    private final Random random;
    private final int maxDocumentDepth;
    private final boolean canGenerateInvalid;

    RandomIterator(JSONSchema schema, int maxDocumentDepth, boolean canGenerateInvalid, RandomGenerator generator,
            Random random) {
        this.schema = schema;
        this.random = random;
        this.generator = generator;
        this.maxDocumentDepth = maxDocumentDepth;
        this.canGenerateInvalid = canGenerateInvalid;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public JSONObject next() {
        try {
            return generator.generate(schema, maxDocumentDepth, canGenerateInvalid, random);
        } catch (JSONException | JSONSchemaException | GeneratorException e) {
            return null;
        }
    }

}
