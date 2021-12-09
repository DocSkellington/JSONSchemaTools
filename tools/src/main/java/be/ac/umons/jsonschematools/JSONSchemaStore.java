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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONSchemaStore {

    private static int TRUE_IDENTIFIER = -1;
    private static int FALSE_IDENTIFIER = -2;

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

    public static JSONObject trueDocument() {
        return new JSONObject();
    }

    public JSONSchema trueSchema() throws JSONSchemaException {
        return new JSONSchema(trueDocument(), this, TRUE_IDENTIFIER);
    }

    public static JSONObject falseDocument() {
        JSONObject falseDocument = new JSONObject();
        falseDocument.put("not", new JSONObject());
        return falseDocument;
    }

    public JSONSchema falseSchema() throws JSONSchemaException {
        return new JSONSchema(falseDocument(), this, FALSE_IDENTIFIER);
    }
    
    public static boolean isTrueDocument(JSONObject document) {
        return document.length() == 0;
    }

    public static boolean isTrueSchema(JSONSchema schema) {
        return schema.getSchemaId() == TRUE_IDENTIFIER;
    }

    public static boolean isFalseDocument(JSONObject document) {
        if (document.length() == 1 && document.has("not")) {
            try {
                return isTrueDocument(document.getJSONObject("not"));
            }
            catch (JSONException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isFalseSchema(JSONSchema schema) {
        return schema.getSchemaId() == FALSE_IDENTIFIER;
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
