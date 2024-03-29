/*
 * JSONSchemaTools - Generators and validator for JSON schema, with abstract values
 *
 * Copyright 2022 University of Mons, University of Antwerp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    /**
     * Loads a JSON schema from a file stored in the computer's filesystem.
     * 
     * This implementation does not support downloading documents from the Internet.
     * Every schema must be locally available.
     * @param path The path to the file
     * @return The schema
     * @throws FileNotFoundException
     * @throws JSONSchemaException
     */
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
        FileReader reader = new FileReader(new File(path));
        JSONObject object = new HashableJSONObject(new JSONTokener(reader));
        return load(object, actualPath);
    }

    /**
     * Loads a JSON schema directly from a JSON object
     * @param schemaObject The JSON document
     * @return The schema
     * @throws JSONSchemaException
     */
    public JSONSchema loadFromJSONObject(JSONObject schemaObject) throws JSONSchemaException {
        final Path path = Paths.get("fromSchema");
        return load(new HashableJSONObject(schemaObject), path);
    }

    private JSONSchema load(JSONObject schemaObject, Path path) throws JSONSchemaException {
        final int schemaId = schemas.size();
        final JSONSchema schema = new JSONSchema(schemaObject, this, schemaId);
        schemas.add(schema);
        pathToSchema.put(path, schema);
        idToPath.put(schemaId, path);
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
