package be.ac.umons.jsonschematools;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestGettingKeys {
    private JSONSchema loadSchema(String path) throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchemaStore store = new JSONSchemaStore();
        return store.load(TestGettingKeys.class.getResource("/" + path).toURI());
    }

    @Test
    public void testRecursive() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("recursiveList.json");
        Set<String> keys = schema.getAllKeysDefinedInSchema();
        Assert.assertEquals(keys.size(), 2);
        Assert.assertTrue(keys.contains("name"));
        Assert.assertTrue(keys.contains("list"));
    }

    @Test
    public void testBasicTypes() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("basicTypes.json");
        Set<String> keys = schema.getAllKeysDefinedInSchema();
        Assert.assertEquals(keys.size(), 8);
        Assert.assertTrue(keys.contains("string"));
        Assert.assertTrue(keys.contains("double"));
        Assert.assertTrue(keys.contains("integer"));
        Assert.assertTrue(keys.contains("boolean"));
        Assert.assertTrue(keys.contains("enumVar"));
        Assert.assertTrue(keys.contains("object"));
        Assert.assertTrue(keys.contains("anything"));
        Assert.assertTrue(keys.contains("array"));
    }

    @Test(timeOut = 1000)
    public void testDefinitionByRef() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("definitionByRef.json");
        Set<String> keys = schema.getAllKeysDefinedInSchema();
        Assert.assertEquals(keys.size(), 4);
        Assert.assertTrue(keys.contains("comment"));
        Assert.assertTrue(keys.contains("description"));
        Assert.assertTrue(keys.contains("arguments"));
        Assert.assertTrue(keys.contains("values"));
    }

    @Test
    public void testSchemaTwoFiles() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("firstPart.json");
        Set<String> keys = schema.getAllKeysDefinedInSchema();
        Assert.assertEquals(keys.size(), 3);
        Assert.assertTrue(keys.contains("value"));
        Assert.assertTrue(keys.contains("comment"));
        Assert.assertTrue(keys.contains("id"));
    }

    @Test
    public void testAllOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("allOf.json");
        Set<String> keys = schema.getAllKeysDefinedInSchema();
        Assert.assertEquals(keys.size(), 4);
        Assert.assertTrue(keys.contains("allOfObject"));
        Assert.assertTrue(keys.contains("prop"));
        Assert.assertTrue(keys.contains("val"));
        Assert.assertTrue(keys.contains("allOfArray"));
    }

    @Test
    public void testAnyOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("anyOf.json");
        Set<String> keys = schema.getAllKeysDefinedInSchema();
        Assert.assertEquals(keys.size(), 3);
        Assert.assertTrue(keys.contains("anyOfObject"));
        Assert.assertTrue(keys.contains("prop"));
        Assert.assertTrue(keys.contains("anyOfArray"));
    }

    @Test
    public void testOneOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("oneOf.json");
        Set<String> keys = schema.getAllKeysDefinedInSchema();
        Assert.assertEquals(keys.size(), 4);
        Assert.assertTrue(keys.contains("oneOfArray"));
        Assert.assertTrue(keys.contains("oneOfObject"));
        Assert.assertTrue(keys.contains("prop1"));
        Assert.assertTrue(keys.contains("prop2"));
    }

    @Test
    public void testNot() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("notSchema.json");
        Set<String> keys = schema.getAllKeysDefinedInSchema();
        Assert.assertEquals(keys.size(), 4);
        Assert.assertTrue(keys.contains("subObject"));
        Assert.assertTrue(keys.contains("value"));
        Assert.assertTrue(keys.contains("empty"));
        Assert.assertTrue(keys.contains("inNot"));
    }
}
