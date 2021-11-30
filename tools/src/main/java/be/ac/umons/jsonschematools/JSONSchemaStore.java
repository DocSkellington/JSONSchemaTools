package be.ac.umons.jsonschematools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONSchemaStore {

    private final List<JSONSchema> schemas = new ArrayList<>();

    public JSONSchema load(URI path) throws FileNotFoundException, JSONSchemaException {
        if (path.getHost() != null) {
            System.err.println(
                    "The implementation does not support downloading JSON schemas. Please make sure all the files are locally present in our machine and the paths are correctly set.");
            return null;
        }
        FileReader reader = new FileReader(new File(path));
        JSONObject object = new JSONObject(new JSONTokener(reader));
        JSONSchema schema = new JSONSchema(object, this, schemas.size());
        schemas.add(schema);
        // TODO: check whether the schema depends on other files and load them if needed
        return schema;
    }

    JSONSchema get(int schemaId) {
        return schemas.get(schemaId);
    }
}
