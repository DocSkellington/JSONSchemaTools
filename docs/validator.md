---
title: Validator module
layout: pageMath
---

* 
{:toc}

A *validator* is a tool that can decide whether a document is valid for a schema.
That is, a validator checks if the document satisfies the constraints given by the schema.

## Main algorithm
Given the tools of the [core module](core.html) and the abstractions used by the [generators](generator.html), the principles behind our validator are straightforward.
We simply apply the Boolean operations in a classical way, and check that each part of the document satisfies a corresponding part in the schema.
For instance, if we have an `anyOf` keyword, we simply check whether at least one of the schemas in the array is valid.

Using the same principle as the generators, the validator relies on several handlers, one per type of value.
We provide default handlers, operating on the abstractions defined in the [generator module documentation](generator.html).
One can provide new handlers, if needed.

## Implementation
The validator is implemented in the package [`be.ac.umons.jsonschematools.validator`](api/apidocs/be/ac/umons/jsonschematools/validator/package-summary.html).
The default handlers are provided in the package [`be.ac.umons.jsonschematools.validator.handlers`](api/apidocs/be/ac/umons/jsonschematools/validator/handlers/package-summary.html).