---
title: Generator module
layout: pageMath
---

* 
{:toc}

A *generator* is a tool to automatically produce (valid or invalid) documents from a schema.
As some keywords make the generation process hard, not all keywords are supported.
See the [appropriate page](keywords.html) for a list of unsupported keywords.

## Main algorithm
Thanks to our algorithms to apply the boolean operations (see the [core module documentation](core.html)), the idea behind the generator is straightforward.
Let $$schema$$ be the current schema the generator is processing.
  1. Let $$allOf$$ be the JSON schema induced by the `allOf` keyword of $$schema$$.
  2. Let $$anyOfList$$ be the list of JSON schemas induced by the `anyOf` keyword of $$schema$$.
  3. Let $$oneOfList$$ be the list of JSON schemas induced by the `oneOf` keyword of $$schema$$.
  4. Let $$notList$$ be the list of JSON schemas induced by the `not` keyword of $$schema$$.
  5. Let $$anyOf$$ be a JSON schema selected in $$anyOfList$$.
  6. Let $$oneOf$$ be a JSON schema selected in $$oneOfList$$.
  7. Let $$not$$ be a JSON schema selected in $$notList$$.
  8. Let $$schema_{final}$$ be the JSON schema obtained by merging $$schema, allOf, anyOf, oneOf$$, and $$not$$ (as explained in [the core documentation](core.html#allof)).
  9. Generate a value according to $$schema_{final}$$.

We implemented two different generators:
  1. A generator that selects $$anyOf, oneOf$$, and $$not$$ randomly.
  2. A generator that exhaustively considers every possibility.

Moreover, each generator is able to produce documents that are valid or invalid, i.e., a document that does not satisfy the schema.

In the implementation, generating a value according to $$schema_{final}$$ is delegated to handlers (one by type allowed in a JSON schema).
Default handlers are provided but can be replaced by your own handlers, if needed (see the API documentation [for the random generator](api/apidocs/be/ac/umons/jsonschematools/generator/random/handlers/package-summary.html) and [for the exhaustive generator](api/apidocs/be/ac/umons/jsonschematools/generator/exploration/handlers/package-summary.html)).

We first give the main idea behind the random generator, followed by the exhaustive generator.
As the handlers behave in similar way for both generators, we conclude this section with an explanation of the handlers, i.e., the parts that actually generate the values according $$schema_{final}$$.

## Generating documents randomly
As mentioned before, the first generator randomly selects $$anyOf, oneOf$$, and $$not$$ in the appropriate lists.
In order to have reproducible results, the seed can be fixed.

As it is possible that the selection lead to a schema that considers every possible document as invalid, we select the values of $$anyOf, oneOf$$, and $$not$$ as follows:
  1. We generate a random permutation $$indices_{anyOf}$$ of numbers between 0 and the size of $$anyOf$$.
  1. We generate a random permutation $$indices_{oneOf}$$ of numbers between 0 and the size of $$anyOf$$.
  1. We generate a random permutation $$indices_{not}$$ of numbers between 0 and the size of $$anyOf$$.
  2. We iterate over each permutation and take the corresponding element in the lists.

## Generating documents by exhaustively exploring every possibility
The second generator produces every possible document described by a schema, by exhaustively taking all the choices one by one.
Each time a choice must be made (for instance, to select one or multiple elements in a list), a bitset is created.
By manipulating the bits of this bitset, we thus can exhaustively encode every possibility for that choice.

The first time we generate a document for a schema, we create and store a sequence of choices up to the point where we obtain a document.
To generate the next document, we repeat all the choices we made, until we reach the last point in the sequence where a new, unseen yet possibility can be taken.
That is, the sequence of choices is used to be able to reproduce the same choices until we want to deviate.

## Default handlers
We now explain how the default handlers work.
The differences between the two types of generators are given in tabs.

### Generating booleans
Generating a boolean is immediate.

{% tabs boolean %}
{% tab boolean Random %}
We simply toss a coin with even probabilities and return whether the result is a tail.
That is, we return `true` with probability $$0.5$$.
{% endtab %}
{% tab boolean Exhaustive %}
We first generate `true`, then `false`.
If one of the value is forbidden by the schema, it is simply skipped.
{% endtab %}
{% endtabs %}

### Generating numbers, integers, strings, and enumeration values
For numbers, integers, strings, and enumeration values, the default handlers return abstract values.
More precisely, the following table indicates which value is returned, depending on the type.

| Type        | Value |
|-------------|-------|
| Number      | \D    |
| Integer     | \I    |
| String      | \S    |
| Enumeration | \E    |

The reasons for these abstractions are explained on [the home page](index#abstract-values).
An implication of this choice is that keywords that impose constraints on numbers, integers, strings, or enumerations are ignored, such as `multipleOf`, `minimum`, `maximum`, and so on.

### Generating arrays
To generate arrays, we must take into account the minimal and the maximal number of items.
If `minItems` is not present, it is assumed to be zero.
Likewise, if `maxItems` is not present, it is assumed to be the maximum value an integer can take.[^1]

{% tabs array %}
{% tab array Random %}
The actual number of items to generate is randomly selected between those bounds.
{% endtab %}
{% tab array Exhaustive %}
We consider every possible size between those bounds.
{% endtab %}
{% endtabs %}

Finally, once the size is fixed, the values are generated by recursively calling the global procedure (i.e., we go back to the [main algorithm](#main-algorithm)).

[^1]: It is possible to set any value as the default maximal number of items/properties, thanks to the constructor of the class.

### Generating objects
Generating an object is very similar to generating an array.
The main difference comes from the fact that properties can be made required in the resulting object.
Thus, the generation must always generate those properties.
Similarly to generating arrays, `minProperties` and `maxProperties` can restrict the number of generated properties.
If they are omitted, they are considered as 0 and the maximal value an integer can take[^1], respectively.

Since a property is defined by a JSON schema, generating appropriate values is done recursively.

#### Additional and pattern properties
On top of the `properties` field, schemas can define `additionalProperties` and `patternProperties` constraints.
The value for the first key is a JSON schema, while it is an object for the second.
While the values are generated as explained in the rest of this document, the keys for these two keywords are abstracted as follows:

  * For `additionalProperties`, the key is always `\S` (i.e., the abstract representation of a string).
    The implication of this is that at most one additional property can be generated by object.
    Of course, it may happen that no additional properties are generated if the schema does not allow it, or if the randomness decides so.
  * For `patternProperties`, the pattern is directly used as a key.
    That is, the generator does **not** generate strings that match the pattern.
    This means that the keyword is **not** fully supported, as it is not possible to merge constraints from `properties` and  `patternProperties`, for instance.

{% include note.html content="The way these two keys are handled implies an abstraction of the schema. Indeed, at most one additional property and at most one pattern property is generated. If we want to follow the schema's syntax completely, it should be possible to generate multiple such properties." %}

{% tabs properties %}
{% tab properties Random %}
If the schema accepts additional properties and if the maximal number of properties is not yet reached, we flip a coin to decide whether we produce such a value.
The same principle applies for `patternProperties`.
{% endtab %}
{% tab properties Exhaustive %}
If the schema accepts additional properties and if the maximal number of properties is not yet reached, we first generate a document without an additional property, and then one with an additional property.
The same principle applies for `patternProperties`.
{% endtab %}
{% endtabs %}

## Generating invalid documents
Given a JSON schema, we want to be able to produce documents that are not valid with regards to the schema.

### Naive generator
Producing random words over the alphabet has a high probability of producing an invalid document, since the probability of it not even being a JSON document at all is high.
However, it is not useful with regards to our final goal (which is learning an automaton) as we would like invalid documents that still follow the same general structure described by the schema.

### Almost invalid generator
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

#### Handlers
Due to the structure of the generator, it is possible to permit only one type of values to be invalid.
For instance, we could decide that only objects may not be correctly generated, or that the only deviation tolerated is selecting the wrong type.

That is, we can be precise in how we generate invalid documents.

### Special case: empty document
If the empty document is not accepted by the generator, it should be used as a potential invalid document.
This is not checked by the generator.
That is, it is up to the caller to decide whether the empty document should be explicitly handled.

## Implementation
To ease the use of the generators, they share the same interface: [`be.ac.umons.jsonschematools.generator.IGenerator`](api/apidocs/be/ac/umons/jsonschematools/IGenerator.html).
More precisely, this interface defines methods to create an iterator.
Each document is then generated lazily, i.e., when requested by the iterator.
Whether to generate valid or invalid documents is set at the creation of the iterator.

{% tabs implementation %}
{% tab implementation Random %}
The random generator's classes are contained in the package [`be.ac.umons.jsonschematools.generator.random`](api/apidocs/be/ac/umons/jsonschematools/generator/random/package-summary.html).

This generator throws an exception if it was not possible to generate a document, due to all the random selections leading to a schema rejecting everything.
{% endtab %}
{% tab implementation Exhaustive %}
The exhaustive generator's classes are contained in the package [`be.ac.umons.jsonschematools.generator.exploration`](api/apidocs/be/ac/umons/jsonschematools/generator/exploration/package-summary.html).

The classes [`be.ac.umons.jsonschematools.generator.exploration.Choice`](api/apidocs/be/ac/umons/jsonschematools/generator/exploration/Choice.html) and [`be.ac.umons.jsonschematools.generator.exploration.ChoicesSequence`](api/apidocs/be/ac/umons/jsonschematools/generator/exploration/ChoicesSequence.html) store, respectively, a choice (for instance, an index in a list), and the sequence of all choices starting from the root of the schema.
{% endtab %}
{% endtabs %}