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

    @Test(invocationCount = 10)
    public void testGeneratorBasicTypes() throws URISyntaxException, FileNotFoundException, JSONSchemaException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("basicTypes.json");
        DefaultGenerator generator = new DefaultGenerator(5);

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

    @Test(invocationCount = 10)
    public void testGeneratorRecursiveList() throws FileNotFoundException, JSONSchemaException, URISyntaxException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("recursiveList.json");
        DefaultGenerator generator = new DefaultGenerator();

        JSONObject document = generator.generate(schema, 5);
        int depth = checkGeneratedRecursiveList(document);
        Assert.assertTrue(0 <= depth && depth <= 5);
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
}
