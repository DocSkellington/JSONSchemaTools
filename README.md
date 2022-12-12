# Validation and generation of documents according to a JSON schema
This documentation assumes the reader is familiar with JSON documents and JSON schemas.
See [json.org](https://www.json.org/json-en.html) and [json-schema.org](https://json-schema.org/) for more information.

See the [full documentation](https://docskellington.github.io/JSONSchemaTools/) for more details about the project, and the [API documentation](https://docskellington.github.io/JSONSchemaTools/api/apidocs/index.html) for the classes and methods.

## Motivation
This project is part of a collection of projects.
The final goal is to be able to automatically construct an automaton that approximates a JSON schema, and use it to validate documents.
An automaton accepting the same set of documents than a given schema is useful in the case of streaming large documents.
A classical tool to validate a document must, in the worst case, wait for the whole document before processing it, while an automaton can process it in an efficient way symbol by symbol.
A similar use-case on XML documents has already been studied in Kumar, V., Madhusudan, P., and Viswanathan, M., 2007. *Visibly pushdown automata for streaming XML*. In Proceedings of the 16th International Conference on World Wide Web (pp. 1053–1062); and Neider, D., 2008. *Learning Automata for Streaming XML Documents*. In Informatiktage (pp.&nbsp;23-26).

The other projects in the collection are:
  - [AutomataLib's fork](https://github.com/DocSkellington/automatalib): implements specific automata.
  - [LearnLib's fork](https://github.com/DocSkellington/learnlib): implements learning algorithms for automata.
  - [Validating JSON documents with learned VPAs](https://github.com/DocSkellington/ValidatingJSONDocumentsWithLearnedVPA/): combines the three other projects to learn automata from a schema, and use the resulting automata to validate documents. Also provides benchmarks for both learning and validation.

This project aims to provide tools to manipulate JSON schemas and is split into three modules:
  1. The `core` module contains an implementation of tools to process and navigate a JSON schema.
    For instance, a schema can contain the key `$ref` whose value is a path to some (sub-)schema.
    The core module handles the references in a transparent way for the user, i.e., the pointed (sub-)schema is directly used.
    The user does not have to worry about handling references.
    The module also processes the Boolean operations (`allOf`, `anyOf`, `oneOf`, and `not`) to ease the implementation of the next two modules.
  2. The `validator` module contains an implementation that check whether a provided JSON document is correct for a schema.
    The document must satisfy all the constraints described in the schema to be considered valid.
  3. The `generator` module contains an implementation of two generators that can produce (valid or invalid) documents for a schema.

### Abstract values
In our approach, we want to abstract the values in a document in order to reduce the number of different symbols we must consider.
For instance, instead of considering all figures 0 to 9 and concatenations of these symbols to obtain integers, we assume that integers are always represented as `\I`.
The following table provides the abstracted values per type.

| Type        | Value |
|-------------|-------|
| Number      | \D    |
| Integer     | \I    |
| String      | \S    |
| Enumeration | \E    |

See the documentation of each module for more details.

## How to use
To locally install the project, run the following command at the root of the repository:
```bash
mvn install
```

This will download the required dependencies from Maven's repositories.

Then, to use the modules, add the following code in your `pom.xml`:
```XML
<dependencies>
  ...
  <dependency>
    <groupId>be.ac.umons.jsonschematools</groupId>
    <artifactId>jsonschematools-core</artifactId>
    <version>3.0</version>
  </dependency>
  <dependency>
    <groupId>be.ac.umons.jsonschematools</groupId>
    <artifactId>jsonschematools-validator</artifactId>
    <version>3.0</version>
  </dependency>
  <dependency>
    <groupId>be.ac.umons.jsonschematools</groupId>
    <artifactId>jsonschematools-generator</artifactId>
    <version>3.0</version>
  </dependency>
  ...
</dependencies>
```

To generate a document for any schema (with supported keywords), see the [`be.ac.umons.jsonschematools.generator.IGenerator` interface and its implementing classes documentation](https://docskellington.github.io/JSONSchemaTools/api/apidocs/be/ac/umons/jsonschematools/generator/IGenerator.html).
To validate a document against a schema, see the [`be.ac.umons.jsonschematools.validator.Validator` class documentation](https://docskellington.github.io/JSONSchemaTools/api/apidocs/be/ac/umons/jsonschematools/validator/Validator.html).
Moreover, the unit tests of each module provide examples on how to configure and use the different parts.

## How to extend
If you desire the modify the way integers (for instance) are handled for the validator, you simply have to create a new class that implements the `Handler` interface from the package `be.ac.umons.jsonschematools.validator.handlers`.
Then, when creating an instance of the validator, you can provide your handler.
Handlers for both generators can be created in a similar fashion.

If you want to add support for more keywords, you can modify the class `be.ac.umons.jsonschematools.MergeKeys`.
More precisely, the class dictates the keywords that must be kept when encountered and how to merge them when seen inside `allOf`, `anyOf`, `oneOf` or `not`.
See [the core module documentation for more information](https://docskellington.github.io/JSONSchemaTools/core.html).

## Known bugs
  - A double negation (a "not" inside a "not") is incorrectly handled by the generators.
    With regards to our [learning and validation benchmarks](https://github.com/DocSkellington/ValidatingJSONDocumentsWithLearnedVPA/), this bug does not influence the results as this situation does not arise in any of the considered schemas.
    This also means that a "oneOf" inside a "not" does not work as intended.
    See [issue #1](https://github.com/DocSkellington/JSONSchemaTools/issues/1).

## License
The code of the library is distributed under Apache License 2.0.

## Maintainer
* Gaëtan Staquet, F.R.S.-FNRS, University of Mons, and University of Antwerp