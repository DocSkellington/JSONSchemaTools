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

/**
 * Exception thrown by a JSON Schema.
 * 
 * @author Gaëtan Staquet
 */
public class JSONSchemaException extends Exception {
    public JSONSchemaException(String message) {
        super(message);
    }
}
