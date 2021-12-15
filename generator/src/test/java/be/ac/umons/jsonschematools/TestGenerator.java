package be.ac.umons.jsonschematools;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestGenerator {
    private JSONSchema loadSchema(String path) throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchemaStore store = new JSONSchemaStore();
        return store.load(TestGenerator.class.getResource("/" + path).toURI());
    }

    @Test(invocationCount = 100, timeOut = 1000)
    public void testGeneratorBasicTypes() throws URISyntaxException, FileNotFoundException, JSONSchemaException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("basicTypes.json");
        DefaultGenerator generator = new DefaultGenerator(100, 5);

        JSONObject document = generator.generate(schema, 5);
        Set<String> keys = document.keySet();
        Assert.assertEquals(keys.size(), 7);

        Assert.assertTrue(keys.contains("string"));
        Assert.assertEquals(document.getString("string"), AbstractConstants.stringConstant);

        Assert.assertTrue(keys.contains("double"));
        Assert.assertEquals(document.getString("double"), AbstractConstants.numberConstant);

        Assert.assertTrue(keys.contains("integer"));
        Assert.assertEquals(document.getString("integer"), AbstractConstants.integerConstant);

        Assert.assertTrue(keys.contains("boolean"));
        Assert.assertTrue(document.getBoolean("boolean") == true || document.getBoolean("boolean") == false);

        Assert.assertTrue(keys.contains("enumVar"));
        Assert.assertEquals(document.getString("enumVar"), AbstractConstants.enumConstant);

        Assert.assertTrue(keys.contains("object"));
        JSONObject object = document.getJSONObject("object");
        Assert.assertTrue(object.length() == 1 || object.length() == 0);
        if (object.length() == 1) {
            Assert.assertTrue(object.has("anything"));
        }

        Assert.assertTrue(keys.contains("array"));
        JSONArray array = document.getJSONArray("array");
        Assert.assertTrue(array.length() >= 2);
        for (int i = 0; i < array.length(); i++) {
            Assert.assertEquals(array.get(i), AbstractConstants.stringConstant);
        }
    }

    @Test(invocationCount = 100, timeOut = 1000)
    public void testGeneratorRecursiveList() throws FileNotFoundException, JSONSchemaException, URISyntaxException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("recursiveList.json");
        DefaultGenerator generator = new DefaultGenerator();

        JSONObject document = generator.generate(schema, 5);
        int depth = checkGeneratedRecursiveList(document);
        Assert.assertTrue(0 <= depth && depth <= 5);

        document = generator.generate(schema, 2);
        checkGeneratedRecursiveList(document);
    }

    private int checkGeneratedRecursiveList(JSONObject document) {
        Assert.assertTrue(document.length() == 1 || document.length() == 2);
        Assert.assertEquals(document.getString("name"), AbstractConstants.stringConstant);

        if (document.length() == 2) {
            JSONArray list = document.getJSONArray("list");
            Assert.assertTrue(list.length() <= 1);
            if (list.length() == 1) {
                return checkGeneratedRecursiveList(list.getJSONObject(0)) + 1;
            }
        }
        return 0;
    }

    @Test(invocationCount = 100, timeOut = 1000)
    public void testGeneratorDefinitionByRef() throws FileNotFoundException, JSONSchemaException, URISyntaxException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("definitionByRef.json");
        DefaultGenerator generator = new DefaultGenerator();
        JSONObject document = generator.generate(schema, 5);
        
        Assert.assertTrue(1 <= document.length() && document.length() <= 3);
        Assert.assertTrue(document.has("description"));

        JSONObject description = document.getJSONObject("description");
        Assert.assertEquals(description.length(), 1);
        Assert.assertTrue(description.has("arguments"));
        checkArgumentsInDefinitionByRef(description.getJSONObject("arguments"));

        if (document.has("comment")) {
            Assert.assertEquals(document.getString("comment"), AbstractConstants.stringConstant);
        }

        if (document.has("arguments")) {
            checkArgumentsInDefinitionByRef(document.getJSONObject("arguments"));
        }
    }

    private void checkArgumentsInDefinitionByRef(JSONObject arguments) {
        Assert.assertEquals(arguments.length(), 1);
        Assert.assertTrue(arguments.has("values"));
        JSONArray values = arguments.getJSONArray("values");
        Assert.assertTrue(1 <= values.length() && values.length() <= 5);
        for (int i = 0 ; i < values.length() ; i++) {
            Assert.assertTrue(values.getBoolean(i) == true || values.getBoolean(i) == false);
        }
    }

    @Test(invocationCount = 100, timeOut = 1000)
    public void testGeneratorSchemaTwoFiles() throws FileNotFoundException, JSONSchemaException, URISyntaxException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("firstPart.json");
        DefaultGenerator generator = new DefaultGenerator();
        JSONObject document = generator.generate(schema, 5);

        Assert.assertEquals(document.length(), 1);
        Assert.assertTrue(document.has("value"));

        JSONObject value = document.getJSONObject("value");
        Assert.assertTrue(1 <= value.length() && value.length() <= 2);
        Assert.assertTrue(value.has("id"));
        Assert.assertEquals(value.getString("id"), AbstractConstants.integerConstant);

        if (value.has("comment")) {
            Assert.assertEquals(value.getString("comment"), AbstractConstants.stringConstant);
        }
    }

    @Test(invocationCount = 100, timeOut = 1000)
    public void testGeneratorBoundedNumberProperties() throws FileNotFoundException, JSONSchemaException, URISyntaxException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("boundedProperties.json");
        DefaultGenerator generator = new DefaultGenerator();
        JSONObject document = generator.generate(schema, 5);

        Assert.assertTrue(1 <= document.length() && document.length() <= 2);
    }

    @Test(invocationCount = 100, timeOut = 1000)
    public void testGeneratorAllOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("allOf.json");
        Generator generator = new DefaultGenerator(5, 5);
        JSONObject document = generator.generate(schema, 5);

        Assert.assertEquals(document.length(), 2);
        Assert.assertTrue(document.has("allOfObject"));
        Assert.assertTrue(document.has("allOfArray"));

        JSONArray allOfArray = document.getJSONArray("allOfArray");
        Assert.assertTrue(2 <= allOfArray.length() && allOfArray.length() <= 4);

        JSONObject allOfObject = document.getJSONObject("allOfObject");
        Assert.assertEquals(allOfObject.length(), 2);
        Assert.assertTrue(allOfObject.has("prop"));
        Assert.assertTrue(allOfObject.has("val"));

        JSONArray prop = allOfObject.getJSONArray("prop");
        Assert.assertEquals(prop.length(), 2);

        Assert.assertEquals(allOfObject.get("val"), AbstractConstants.numberConstant);
    }

    @Test(invocationCount = 100, timeOut = 1000)
    public void testGeneratorAnyOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("anyOf.json");
        Generator generator = new DefaultGenerator(5, 5);
        JSONObject document = generator.generate(schema, 5);

        Assert.assertEquals(document.length(), 2);
        Assert.assertTrue(document.has("anyOfObject"));
        Assert.assertTrue(document.has("anyOfArray"));

        JSONArray anyOfArray = document.getJSONArray("anyOfArray");
        Assert.assertTrue(1 <= anyOfArray.length() || anyOfArray.length() <= 4);
        for (int i = 0 ; i < anyOfArray.length() ; i++) {
            Assert.assertEquals(anyOfArray.get(i), AbstractConstants.stringConstant);
        }
    }

    @Test(invocationCount = 100, timeOut = 1000)
    public void testGeneratorOneOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("oneOf.json");
        Generator generator = new DefaultGenerator(5, 5);
        JSONObject document = generator.generate(schema, 5);

        Assert.assertEquals(document.length(), 2);
        Assert.assertTrue(document.has("oneOfObject"));
        Assert.assertTrue(document.has("oneOfArray"));

        JSONArray oneOfArray = document.getJSONArray("oneOfArray");
        Assert.assertNotEquals(2 <= oneOfArray.length(), oneOfArray.length() <= 3);

        JSONObject oneOfObject = document.getJSONObject("oneOfObject");
        Assert.assertNotEquals(1 <= oneOfObject.length() && oneOfObject.length() <= 2, 0 <= oneOfObject.length() && oneOfObject.length() <= 1);
    }

    @Test(invocationCount = 100, timeOut = 1000)
    public void testNot() throws FileNotFoundException, JSONSchemaException, URISyntaxException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("notSchema.json");
        Generator generator = new DefaultGenerator(5, 5);
        JSONObject document = generator.generate(schema, 5);

        Assert.assertEquals(document.length(), 1);
        Assert.assertTrue(document.has("subObject"));

        JSONObject subObject = document.getJSONObject("subObject");
        Assert.assertEquals(subObject.length(), 2);

        Assert.assertEquals(subObject.getString("value"), AbstractConstants.stringConstant);
        Assert.assertTrue(subObject.getBoolean("empty") == true || subObject.getBoolean("empty") == false);
    }

    @Test(expectedExceptions = {GeneratorException.class}, invocationCount = 100, timeOut = 1000)
    public void testNotError() throws FileNotFoundException, JSONSchemaException, URISyntaxException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("notSchemaError.json");
        Generator generator = new DefaultGenerator();
        generator.generate(schema, 5);
    }

    @Test(invocationCount = 100, timeOut = 1000)
    public void testCodecov() throws FileNotFoundException, JSONSchemaException, URISyntaxException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("codecov.json");
        Validator validator = new DefaultValidator();
        Generator generator = new DefaultGenerator(5, 5);
        JSONObject document = generator.generate(schema, 5);
        Assert.assertTrue(validator.validate(schema, document));
    }
}
