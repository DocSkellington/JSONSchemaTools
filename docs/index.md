---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults

layout: home
title: Generator and validator for JSON schemas, with abstract values
---

## Important pages
  1. [Structure of the project and abstraction of the documents](structure.html)
  2. [List of unsupported keywords and remaining tasks](todos.html)

This documentation assumes the reader is familiar with JSON documents and JSON schemas.
See [json.org] and [json-schema.org] for more information.

## Goal of the project
The aim of the project is to provide a generator and a validator for JSON schemas that can be used in automata learning experiments.
An automaton that accepts the same set of documents than a given schema can be used in the case of streaming documents to validate the document without waiting for the whole document.
See [^1] for a similar use-case on XML documents.

In our approach, we want to abstract the values in a document in order to reduce the number of different symbols we must consider.
For instance, instead of considering all figures 0 to 9 and concatenations of these symbols to obtain integers, we assume that integers are always represented as `\I`.
More details are given in [Structure of the project and abstraction of the documents](structure.html).

[json.org]: https://www.json.org/json-en.html
[json-schema.org]: https://json-schema.org/
[^1]: Neider, D., 2008. Learning Automata for Streaming XML Documents. In Informatiktage (pp. 23-26).