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

/**
 * Contains a generator that produces documents by exhaustively exploring the JSON schema.
 * 
 * With the default value handlers, the produced documents have abstracted values.
 * For instance, integer values are all represented by the special symbol {@code \I}.
 * See {@link be.ac.umons.jsonschematools.AbstractConstants} for all abstract values, and the <a href="https://docskellington.github.io/JSONSchemaTools/structure.html#generating-numbers-integers-strings-and-enumeration-values">non-API documentation</a> for more information.
 * 
 * @author Gaëtan Staquet
 */
package be.ac.umons.jsonschematools.generator.exploration;
