package be.ac.umons.jsonschematools.generator;

import java.util.Iterator;

import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;

/**
 * The interface for a generator producing JSON documents by using a JSONSchema.
 * 
 * @author Gaëtan Staquet
 */
public interface IGenerator {
    /**
     * Creates an iterator over the documents this generator can produce.
     * 
     * Documents are created when calling {@code next()}.
     * 
     * The depth of the documents is not bounded, i.e., they can be an infinite
     * number of documents if the schema is recursive.
     * 
     * Only valid documents are generated.
     * 
     * @param schema The schema
     * @return An iterator
     */
    default Iterator<JSONObject> createIterator(JSONSchema schema) {
        return createIterator(schema, -1);
    }

    /**
     * Creates an iterator over the documents this generator can produce up to the
     * given document depth.
     * 
     * Documents are created when calling {@code next()}.
     * 
     * The depth of the documents is bounded, i.e., in any document, there can only
     * be at most {@code maxDocumentDepth} nested objects and arrays.
     * Note that if the bound is set too low, only invalid documents may be
     * generated as the deepest objects or arrays may not be correct.
     * In particular, no documents will be generated with a depth of zero.
     * 
     * Only valid documents are generated.
     * 
     * @param schema           The schema
     * @param maxDocumentDepth The maximal depth of the documents
     * @return An iterator
     */
    default Iterator<JSONObject> createIterator(JSONSchema schema, int maxDocumentDepth) {
        return createIterator(schema, maxDocumentDepth, false);
    }

    /**
     * Creates an iterator over the documents this generator can produce.
     * 
     * Documents are created when calling {@code next()}.
     * 
     * The depth of the documents is not bounded, i.e., they can be an infinite
     * number of documents if the schema is recursive.
     * 
     * Invalid documents can be generated if {@code canGenerateInvalid} is
     * {@code true}.
     * Note that valid documents can still be generated, no matter the value of
     * {@code canGenerateInvalid}.
     * 
     * @param schema             The schema
     * @param canGenerateInvalid Whether invalid documents can be generated
     * @return An iterator
     */
    default Iterator<JSONObject> createIterator(JSONSchema schema, boolean canGenerateInvalid) {
        return createIterator(schema, -1, canGenerateInvalid);
    }

    /**
     * Creates an iterator over the documents this generator can produce up to the
     * given document depth.
     * 
     * Documents are created when calling {@code next()}.
     * 
     * The depth of the documents is bounded, i.e., in any document, there can only
     * be at most {@code maxDocumentDepth} nested objects and arrays.
     * Note that if the bound is set too low, only invalid documents may be
     * generated as the deepest objects or arrays may not be correct.
     * In particular, no documents will be generated with a depth of zero.
     * 
     * Invalid documents can be generated if {@code canGenerateInvalid} is
     * {@code true}.
     * Note that valid documents can still be generated, no matter the value of
     * {@code canGenerateInvalid}.
     * 
     * @param schema             The schema
     * @param documentDepth     The maximal depth of the documents
     * @param canGenerateInvalid Whether invalid documents can be generated
     * @return An iterator
     */
    Iterator<JSONObject> createIterator(JSONSchema schema, int documentDepth, boolean canGenerateInvalid);
}
