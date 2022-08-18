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

package be.ac.umons.jsonschematools.generator.exploration;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;
import be.ac.umons.jsonschematools.generator.random.GeneratorException;
import be.ac.umons.jsonschematools.generator.random.TestRandomGenerator;
import be.ac.umons.jsonschematools.validator.DefaultValidator;
import be.ac.umons.jsonschematools.validator.Validator;

public class TestExplorationGenerator {

    private static class Pair<I1, I2> {
        private final I1 first;
        private final I2 second;

        private Pair(I1 first, I2 second) {
            this.first = first;
            this.second = second;
        }

        public I1 getFirst() {
            return first;
        }

        public I2 getSecond() {
            return second;
        }

        public static <A, B> Pair<A, B> of(A first, B second) {
            return new Pair<>(first, second);
        }
    }

    private JSONSchema loadSchema(String path, boolean shouldIgnoreTrueAdditionalProperties)
            throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchemaStore store = new JSONSchemaStore(shouldIgnoreTrueAdditionalProperties);
        return store.load(TestRandomGenerator.class.getResource("/" + path).toURI());
    }

    @Test
    public void testGeneratorZeroDepth() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("primitiveTypes.json", false);
        ExplorationGenerator generator = new DefaultExplorationGenerator();
        Iterator<JSONObject> iterator = generator.createIterator(schema, 0);
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testGeneratorPrimitiveTypes()
            throws URISyntaxException, FileNotFoundException, JSONSchemaException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("primitiveTypes.json", false);
        ExplorationGenerator generator = new DefaultExplorationGenerator();

        Iterator<JSONObject> iterator = generator.createIterator(schema);

        Assert.assertTrue(iterator.hasNext());
        JSONObject document = iterator.next();

        Set<String> keys = document.keySet();
        Assert.assertEquals(keys.size(), 5);

        Assert.assertTrue(keys.contains("string"));
        Assert.assertEquals(document.getString("string"), AbstractConstants.stringConstant);

        Assert.assertTrue(keys.contains("double"));
        Assert.assertEquals(document.getString("double"), AbstractConstants.numberConstant);

        Assert.assertTrue(keys.contains("integer"));
        Assert.assertEquals(document.getString("integer"), AbstractConstants.integerConstant);

        Assert.assertTrue(keys.contains("boolean"));
        Assert.assertTrue(document.getBoolean("boolean") == true);

        Assert.assertTrue(keys.contains("enumVar"));
        Assert.assertEquals(document.getString("enumVar"), AbstractConstants.enumConstant);

        Assert.assertTrue(iterator.hasNext());
        document = iterator.next();

        keys = document.keySet();
        Assert.assertEquals(keys.size(), 5);

        Assert.assertTrue(keys.contains("string"));
        Assert.assertEquals(document.getString("string"), AbstractConstants.stringConstant);

        Assert.assertTrue(keys.contains("double"));
        Assert.assertEquals(document.getString("double"), AbstractConstants.numberConstant);

        Assert.assertTrue(keys.contains("integer"));
        Assert.assertEquals(document.getString("integer"), AbstractConstants.integerConstant);

        Assert.assertTrue(keys.contains("boolean"));
        Assert.assertTrue(document.getBoolean("boolean") == false);

        Assert.assertTrue(keys.contains("enumVar"));
        Assert.assertEquals(document.getString("enumVar"), AbstractConstants.enumConstant);

        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testGeneratorBasicTypes()
            throws URISyntaxException, FileNotFoundException, JSONSchemaException, JSONException, GeneratorException {
        JSONSchema schema = loadSchema("basicTypes.json", true);
        ExplorationGenerator generator = new DefaultExplorationGenerator(4, 4);

        Iterator<JSONObject> iterator = generator.createIterator(schema);

        List<Object> listAnythingInObject = new ArrayList<>();
        listAnythingInObject.add(null);
        listAnythingInObject.add(AbstractConstants.numberConstant);
        listAnythingInObject.add(AbstractConstants.integerConstant);
        listAnythingInObject.add(true);
        listAnythingInObject.add(false);
        listAnythingInObject.add(AbstractConstants.stringConstant);

        for (final boolean booleanValue : List.of(true, false)) {
            for (Object anythingInObject : listAnythingInObject) {
                for (int sizeArray = 2; sizeArray <= 4; sizeArray++) {
                    Assert.assertTrue(iterator.hasNext());
                    JSONObject document = iterator.next();
                    checkValuesInBasicTypes(document, booleanValue, anythingInObject, sizeArray);
                }
            }
        }

        Assert.assertFalse(iterator.hasNext());
    }

    private void checkValuesInBasicTypes(JSONObject document, boolean expectedBoolean, Object anythingInObject,
            int sizeArray) {
        Set<String> keys = document.keySet();
        Assert.assertEquals(keys.size(), 7);

        Assert.assertTrue(keys.contains("string"));
        Assert.assertEquals(document.getString("string"), AbstractConstants.stringConstant);

        Assert.assertTrue(keys.contains("double"));
        Assert.assertEquals(document.getString("double"), AbstractConstants.numberConstant);

        Assert.assertTrue(keys.contains("integer"));
        Assert.assertEquals(document.getString("integer"), AbstractConstants.integerConstant);

        Assert.assertTrue(keys.contains("boolean"));
        Assert.assertTrue(document.getBoolean("boolean") == expectedBoolean);

        Assert.assertTrue(keys.contains("enumVar"));
        Assert.assertEquals(document.getString("enumVar"), AbstractConstants.enumConstant);

        Assert.assertTrue(keys.contains("object"));
        JSONObject object = document.getJSONObject("object");
        if (anythingInObject == null) {
            Assert.assertFalse(object.has("anything"));
        } else {
            Assert.assertTrue(object.has("anything"));
            Assert.assertEquals(object.get("anything"), anythingInObject);
        }
    }

    @Test
    public void testGeneratorDefinitionByRef() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("definitionByRef.json", true);
        ExplorationGenerator generator = new DefaultExplorationGenerator();
        Iterator<JSONObject> iterator = generator.createIterator(schema);

        // @formatter:off
        List<List<Boolean>> listDescriptionArgumentsValues = List.of(
            List.of(true),
            List.of(false),
            List.of(true, true),
            List.of(true, false),
            List.of(false, true),
            List.of(false, false)
        );
        // @formatter:on

        for (List<Boolean> descriptionArgumentsValues : listDescriptionArgumentsValues) {
            Assert.assertTrue(iterator.hasNext());
            JSONObject document = iterator.next();
            checkValuesInDefinitionByRef(document, descriptionArgumentsValues, List.of(), false);

            Assert.assertTrue(iterator.hasNext());
            document = iterator.next();
            checkValuesInDefinitionByRef(document, descriptionArgumentsValues, List.of(), true);

            for (boolean commentPresent : List.of(false, true)) {
                for (List<Boolean> argumentsValues : listDescriptionArgumentsValues) {
                    Assert.assertTrue(iterator.hasNext());
                    document = iterator.next();
                    checkValuesInDefinitionByRef(document, descriptionArgumentsValues, argumentsValues, commentPresent);
                }
            }
        }

        Assert.assertFalse(iterator.hasNext());
    }

    private void checkValuesInDefinitionByRef(final JSONObject document, final List<Boolean> descriptionArgumentsValues,
            final List<Boolean> argumentsValues, boolean shouldCommentBePresent) {
        Assert.assertTrue(document.has("description"));

        JSONObject description = document.getJSONObject("description");
        Assert.assertEquals(description.length(), 1);
        Assert.assertTrue(description.has("arguments"));

        JSONObject arguments = description.getJSONObject("arguments");
        Assert.assertEquals(arguments.length(), 1);
        Assert.assertTrue(arguments.has("values"));

        JSONArray values = arguments.getJSONArray("values");
        Assert.assertEquals(values.length(), descriptionArgumentsValues.size());
        for (int i = 0; i < values.length(); i++) {
            Assert.assertEquals(values.get(i), descriptionArgumentsValues.get(i));
        }

        if (argumentsValues.size() != 0) {
            Assert.assertTrue(document.has("arguments"));
            arguments = document.getJSONObject("arguments");
            Assert.assertEquals(arguments.length(), 1);
            Assert.assertTrue(arguments.has("values"));
            values = arguments.getJSONArray("values");
            Assert.assertEquals(values.length(), argumentsValues.size());
            for (int i = 0; i < values.length(); i++) {
                Assert.assertEquals(values.get(i), argumentsValues.get(i));
            }
        } else {
            Assert.assertFalse(document.has("arguments"));
        }

        if (shouldCommentBePresent) {
            Assert.assertTrue(document.has("comment"));
            Assert.assertEquals(document.get("comment"), AbstractConstants.stringConstant);
        } else {
            Assert.assertFalse(document.has("comment"));
        }
    }

    @Test
    public void testGeneratorAdditionalAndPatternProperties()
            throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("additionalAndPatternPropertiesForExploration.json", false);
        ExplorationGenerator generator = new DefaultExplorationGenerator();
        Iterator<JSONObject> iterator = generator.createIterator(schema);

        // @formatter:off
        final List<List<String>> listPresentKeys = List.of(
            List.of("key1", "key2", "\\S"          ),
            List.of("key1", "key2",        "^key3$"),
            List.of("key1",         "\\S", "^key3$"),
            List.of(        "key2", "\\S", "^key3$"),
            List.of("key1", "key2", "\\S", "^key3$")
        );
        // @formatter:on
        final List<String> allKeys = List.of("key1", "key2", "\\S", "^key3$");

        for (final List<String> presentKeys : listPresentKeys) {
            final List<String> absentKeys = new ArrayList<>(allKeys);
            absentKeys.removeAll(presentKeys);
            if (presentKeys.contains("key2")) {
                Assert.assertTrue(iterator.hasNext());
                final JSONObject document = iterator.next();
                checkValuesInAdditionalAndPatternProperties(document, presentKeys, absentKeys, true);
            } else {
                Assert.assertTrue(iterator.hasNext());
                final JSONObject document = iterator.next();
                checkValuesInAdditionalAndPatternProperties(document, presentKeys, absentKeys, false);
            }
        }

        Assert.assertFalse(iterator.hasNext());
    }

    private void checkValuesInAdditionalAndPatternProperties(final JSONObject document, final List<String> presentKeys,
            final List<String> absentKeys, boolean key2Present) {
        for (String key : presentKeys) {
            Assert.assertTrue(document.has(key));

            switch (key) {
                case "key1":
                    Assert.assertEquals(document.get(key), AbstractConstants.stringConstant);
                    break;
                case "key2":
                    JSONArray array = document.getJSONArray(key);
                    if (key2Present) {
                        Assert.assertEquals(array.length(), 1);
                        JSONObject objectInArray = array.getJSONObject(0);
                        Assert.assertTrue(objectInArray.has("^key*$"));
                        Assert.assertEquals(objectInArray.get("^key*$"), true);
                    }
                    break;
                case "\\S":
                    Assert.assertEquals(document.get(key), AbstractConstants.integerConstant);
                    break;
                case "^key3$":
                    Assert.assertEquals(document.get(key), AbstractConstants.enumConstant);
                    break;
            }
        }
        for (String key : absentKeys) {
            Assert.assertFalse(document.has(key));
        }
    }

    @Test
    public void testGeneratorAllOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("allOf.json", true);
        ExplorationGenerator generator = new DefaultExplorationGenerator();
        Iterator<JSONObject> iterator = generator.createIterator(schema);

        for (int sizeArray = 2; sizeArray <= 4; sizeArray++) {
            Assert.assertTrue(iterator.hasNext());
            JSONObject document = iterator.next();

            Assert.assertTrue(document.has("allOfArray"));
            JSONArray array = document.getJSONArray("allOfArray");
            Assert.assertEquals(array.length(), sizeArray);
            for (int i = 0; i < sizeArray; i++) {
                Assert.assertEquals(array.get(i), AbstractConstants.stringConstant);
            }

            Assert.assertTrue(document.has("allOfObject"));
            JSONObject object = document.getJSONObject("allOfObject");
            Assert.assertEquals(object.length(), 2);
            Assert.assertTrue(object.has("val"));
            Assert.assertEquals(object.get("val"), AbstractConstants.numberConstant);
            Assert.assertTrue(object.has("prop"));
            JSONArray prop = object.getJSONArray("prop");
            Assert.assertEquals(prop.length(), 2);
            for (int i = 0; i < prop.length(); i++) {
                Assert.assertEquals(prop.get(i), AbstractConstants.integerConstant);
            }
        }

        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testGeneratorAnyOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("anyOf.json", true);
        ExplorationGenerator generator = new DefaultExplorationGenerator(4, 4);
        Iterator<JSONObject> iterator = generator.createIterator(schema);

        // @formatter:off
        List<Pair<Integer, Integer>> arraySizeBounds = List.of(
            Pair.of(1, 4),
            Pair.of(1, 3),
            Pair.of(1, 3),
            Pair.of(2, 4),
            Pair.of(2, 4),
            Pair.of(2, 3),
            Pair.of(2, 3)
        );
        // @formatter:on

        for (String valueInObject : List.of(AbstractConstants.integerConstant, AbstractConstants.stringConstant)) {
            for (Pair<Integer, Integer> bounds : arraySizeBounds) {
                for (int sizeArray = bounds.getFirst(); sizeArray <= bounds.getSecond(); sizeArray++) {
                    Assert.assertTrue(iterator.hasNext());
                    JSONObject document = iterator.next();
                    Assert.assertEquals(document.length(), 2);

                    Assert.assertTrue(document.has("anyOfArray"));
                    JSONArray array = document.getJSONArray("anyOfArray");
                    Assert.assertEquals(array.length(), sizeArray);
                    for (int i = 0; i < array.length(); i++) {
                        Assert.assertEquals(array.get(i), AbstractConstants.stringConstant);
                    }
                    Assert.assertTrue(document.has("anyOfObject"));
                    JSONObject object = document.getJSONObject("anyOfObject");
                    Assert.assertEquals(object.length(), 1);
                    Assert.assertTrue(object.has("prop"));
                    Assert.assertEquals(object.get("prop"), valueInObject);
                }
            }
        }

        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testGeneratorOneOf() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("oneOf.json", true);
        ExplorationGenerator generator = new DefaultExplorationGenerator(4, 4);
        Iterator<JSONObject> iterator = generator.createIterator(schema);

        for (boolean properties : List.of(true, false)) {
            for (int sizeArray : List.of(4, 0, 1)) {
                Assert.assertTrue(iterator.hasNext());
                final JSONObject document = iterator.next();
                Assert.assertEquals(document.length(), 2);

                Assert.assertTrue(document.has("oneOfArray"));
                JSONArray array = document.getJSONArray("oneOfArray");
                Assert.assertEquals(array.length(), sizeArray);
                for (int i = 0; i < array.length(); i++) {
                    Assert.assertEquals(array.get(i), AbstractConstants.stringConstant);
                }

                Assert.assertTrue(document.has("oneOfObject"));
                JSONObject object = document.getJSONObject("oneOfObject");
                if (properties) {
                    Assert.assertEquals(object.length(), 2);
                    Assert.assertTrue(object.has("prop1"));
                    Assert.assertEquals(object.get("prop1"), AbstractConstants.stringConstant);
                    Assert.assertTrue(object.has("prop2"));
                    Assert.assertEquals(object.get("prop2"), AbstractConstants.stringConstant);
                }
            }
        }

        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testGeneratorNot() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("notSchema.json", true);
        ExplorationGenerator generator = new DefaultExplorationGenerator(4, 4);
        Iterator<JSONObject> iterator = generator.createIterator(schema);

        Assert.assertTrue(iterator.hasNext());
        JSONObject document = iterator.next();
        Assert.assertEquals(document.length(), 1);
        Assert.assertTrue(document.has("subObject"));
        JSONObject subObject = document.getJSONObject("subObject");
        Assert.assertEquals(subObject.length(), 2);
        Assert.assertTrue(subObject.has("value"));
        Assert.assertEquals(subObject.get("value"), AbstractConstants.stringConstant);
        Assert.assertTrue(subObject.has("empty"));
        Assert.assertEquals(subObject.get("empty"), true);

        Assert.assertTrue(iterator.hasNext());
        document = iterator.next();
        Assert.assertEquals(document.length(), 1);
        Assert.assertTrue(document.has("subObject"));
        subObject = document.getJSONObject("subObject");
        Assert.assertEquals(subObject.length(), 2);
        Assert.assertTrue(subObject.has("value"));
        Assert.assertEquals(subObject.get("value"), AbstractConstants.stringConstant);
        Assert.assertTrue(subObject.has("empty"));
        Assert.assertEquals(subObject.get("empty"), false);

        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testGeneratorRecursiveList() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("recursiveList.json", false);
        ExplorationGenerator generator = new DefaultExplorationGenerator(4, 4);
        Iterator<JSONObject> iterator = generator.createIterator(schema);

        for (int nRecursion = 0; nRecursion < 20; nRecursion++) {
            Assert.assertTrue(iterator.hasNext());
            JSONObject document = iterator.next();
            checkRecursiveList(document, nRecursion, false);
        }

        // There is an infinite number of documents
        Assert.assertTrue(iterator.hasNext());
    }

    @Test
    public void testGeneratorRecursiveListBoundedDepth()
            throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("recursiveList.json", false);
        ExplorationGenerator generator = new DefaultExplorationGenerator(4, 4);
        Iterator<JSONObject> iterator = generator.createIterator(schema, 40);

        for (int nRecursion = 0; nRecursion < 20; nRecursion++) {
            Assert.assertTrue(iterator.hasNext());
            JSONObject document = iterator.next();
            checkRecursiveList(document, nRecursion, false);
        }

        Assert.assertTrue(iterator.hasNext());
        checkRecursiveList(iterator.next(), 20, true);

        // There is a finite number of documents thanks to maximal depth
        Assert.assertFalse(iterator.hasNext());
    }

    private void checkRecursiveList(JSONObject document, int wantedDepth, boolean reachedMaxDepth) {
        if (wantedDepth != 0) {
            Assert.assertEquals(document.length(), 2);
        } else {
            Assert.assertEquals(document.length(), 1);
        }

        Assert.assertTrue(document.has("name"));
        Assert.assertEquals(document.get("name"), AbstractConstants.stringConstant);

        if (wantedDepth != 0) {
            Assert.assertTrue(document.has("list"));
            JSONArray array = document.getJSONArray("list");
            if (wantedDepth == 1 && reachedMaxDepth) {
                Assert.assertEquals(array.length(), 0);
            } else {
                Assert.assertEquals(array.length(), 1);
                checkRecursiveList(array.getJSONObject(0), wantedDepth - 1, reachedMaxDepth);
            }
        }
    }

    @Test
    public void testGeneratorWithConst() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchema schema = loadSchema("withConst.json", true);
        ExplorationGenerator generator = new DefaultExplorationGenerator(4, 4);
        Iterator<JSONObject> iterator = generator.createIterator(schema);

        for (int idDocument = 0; idDocument < 6; idDocument++) {
            Assert.assertTrue(iterator.hasNext());
            JSONObject document = iterator.next();
            Assert.assertEquals(document.length(), 12);

            Assert.assertTrue(document.has("positiveConstBoolean"));
            Assert.assertEquals(document.get("positiveConstBoolean"), true);

            Assert.assertTrue(document.has("negativeConstBoolean"));
            Assert.assertEquals(document.get("negativeConstBoolean"), false);

            Assert.assertTrue(document.has("positiveConstInteger"));
            Assert.assertEquals(document.get("positiveConstInteger"), AbstractConstants.integerConstant);

            Assert.assertTrue(document.has("negativeConstInteger"));
            Assert.assertEquals(document.get("negativeConstInteger"), AbstractConstants.integerConstant);

            Assert.assertTrue(document.has("positiveConstNumber"));
            Assert.assertEquals(document.get("positiveConstNumber"), AbstractConstants.numberConstant);

            Assert.assertTrue(document.has("negativeConstNumber"));
            Assert.assertEquals(document.get("negativeConstNumber"), AbstractConstants.numberConstant);

            Assert.assertTrue(document.has("positiveConstString"));
            Assert.assertEquals(document.get("positiveConstString"), AbstractConstants.stringConstant);

            Assert.assertTrue(document.has("negativeConstString"));
            Assert.assertEquals(document.get("negativeConstString"), AbstractConstants.stringConstant);

            Assert.assertTrue(document.has("positiveConstObject"));
            JSONObject positiveConstObject = document.getJSONObject("positiveConstObject");
            JSONObject expectedPositiveConstObject = new JSONObject(
                    "{\"test\": true, \"int\": \"\\" + AbstractConstants.integerConstant + "\"}");
            Assert.assertTrue(positiveConstObject.similar(expectedPositiveConstObject));

            Assert.assertTrue(document.has("negativeConstObject"));
            // Due to the abstracted values, we can not explicitly test that the generated
            // value is correct

            Assert.assertTrue(document.has("positiveConstArray"));
            JSONArray positiveConstArray = document.getJSONArray("positiveConstArray");
            JSONArray expectedPositiveConstArray = new JSONArray("[true, true, false]");
            Assert.assertTrue(positiveConstArray.similar(expectedPositiveConstArray));

            Assert.assertTrue(document.has("negativeConstArray"));
        }

        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testInvalidGeneratorConstPrimitives()
            throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        runInvalidGenerator(loadSchema("withConstInteger.json", true), 1, 1);
        runInvalidGenerator(loadSchema("withConstNumber.json", true), 1, 1);
        runInvalidGenerator(loadSchema("withConstString.json", true), 1, 1);
        runInvalidGenerator(loadSchema("withConstBoolean.json", true), 1, 1);
    }

    private void runInvalidGenerator(JSONSchema schema, int maxProperties, int maxItems) throws JSONSchemaException {
        ExplorationGenerator generator = new DefaultExplorationGenerator(maxProperties, maxItems);
        Iterator<JSONObject> iterator = generator.createIterator(schema, 2, true);
        Validator validator = new DefaultValidator();

        boolean atLeastOneValid = false;
        boolean atLeastOneInvalid = false;

        while (iterator.hasNext()) {
            JSONObject document = iterator.next();
            boolean valid = validator.validate(schema, document);
            atLeastOneValid = atLeastOneValid || valid;
            atLeastOneInvalid = atLeastOneInvalid || !valid;
        }

        Assert.assertTrue(atLeastOneValid);
        Assert.assertTrue(atLeastOneInvalid);
    }

    @Test
    public void testInvalidGeneratorConstObject()
            throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        runInvalidGenerator(loadSchema("withConstObject.json", true), 3, 1);
    }

    @Test
    public void testInvalidGeneratorConstArray() throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        runInvalidGenerator(loadSchema("withConstArray.json", true), 1, 3);
    }
}