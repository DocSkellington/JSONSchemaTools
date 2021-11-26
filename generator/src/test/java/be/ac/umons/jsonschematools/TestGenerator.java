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
    @Test(invocationCount = 10)
    public void testGenerator() throws URISyntaxException, FileNotFoundException, JSONSchemaException, JSONException, GeneratorException {
        JSONSchemaStore store = new JSONSchemaStore();
        JSONSchema schema = store.load(TestGenerator.class.getResource("/basicTypes.json").toURI());
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
}
