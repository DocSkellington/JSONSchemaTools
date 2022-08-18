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

package be.ac.umons.jsonschematools.validator;

import be.ac.umons.jsonschematools.validator.handlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultStringHandler;

/**
 * Constructs a {@link Validator} with default handlers.
 * 
 * @author Gaëtan Staquet
 */
public class DefaultValidator extends Validator {
    public DefaultValidator() {
        super(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(), new DefaultBooleanHandler(), new DefaultEnumHandler(), new DefaultObjectHandler(), new DefaultArrayHandler());
    }
}
