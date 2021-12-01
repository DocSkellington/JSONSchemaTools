package be.ac.umons.jsonschematools;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        stringBuilder.append("{").append("\"integer\": \"").append("\\" + AbstractConstants.integerConstant)
                .append("\"").append("}");
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
                append("\"integer\": \"").append("\\" + AbstractConstants.numberConstant).append("\"").
                append(',').
                append("\"boolean\": false").
                append(',').
                append("\"string\": \"").append("\\" + AbstractConstants.stringConstant).append("\"").
                append(',').
                append("\"double\": \"").append("\\" + AbstractConstants.numberConstant).append("\"").
                append(",").
                append("\"enumVar\": \"").append("\\" + AbstractConstants.enumConstant).append("\"").
                append(",").
                append("\"object\": {\"anything\": \"").append("\\" + AbstractConstants.integerConstant).append("\"}").
                append(",").
                append("\"array\": [").append("\"\\" + AbstractConstants.stringConstant + "\"").append(',').append("\"\\" + AbstractConstants.stringConstant + "\"").append(']').
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
                append("\"integer\": \"").append("\\" + AbstractConstants.integerConstant).append("\"").
                append(',').
                append("\"boolean\": false").
                append(',').
                append("\"string\": \"").append("\\" + AbstractConstants.stringConstant).append("\"").
                append(',').
                append("\"double\": \"").append("\\" + AbstractConstants.numberConstant).append("\"").
                append(",").
                append("\"enumVar\": \"").append("\\" + AbstractConstants.enumConstant).append("\"").
                append(",").
                append("\"object\": {\"anything\": \"").append("\\" + AbstractConstants.integerConstant).append("\"}").
                append(",").
                append("\"array\": [").append("\"\\" + AbstractConstants.stringConstant + "\"").append(',').append("\"\\" + AbstractConstants.stringConstant + "\"").append(']').
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testMissingRequiredPropertiesRecursiveList() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("recursiveList.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
            append("\"name\": \"").append("\\" + AbstractConstants.stringConstant).append("\"").
            append(',').
            append("\"list\": [{").
                    append("\"name\": \"").append("\\" + AbstractConstants.stringConstant).append("\"").
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
            append("\"name\": \"").append("\\" + AbstractConstants.stringConstant).append("\"").
            append(',').
            append("\"list\": [{").
                    append("\"name\": \"").append("\\" + AbstractConstants.stringConstant).append("\"").
                    append(',').
                    append("\"list\": [{").
                        append("\"name\": \"").append("\\" + AbstractConstants.integerConstant).append("\"").
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
            append("\"name\": \"").append("\\" + AbstractConstants.stringConstant).append("\"").
            append(',').
            append("\"list\": [{").
                    append("\"name\": \"").append("\\" + AbstractConstants.stringConstant).append("\"").
                    append(',').
                    append("\"list\": [{").
                        append("\"name\": \"").append("\\" + AbstractConstants.stringConstant).append("\"").
                    append("}]").
                append("}]").
            append('}');
        // @formatter:on
        Assert.assertTrue(validator.validate(schema, new JSONObject(builder.toString())));
    }

    @Test
    public void testMissingReferencedPropertyDefinitionByRef() throws JSONException, JSONSchemaException, FileNotFoundException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("definitionByRef.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"comment\": \"").append("\\" + AbstractConstants.stringConstant).append("\"").
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
    public void testValidDocumentDefinitionByRef() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("definitionByRef.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"comment\": \"").append("\\" + AbstractConstants.stringConstant).append("\"").
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
    public void testValidDocumentSchemaTwoFiles() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchemaResource("firstPart.json");
        Validator validator = new DefaultValidator();
        StringBuilder builder = new StringBuilder();
        // @formatter:off
        builder.
            append('{').
                append("\"value\": {").
                    append("\"comment\": \"\\").append(AbstractConstants.stringConstant).append("\"").
                    append(',').
                    append("\"id\": \"\\").append(AbstractConstants.integerConstant).append("\"").
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
                append("\"str3\": \"\\").append(AbstractConstants.stringConstant).append("\"").
                append(',').
                append("\"str1\": \"\\").append(AbstractConstants.stringConstant).append("\"").
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
                append("\"str3\": \"\\").append(AbstractConstants.stringConstant).append("\"").
                append(',').
                append("\"str1\": \"\\").append(AbstractConstants.stringConstant).append("\"").
                append(',').
                append("\"str4\": \"\\").append(AbstractConstants.stringConstant).append("\"").
            append('}');
        // @formatter:on
        Assert.assertFalse(validator.validate(schema, new JSONObject(builder.toString())));
    }
}
