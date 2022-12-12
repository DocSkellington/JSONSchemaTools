---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults

layout: page
title: Generator and validator for JSON schemas, with abstract values
---

This documentation assumes the reader is familiar with JSON documents and JSON schemas.
See [json.org] and [json-schema.org] for more information.

See the [API documentation](api/apidocs/index.html) if you are interested in the classes and methods.

## Motivation
This project is part of a collection of projects.
The final goal is to be able to automatically construct an automaton that approximates a JSON schema, and use it to validate documents.
An automaton accepting the same set of documents than a given schema is useful in the case of streaming large documents.
A classical tool to validate a document must, in the worst case, wait for the whole document before processing it, while an automaton can process it in an efficient way symbol by symbol.
A similar use-case on XML documents has already been studied.[^1]

The other projects in the collection are:
  - [AutomataLib's fork](https://github.com/DocSkellington/automatalib): implements specific automata.
  - [LearnLib's fork](https://github.com/DocSkellington/learnlib): implements learning algorithms for automata.
  - [Validating JSON documents with learned VPAs](https://github.com/DocSkellington/ValidatingJSONDocumentsWithLearnedVPA/): combines the three other projects to learn automata from a schema, and use the resulting automata to validate documents. Also provides benchmarks for both learning and validation.

This project aims to provide tools to manipulate JSON schemas and is split into three modules:
  1. The [`core` module](core.html) contains an implementation of tools to process and navigate a JSON schema.
    For instance, a schema can contain the key `$ref` whose value is a path to some (sub-)schema.
    The core module handles the references in a transparent way for the user, i.e., the pointed (sub-)schema is directly used.
    The user does not have to worry about handling references.
    The module also processes the Boolean operations (`allOf`, `anyOf`, `oneOf`, and `not`) to ease the implementation of the next two modules.
  2. The [`validator` module](validator.html) contains an implementation that check whether a provided JSON document is correct for a schema.
    The document must satisfy all the constraints described in the schema to be considered valid.
  3. The [`generator` module](generator.html) contains an implementation of two generators that can produce (valid or invalid) documents for a schema.

### Abstract values
In our approach, we want to abstract the values in a document in order to reduce the number of different symbols we must consider.
For instance, instead of considering all figures 0 to 9 and concatenations of these symbols to obtain integers, we assume that integers are always represented as `\I`.
The following table provides the abstracted values per type.

| Type        | Value |
|-------------|-------|
| Number      | d     |
| Integer     | i     |
| String      | s     |
| Enumeration | e     |

See the documentation of each module for more details.

## Important pages
  1. [Documentation of the core module](core.html)
  2. [Documentation of the generator module](generator.html)
  3. [Documentation of the validator module](validator.html)
  4. [List of partially supported or unsupported keywords](keywords.html)
  5. [API documentation](api/apidocs/index.html)

[json.org]: https://www.json.org/json-en.html
[json-schema.org]: https://json-schema.org/
[^1]: Kumar, V., Madhusudan, P., and Viswanathan, M., 2007, Visibly pushdown automata for streaming XML. In Proceedings of the 16th International Conference on World Wide Web (pp. 1053–1062); and Neider, D., 2008. Learning Automata for Streaming XML Documents. In Informatiktage (pp.&nbsp;23-26).