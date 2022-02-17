# Generating invalid documents

Given a JSON schema, we want to be able to produce documents that are not valid with regards to the schema.

## Naive generator

Producing random words over the alphabet has a high probability of producing an invalid document, since the probability of it not even being a JSON document at all is high.
However, it is not useful with regards to our goal (which is learning an automaton).

## Almost invalid generator

For learning purposes, we want to generate documents that are "almost valid", i.e., they are invalid but follow the same global structure as a valid document.
In other words, we want documents that can be used for mutant testing.

Here are the main ideas behind the generator, when asked to produce an invalid document:
  * Sometimes, it selects the wrong type when generating a value (for instance, generate a boolean instead of a string).
  * If `const` is set, it sometimes generates something else.
  * If we have `"not": {"const": A}`, it sometimes return `A`.
  * For objects:
    * It sometimes does not generate one of the required properties.
    * If `additionalProperties` is false, it sometimes generates an additional property anyway.
    * If `minProperties` and/or `maxProperties` are set, it sometimes does not respect these constraints.
  * For arrays:
    * If `minItems` and/or `maxItems` are set, it sometimes does not respect these constraints.

Note that the generator will not check whether the produced document is valid or invalid.
It is up to the caller to check and repeat the process, if needed.

### Handlers

Due to the structure of the generator, it is possible to permit only one type of values to be invalid.
For instance, we could decide that only objects may not be correctly generated, or that the only deviation tolerated is selecting the wrong type.

That is, we can be precise in how we generate invalid documents.

## Special case: empty document

If the empty document is not accepted by the generator, it should be used as a potential invalid document.
This is not checked by the generator.
That is, it is up to the caller to decide whether the empty document should be explicitly handled.