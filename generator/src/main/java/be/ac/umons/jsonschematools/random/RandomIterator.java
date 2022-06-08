package be.ac.umons.jsonschematools.random;

import java.util.Iterator;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;

/**
 * An iterator over the documents produced by a {@link RandomGenerator}.
 * 
 * @author Gaëtan Staquet
 */
public class RandomIterator implements Iterator<JSONObject> {

    private final JSONSchema schema;
    private final RandomGenerator generator;
    private final Random random;
    private final int maxDocumentDepth;
    private final boolean canGenerateInvalid;

    RandomIterator(JSONSchema schema, int maxDocumentDepth, boolean canGenerateInvalid, RandomGenerator generator, Random random) {
        this.schema = schema;
        this.random = random;
        this.generator = generator;
        this.maxDocumentDepth = maxDocumentDepth;
        this.canGenerateInvalid = canGenerateInvalid;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public JSONObject next() {
        try {
            return generator.generate(schema, maxDocumentDepth, canGenerateInvalid, random);
        } catch (JSONException | JSONSchemaException | GeneratorException e) {
            return null;
        }
    }
    
}
