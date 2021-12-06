package be.ac.umons.jsonschematools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONSchemaStore {

    private static int TRUE_IDENTIFIER = -1;

    private final List<JSONSchema> schemas = new ArrayList<>();
    private final Map<Path, JSONSchema> pathToSchema = new HashMap<>();
    private final Map<Integer, Path> idToPath = new HashMap<>();

    public JSONSchemaStore() {
        Keys.prepareKeys();
    }

    public JSONSchema load(URI path) throws FileNotFoundException, JSONSchemaException {
        if (path.getHost() != null) {
            System.err.println(
                    "The implementation does not support downloading JSON schemas. Please make sure all the files are locally present in our machine and the paths are correctly set.");
            return null;
        }
        Path actualPath = Paths.get(path);
        if (pathToSchema.containsKey(actualPath)) {
            return pathToSchema.get(actualPath);
        }
        int schemaId = schemas.size();
        FileReader reader = new FileReader(new File(path));
        JSONObject object = new JSONObject(new JSONTokener(reader));
        JSONSchema schema = new JSONSchema(object, this, schemaId);
        schemas.add(schema);
        pathToSchema.put(actualPath, schema);
        idToPath.put(schemaId, actualPath);
        return schema;
    }

    public JSONSchema trueSchema() throws JSONSchemaException {
        return new JSONSchema(new JSONObject(), this, TRUE_IDENTIFIER);
    }

    public boolean isTrueSchema(JSONSchema schema) {
        return schema.getSchemaId() == TRUE_IDENTIFIER;
    }

    JSONSchema get(int schemaId) {
        return schemas.get(schemaId);
    }

    JSONSchema loadRelative(final int schemaId, final String relativePath) throws FileNotFoundException, JSONSchemaException {
        final Path basePath = idToPath.get(schemaId).getParent();
        final Path pathOfTargetSchema;
        if (relativePath.charAt(0) == '/') {
            pathOfTargetSchema = basePath.resolve(relativePath.substring(1) + ".json");
        } else {
            pathOfTargetSchema = basePath.resolve(relativePath + ".json");
        } 
        return load(pathOfTargetSchema.toUri());
    }
}
