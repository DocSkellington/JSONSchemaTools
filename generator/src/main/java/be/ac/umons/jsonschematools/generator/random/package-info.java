/**
 * Contains a generator that produces documents by making random choices in the
 * JSON schema.
 * 
 * With the default value handlers, the produced documents have abstracted
 * values.
 * For instance, integer values are all represented by the special symbol
 * {@code \I}.
 * See {@link be.ac.umons.jsonschematools.AbstractConstants} for all abstract
 * values, and the <a href=
 * "https://docskellington.github.io/JSONSchemaTools/structure.html#generating-numbers-integers-strings-and-enumeration-values">non-API
 * documentation</a> for more information.
 * 
 * @author Gaëtan Staquet
 */
package be.ac.umons.jsonschematools.generator.random;
