package be.ac.umons.jsonschematools;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSchema {
    @Test
    public void testDepth() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = TestGettingKeys.loadSchema("composition.json");
        Assert.assertEquals(schema.depth(), 3);
    }
}
