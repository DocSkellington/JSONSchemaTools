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

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.jsonschematools.validator.DefaultValidator;
import be.ac.umons.jsonschematools.validator.Validator;

public class TestValidator {
    private JSONSchema loadSchemaResource(String path)
            throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchemaStore store = new JSONSchemaStore();
        return store.load(TestValidator.class.getResource("/" + path).toURI());
    }

    @Test
    public void testMissingRequiredPropertiesBasicTypes()
            throws FileNotFoundException, JSONException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("basicTypes.json");
        Validator validator = new DefaultValidator();
        StringBuilder stringBuilder = new StringBuilder();
        // @formatter:off
        stringBuilder.append("{").
            append("\"integer\": ").append(AbstractConstants.integerConstant).
        append("}");
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(stringBuilder.toString())));
    }

    @Test
    public void testInvalidValueBasicTypes() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("basicTypes.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"integer\": ").append(AbstractConstants.numberConstant).
                append(',').
                append("\"boolean\": false").
                append(',').
                append("\"string\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"double\": ").append(AbstractConstants.numberConstant).
                append(",").
                append("\"enumVar\": ").append(AbstractConstants.enumConstant).
                append(",").
                append("\"object\": {").
                    append("\"anything\": ").append(AbstractConstants.integerConstant).
                append("}").
                append(",").
                append("\"array\": [").
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(']').
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testValidDocumentBasicTypes()
            throws JSONException, JSONSchemaException, FileNotFoundException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("basicTypes.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"integer\": ").append(AbstractConstants.integerConstant).
                append(',').
                append("\"boolean\": true").
                append(',').
                append("\"string\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"double\": ").append(AbstractConstants.numberConstant).
                append(",").
                append("\"enumVar\": ").append(AbstractConstants.enumConstant).
                append(",").
                append("\"object\": {").
                    append("\"anything\": ").append(AbstractConstants.integerConstant).
                append("}").
                append(",").
                append("\"array\": [").
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(']').
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testMissingRequiredPropertiesRecursiveList()
            throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("recursiveList.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
            append("\"name\": ").append(AbstractConstants.stringConstant).
            append(',').
            append("\"list\": [{").
                    append("\"name\": ").append(AbstractConstants.stringConstant).
                    append(',').
                    append("\"list\": [{").
                    append("}]").
                append("}]").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testInvalidValueRecursiveList() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("recursiveList.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
            append("\"name\": ").append(AbstractConstants.stringConstant).
            append(',').
            append("\"list\": [{").
                    append("\"name\": ").append(AbstractConstants.stringConstant).
                    append(',').
                    append("\"list\": [{").
                        append("\"name\": ").append(AbstractConstants.integerConstant).
                    append("}]").
                append("}]").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testValidDocumentRecursiveList() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("recursiveList.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
            append("\"name\": ").append(AbstractConstants.stringConstant).
            append(',').
            append("\"list\": [{").
                    append("\"name\": ").append(AbstractConstants.stringConstant).
                    append(',').
                    append("\"list\": [{").
                        append("\"name\": ").append(AbstractConstants.stringConstant).
                    append("}]").
                append("}]").
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testMissingReferencedPropertyDefinitionByRef()
            throws JSONException, JSONSchemaException, FileNotFoundException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("definitionByRef.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"comment\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"description\": {").
                    append("\"arguments\": {").
                    append('}').
                append('}').
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testValidDocumentDefinitionByRef()
            throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("definitionByRef.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"comment\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"description\": {").
                    append("\"arguments\": {").
                        append("\"values\": [true, true]").
                    append('}').
                append('}').
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"arguments\": {").
                    append("\"values\": [false, true, false, true, false]").
                append('}').
                append(',').
                append("\"description\": {").
                    append("\"arguments\": {").
                        append("\"values\": [true, true]").
                    append('}').
                append('}').
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testValidDocumentSchemaTwoFiles()
            throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("firstPart.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"value\": {").
                    append("\"comment\": ").append(AbstractConstants.stringConstant).
                    append(',').
                    append("\"id\": ").append(AbstractConstants.integerConstant).
                    append('}').
                append('}').
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testBoundedProperties() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("boundedProperties.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"str3\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"str1\": ").append(AbstractConstants.stringConstant).
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"str3\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"str1\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"str4\": ").append(AbstractConstants.stringConstant).
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testAllOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("allOf.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"allOfObject\": {").
                    append("\"prop\": [").
                        append(AbstractConstants.integerConstant).
                        append(',').
                        append(AbstractConstants.integerConstant).
                    append("],").
                    append("\"val\": ").append(AbstractConstants.numberConstant).
                append("},").
                append("\"allOfArray\": [").
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                append("]").
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"allOfObject\": {").
                    append("\"prop\": [").
                        append(AbstractConstants.integerConstant).
                    append("],").
                    append("\"val\": ").append(AbstractConstants.numberConstant).
                append("},").
                append("\"allOfArray\": [").
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                append("]").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"allOfObject\": {").
                    append("\"prop\": [").
                        append(AbstractConstants.integerConstant).
                        append(',').
                        append(AbstractConstants.integerConstant).
                    append("],").
                    append("\"val\": ").append(AbstractConstants.numberConstant).
                append("},").
                append("\"allOfArray\": [").
                    append(AbstractConstants.numberConstant).
                    append(',').
                    append(AbstractConstants.numberConstant).
                    append(',').
                    append(AbstractConstants.numberConstant).
                append("]").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testAnyOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("anyOf.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"anyOfObject\": {").
                    append("\"prop\": ").append(AbstractConstants.stringConstant).
                append("},").
                append("\"anyOfArray\": [").
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                append("]").
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"anyOfObject\": {").
                    append("\"prop\": ").append(AbstractConstants.integerConstant).
                append("},").
                append("\"anyOfArray\": [").
                    append(AbstractConstants.stringConstant).
                append("]").
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"anyOfObject\": {").
                    append("\"prop\": ").append(AbstractConstants.stringConstant).
                append("},").
                append("\"anyOfArray\": [").
                    append(false).
                    append(',').
                    append(true).
                    append(',').
                    append(true).
                append("]").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testOneOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("oneOf.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"oneOfObject\": {").
                append("},").
                append("\"oneOfArray\": [").
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                append("]").
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"oneOfObject\": {").
                    append("\"prop1\": ").append(AbstractConstants.stringConstant).
                    append(',').
                    append("\"prop2\": ").append(AbstractConstants.stringConstant).
                append("},").
                append("\"oneOfArray\": [").
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                append("]").
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"oneOfObject\": {").
                    append("\"prop1\": ").append(AbstractConstants.stringConstant).
                    append(',').
                    append("\"prop2\": ").append(AbstractConstants.stringConstant).
                append("},").
                append("\"oneOfArray\": [").
                    append(AbstractConstants.stringConstant).
                append("]").
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"oneOfObject\": {").
                    append("\"prop1\": ").append(AbstractConstants.stringConstant).
                append("},").
                append("\"oneOfArray\": [").
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                append("]").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"oneOfObject\": {").
                    append("\"prop1\": ").append(AbstractConstants.stringConstant).
                    append(',').
                    append("\"prop2\": ").append(AbstractConstants.stringConstant).
                append("},").
                append("\"oneOfArray\": [").
                    append(AbstractConstants.stringConstant).
                    append(',').
                    append(AbstractConstants.stringConstant).
                append("]").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testNot() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("notSchema.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"subObject\": {").
                    append("\"value\": ").append(AbstractConstants.stringConstant).
                    append(',').
                    append("\"empty\": true").
                append("}").
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"subObject\": {").
                    append("\"value\": ").append(AbstractConstants.integerConstant).
                    append(',').
                    append("\"empty\": false").
                append("}").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"subObject\": {").
                    append("\"value\": ").append(AbstractConstants.numberConstant).
                    append(',').
                    append("\"empty\": false").
                append("}").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testNotError() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("notSchemaError.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"subObject\": {").
                    append("\"value\": ").append(AbstractConstants.stringConstant).
                    append(',').
                    append("\"empty\": true").
                append("}").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"subObject\": {").
                    append("\"value\": ").append(AbstractConstants.integerConstant).
                    append(',').
                    append("\"empty\": false").
                append("}").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"subObject\": {").
                    append("\"value\": ").append(AbstractConstants.numberConstant).
                    append(',').
                    append("\"empty\": false").
                append("}").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testConst() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("withConst.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"positiveConstBoolean\": true").
                append(',').
                append("\"negativeConstBoolean\": false").
                append(',').
                append("\"positiveConstInteger\": ").append(AbstractConstants.integerConstant).
                append(',').
                append("\"negativeConstInteger\": ").append(AbstractConstants.integerConstant).
                append(',').
                append("\"positiveConstNumber\": ").append(AbstractConstants.numberConstant).
                append(',').
                append("\"negativeConstNumber\": ").append(AbstractConstants.numberConstant).
                append(',').
                append("\"positiveConstString\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"negativeConstString\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"positiveConstObject\": {").
                    append("\"test\": true").
                    append(',').
                    append("\"int\": ").append(AbstractConstants.integerConstant).
                    append("}").
                append(',').
                append("\"negativeConstObject\": {").
                    append("\"int\": ").append(AbstractConstants.integerConstant).
                    append("}").
                append(',').
                append("\"positiveConstArray\": [").
                    append(AbstractConstants.integerConstant).
                    append(',').
                    append(AbstractConstants.integerConstant).
                    append(',').
                    append(AbstractConstants.integerConstant).
                    append(']').
                append(',').
                append("\"negativeConstArray\": [").
                    append(AbstractConstants.integerConstant).
                    append(',').
                    append(AbstractConstants.integerConstant).
                    append(',').
                    append(AbstractConstants.integerConstant).
                    append(']').
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"positiveConstBoolean\": true").
                append(',').
                append("\"negativeConstBoolean\": false").
                append(',').
                append("\"positiveConstInteger\": ").append(AbstractConstants.integerConstant).
                append(',').
                append("\"negativeConstInteger\": ").append(AbstractConstants.integerConstant).
                append(',').
                append("\"positiveConstNumber\": ").append(AbstractConstants.numberConstant).
                append(',').
                append("\"negativeConstNumber\": ").append(AbstractConstants.numberConstant).
                append(',').
                append("\"positiveConstString\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"negativeConstString\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"positiveConstObject\": {").
                    append("\"test\": true").
                    append(',').
                    append("\"int\": ").append(AbstractConstants.integerConstant).
                    append("}").
                append(',').
                append("\"negativeConstObject\": {").
                    append("\"test\": true").
                    append(',').
                    append("\"int\": ").append(AbstractConstants.integerConstant).
                    append("}").
                append(',').
                append("\"positiveConstArray\": [").
                    append(AbstractConstants.integerConstant).
                    append(',').
                    append(AbstractConstants.integerConstant).
                    append(',').
                    append(AbstractConstants.integerConstant).
                    append(']').
                append(',').
                append("\"negativeConstArray\": [").
                    append(AbstractConstants.integerConstant).
                    append(',').
                    append(AbstractConstants.integerConstant).
                    append(',').
                    append(AbstractConstants.integerConstant).
                    append(']').
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"positiveConstBoolean\": true").
                append(',').
                append("\"negativeConstBoolean\": false").
                append(',').
                append("\"positiveConstInteger\": ").append(AbstractConstants.integerConstant).
                append(',').
                append("\"negativeConstInteger\": ").append(AbstractConstants.integerConstant).
                append(',').
                append("\"positiveConstNumber\": ").append(AbstractConstants.numberConstant).
                append(',').
                append("\"negativeConstNumber\": ").append(AbstractConstants.numberConstant).
                append(',').
                append("\"positiveConstString\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"negativeConstString\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"positiveConstObject\": {").
                    append("\"test\": true").
                    append(',').
                    append("\"int\": ").append(AbstractConstants.integerConstant).
                    append("}").
                append(',').
                append("\"negativeConstObject\": {").
                    append("\"int\": ").append(AbstractConstants.integerConstant).
                    append("}").
                append(',').
                append("\"positiveConstArray\": [").
                    append(AbstractConstants.integerConstant).
                    append(',').
                    append(AbstractConstants.integerConstant).
                    append(',').
                    append(AbstractConstants.integerConstant).
                    append(']').
                append(',').
                append("\"negativeConstArray\": [").
                    append(false).
                    append(',').
                    append(AbstractConstants.stringConstant).
                    append(']').
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testAdditionalAndPatternProperties()
            throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("additionalAndPatternProperties.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"key1\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"key2\": ").
                    append("[").
                        append("{").
                            append("\"^key*$\": true").
                        append("}").
                        append(",").
                        append("{").
                            append("\"^key*$\": true").
                        append("}").
                        append(",").
                        append("{").
                            append("\"^key*$\": true").
                        append("}").
                    append("]").
                append(',').
                append("\"^key3$\":").append(AbstractConstants.enumConstant).
                append(",").
                append(AbstractConstants.stringConstant).append(":").append(AbstractConstants.integerConstant).
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"key1\": ").append(AbstractConstants.stringConstant).
                append(',').
                append("\"key2\": ").
                    append("[").
                        append("{").
                            append("\"^key*$\": true").
                            append(",").
                            // Additional properties are forbidden here
                            append(AbstractConstants.stringConstant).append(": true").
                        append("}").
                        append(",").
                        append("{").
                            append("\"^key*$\": true").
                        append("}").
                        append(",").
                        append("{").
                            append("\"^key*$\": true").
                        append("}").
                    append("]").
                append(',').
                append("\"^key3$\":").append(AbstractConstants.enumConstant).
                append(",").
                append(AbstractConstants.stringConstant).append(":").append(AbstractConstants.integerConstant).
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testCompositionTopLevel() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("compositionTopLevel.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder
            .append("{")
            .append("}")
        ;
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder
            .append("{")
                .append("\"2\":")
                .append(AbstractConstants.stringConstant)
            .append("}")
        ;
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder
            .append("{")
                .append("\"1\":")
                    .append(AbstractConstants.stringConstant)
                .append(",")
                .append("\"2\":")
                    .append(AbstractConstants.stringConstant)
            .append("}")
        ;
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));

        builder = new StringBuilder();
        // @formatter:off
        builder
            .append("{")
                .append(AbstractConstants.stringConstant + ":")
                    .append(AbstractConstants.stringConstant)
                .append(",")
                .append("\"2\":")
                    .append(AbstractConstants.stringConstant)
            .append("}")
        ;
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));
    }
}
