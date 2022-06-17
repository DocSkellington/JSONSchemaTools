package be.ac.umons.jsonschematools;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestGettingKeys {
    public static JSONSchema loadSchema(String path) throws FileNotFoundException, JSONSchemaException, URISyntaxException {
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

    @Test(timeOut = 1000)
    public void testCodecov() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("codecov.json");
        Set<String> keys = schema.getAllKeysDefinedInSchema();

        Assert.assertFalse(keys.contains("$ref"));

        Assert.assertTrue(keys.contains("codecov"));
        Assert.assertTrue(keys.contains("url"));
        Assert.assertTrue(keys.contains("slug"));
        Assert.assertTrue(keys.contains("bot"));
        Assert.assertTrue(keys.contains("branch"));
        Assert.assertTrue(keys.contains("ci"));
        Assert.assertTrue(keys.contains("assume_all_flags"));
        Assert.assertTrue(keys.contains("strict_yaml_branch"));
        Assert.assertTrue(keys.contains("max_report_age"));
        Assert.assertTrue(keys.contains("disable_default_path_fixes"));
        Assert.assertTrue(keys.contains("require_ci_to_pass"));
        Assert.assertTrue(keys.contains("allow_pseudo_compare"));
        Assert.assertTrue(keys.contains("archive"));
        Assert.assertTrue(keys.contains("uploads"));

        Assert.assertTrue(keys.contains("notify"));
        Assert.assertTrue(keys.contains("after_n_builds"));
        Assert.assertTrue(keys.contains("countdown"));
        Assert.assertTrue(keys.contains("delay"));
        Assert.assertTrue(keys.contains("wait_for_ci"));

        Assert.assertTrue(keys.contains("ui"));
        Assert.assertTrue(keys.contains("hide_density"));
        Assert.assertTrue(keys.contains("hide_complexity"));
        Assert.assertTrue(keys.contains("hide_contextual"));
        Assert.assertTrue(keys.contains("hide_sunburst"));
        Assert.assertTrue(keys.contains("hide_search"));

        Assert.assertTrue(keys.contains("coverage"));
        Assert.assertTrue(keys.contains("precision"));
        Assert.assertTrue(keys.contains("round"));
        Assert.assertTrue(keys.contains("range"));

        Assert.assertTrue(keys.contains("notify"));

        Assert.assertTrue(keys.contains("irc"));
        Assert.assertTrue(keys.contains("url"));
        Assert.assertTrue(keys.contains("branches"));
        Assert.assertTrue(keys.contains("threshold"));
        Assert.assertTrue(keys.contains("message"));
        Assert.assertTrue(keys.contains("flags"));
        Assert.assertTrue(keys.contains("base"));
        Assert.assertTrue(keys.contains("only_pulls"));
        Assert.assertTrue(keys.contains("paths"));
        Assert.assertTrue(keys.contains("channel"));
        Assert.assertTrue(keys.contains("password"));
        Assert.assertTrue(keys.contains("nickserv_password"));
        Assert.assertTrue(keys.contains("notice"));

        Assert.assertTrue(keys.contains("slack"));
        Assert.assertTrue(keys.contains("url"));
        Assert.assertTrue(keys.contains("branches"));
        Assert.assertTrue(keys.contains("threshold"));
        Assert.assertTrue(keys.contains("message"));
        Assert.assertTrue(keys.contains("flags"));
        Assert.assertTrue(keys.contains("base"));
        Assert.assertTrue(keys.contains("only_pulls"));
        Assert.assertTrue(keys.contains("paths"));
        Assert.assertTrue(keys.contains("attachments"));

        Assert.assertTrue(keys.contains("gitter"));
        Assert.assertTrue(keys.contains("url"));
        Assert.assertTrue(keys.contains("branches"));
        Assert.assertTrue(keys.contains("threshold"));
        Assert.assertTrue(keys.contains("message"));
        Assert.assertTrue(keys.contains("flags"));
        Assert.assertTrue(keys.contains("base"));
        Assert.assertTrue(keys.contains("only_pulls"));
        Assert.assertTrue(keys.contains("paths"));

        Assert.assertTrue(keys.contains("hipchat"));
        Assert.assertTrue(keys.contains("url"));
        Assert.assertTrue(keys.contains("branches"));
        Assert.assertTrue(keys.contains("threshold"));
        Assert.assertTrue(keys.contains("message"));
        Assert.assertTrue(keys.contains("flags"));
        Assert.assertTrue(keys.contains("base"));
        Assert.assertTrue(keys.contains("only_pulls"));
        Assert.assertTrue(keys.contains("paths"));
        Assert.assertTrue(keys.contains("card"));
        Assert.assertTrue(keys.contains("notify"));

        Assert.assertTrue(keys.contains("webhook"));
        Assert.assertTrue(keys.contains("url"));
        Assert.assertTrue(keys.contains("branches"));
        Assert.assertTrue(keys.contains("threshold"));
        Assert.assertTrue(keys.contains("message"));
        Assert.assertTrue(keys.contains("flags"));
        Assert.assertTrue(keys.contains("base"));
        Assert.assertTrue(keys.contains("only_pulls"));
        Assert.assertTrue(keys.contains("paths"));

        Assert.assertTrue(keys.contains("email"));
        Assert.assertTrue(keys.contains("url"));
        Assert.assertTrue(keys.contains("branches"));
        Assert.assertTrue(keys.contains("threshold"));
        Assert.assertTrue(keys.contains("message"));
        Assert.assertTrue(keys.contains("flags"));
        Assert.assertTrue(keys.contains("base"));
        Assert.assertTrue(keys.contains("only_pulls"));
        Assert.assertTrue(keys.contains("paths"));
        Assert.assertTrue(keys.contains("layout"));
        Assert.assertTrue(keys.contains("+to"));

        Assert.assertTrue(keys.contains("status"));
        Assert.assertTrue(keys.contains("project"));
        Assert.assertTrue(keys.contains("patch"));
        Assert.assertTrue(keys.contains("changes"));

        Assert.assertTrue(keys.contains("ignore"));

        Assert.assertTrue(keys.contains("fixes"));

        Assert.assertTrue(keys.contains("flags"));
        Assert.assertTrue(keys.contains("joined"));
        Assert.assertTrue(keys.contains("required"));
        Assert.assertTrue(keys.contains("ignore"));
        Assert.assertTrue(keys.contains("paths"));
        Assert.assertTrue(keys.contains("assume"));

        Assert.assertTrue(keys.contains("comment"));
        Assert.assertTrue(keys.contains("layout"));
        Assert.assertTrue(keys.contains("require_changes"));
        Assert.assertTrue(keys.contains("require_base"));
        Assert.assertTrue(keys.contains("require_head"));
        Assert.assertTrue(keys.contains("branches"));
        Assert.assertTrue(keys.contains("behavior"));
        Assert.assertTrue(keys.contains("flags"));
        Assert.assertTrue(keys.contains("paths"));
    }

    @Test
    public void testAdditionalAndPatternProperties() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("additionalAndPatternProperties.json");
        Set<String> keys = schema.getAllKeysDefinedInSchema();

        Assert.assertTrue(keys.contains("^key*$"));
        Assert.assertTrue(keys.contains("key1"));
        Assert.assertTrue(keys.contains("key2"));
        Assert.assertTrue(keys.contains("^key3$"));
    }
}
