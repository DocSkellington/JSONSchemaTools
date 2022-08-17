package be.ac.umons.jsonschematools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Stores the schemas used so far.
 * 
 * This implementation does not support downloading schemas from the Internet.
 * Every schema must be present locally.
 * 
 * @author Gaëtan Staquet
 */
public class JSONSchemaStore {

    private final static int TRUE_IDENTIFIER = -1;
    private final static int FALSE_IDENTIFIER = -2;

    private final boolean ignoreTrueAdditionalProperties;

    private final List<JSONSchema> schemas = new ArrayList<>();
    private final Map<Path, JSONSchema> pathToSchema = new LinkedHashMap<>();
    private final Map<Integer, Path> idToPath = new LinkedHashMap<>();

    public JSONSchemaStore() {
        this(false);
    }

    /**
     * @param ignoreTrueAdditionalProperties If true, schemas will discard
     *                                       "additionalProperties" if the value is
     *                                       true.
     */
    public JSONSchemaStore(final boolean ignoreTrueAdditionalProperties) {
        this.ignoreTrueAdditionalProperties = ignoreTrueAdditionalProperties;
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
        JSONObject object = new HashableJSONObject(new JSONTokener(reader));
        JSONSchema schema = new JSONSchema(object, this, schemaId);
        schemas.add(schema);
        pathToSchema.put(actualPath, schema);
        idToPath.put(schemaId, actualPath);
        return schema;
    }

    public static JSONObject trueDocument() {
        return new HashableJSONObject();
    }

    public JSONSchema trueSchema() throws JSONSchemaException {
        return new JSONSchema(trueDocument(), this, TRUE_IDENTIFIER);
    }

    public static JSONObject falseDocument() {
        JSONObject falseDocument = new HashableJSONObject();
        falseDocument.put("not", new HashableJSONObject());
        return falseDocument;
    }

    public JSONSchema falseSchema() throws JSONSchemaException {
        return new JSONSchema(falseDocument(), this, FALSE_IDENTIFIER);
    }

    public static boolean isTrueDocument(JSONObject document) {
        return document.length() == 0;
    }

    public static boolean isTrueSchema(JSONSchema schema) {
        return schema.getSchemaId() == TRUE_IDENTIFIER || isTrueDocument(schema.getSchema());
    }

    public static boolean isFalseDocument(JSONObject document) {
        if (document.length() == 1 && document.has("not")) {
            try {
                return isTrueDocument(document.getJSONObject("not"));
            } catch (JSONException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isFalseSchema(JSONSchema schema) {
        return schema.getSchemaId() == FALSE_IDENTIFIER || isFalseDocument(schema.getSchema());
    }

    boolean shouldIgnoreTrueAdditionalProperties() {
        return this.ignoreTrueAdditionalProperties;
    }

    JSONSchema get(int schemaId) {
        return schemas.get(schemaId);
    }

    JSONSchema loadRelative(final int schemaId, final String relativePath)
            throws FileNotFoundException, JSONSchemaException {
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
