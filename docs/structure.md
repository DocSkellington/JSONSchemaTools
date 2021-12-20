---
title: Structure and abstractions
layout: pageMath
---

* 
{:toc}

The project is split into three modules:
  1. Implementations of tools around JSON Schemas.
  2. The generator.
  3. The validator.

## Tools
First, let us discuss the implementations of tools that are used in both the generator and the validator.
More precisely, we need to introduce how we handle the keywords that are defined as boolean operations, and how we handle references inside a schema.

### Boolean keywords
#### "allOf"
To handle "allOf" keywords, we consider the array as a big conjunction and we merge together all the terms in the conjunction.
For instance, the schema
```json
{
    "type": "integer",
    "multipleOf": 3,
    "allOf": [
        {
            "multipleOf": 5
        },
        {
            "multipleOf": 2
        }
    ]
}
```
gets reduced to
```json
{
    "type": "integer",
    "multipleOf": 30
}
```
because we want the integer to be divisible by 2, 3, and 5 at the same time.

#### "anyOf"
Since an "anyOf" is equivalent to the OR operation, we do not have anything to do when retrieving the value.
Indeed, what must be done with the array depends on whether the generator or the validator is running.
Therefore, more information is given later.

#### "not"
Since our goal is to get as much constraints as possible for the generator, we handle "not" by propagating it as much as possible.
For instance, let us consider the following schema:

```json
{
    "type": "object",
    "not": {
        "properties": {
            "key": {
                "type": "integer"
            }
        },
        "minProperties": 2
    }
}
```
First, we apply the "not" over the conjunction induced by the sub-schema.
That is, we transform the "NOT AND" into an "OR NOT":
```json
{
    "type": "object",
    "anyOf": [
        {
            "not": {
                "properties": {
                    "key": {
                        "type": "integer"
                    }
                }
            }
        },
        {
            "not": {
                "minProperties": 2
            }
        }
    ]
}
```
It is then easier to apply the "not" over each item.
Typically, the properties imposing a minimum number of values to be present become a limit over the maximum number of values.
In the case of "properties" or "items", the "not" is propagated even further.
In the end, we obtain the following schema
```json
{
    "type": "object",
    "anyOf": [
        {
            "properties": {
                "key": {
                    "not": {
                        "type": "integer"
                    }
                }
            }
        },
        {
            "maxProperties": 1
        }
    ]
}
```
Notice that we can not propagate the "not" over "type" for now.
When the generate or the validator will process the key "key", the "not" will be taken into account to restrict the range of allowed types.

#### "oneOf"
The "oneOf" keyword is basically a XOR operation over the elements in the array.
Let us denote the XOR operation by $$\oplus$$ and assume the array contains the elements $$A, B$$, and $$C$$.
Then,

$$
A \oplus B \oplus C
\equiv (A \land \neg B \land \neg C) \lor (\neg A \land B \land \neg C) \lor (\neg A \land \neg B \land C).
$$

That is, we can transform the "oneOf" into an "anyOf" containing "allOf".
For instance, the schema
```
{
    "oneOf": [
        A,
        B,
        C
    ]
}
```
(where A, B, and C are valid JSON schemas) becomes
```
{
    "any": [
        {
            "allOf": [
                A,
                {"not": B},
                {"not": C}
            ]
        },
        {
            "allOf": [
                {"not": A},
                B,
                {"not": C}
            ]
        },
        {
            "allOf": [
                {"not": A},
                {"not": B},
                C
            ]
        }
    ]
}
```
We then apply our procedures over the "anyOf", "allOf", and "not" keywords to ensure that all constraints are correctly taken into account.

### References
When encountering the keyword "$ref", the implementation needs to load the required schema.
In the case where that other schema is in a completely different file, we must open this new file and read the schema in it.
In the case where the schema is actually a sub-schema of the current file, we simply have to "jump" to that part of the file.
In both cases, this operation is invisible for the generator and the validator.
That is, whenever a call to a function retrieving a sub-schema (so, the boolean operators, "properties", "items", and so on), the function immediately returns the referenced schema.
In other words, the generator and the validator do not see the "$ref" keywords.
This allows the generation and validation to always behave the same way, regardless of "$ref".

### Abstractions induced by how the keywords are handled
The way "not" and "allOf" (for instance) are handled already abstracts the schema. Indeed, consider the following schema (let us called it A)
```json
{
    "type": "integer",
    "allOf": [
        {
            "not": {
                "minItems": 5
            }
        },
        {
            "not": {
                "maxItems": 3
            }
        }
    ]
}
```
and apply our operations for "not" to obtain
```json
{
    "type": "integer",
    "allOf": [
        {
            "maxItems": 4
        },
        {
            "minItems": 4
        }
    ]
}
```
Finally, apply the "allOf" to get the schema B
```json
{
    "type": "integer",
    "maxItems": 4,
    "minItems": 4
}
```

It is possible to generate documents that are valid against B (since "minItems" and "maxItems" do not impose any kind of constraints upon integers).
However, all of these generate documents are invalid against A since the sub-schema
```json
{
    "not": {
        "minItems": 5
    }
}
```
returns false for any document (again, as "minItems" do not impose restrictions upon integers).

Thus, the way we handle keywords when retrieving the constraints from the schema abstracts the schema.
It is to be noted that these abstraction only make the schema accepts more documents.

### Implementation
The principles described here are implemented in three classes:
  1. `be.ac.umons.jsonschematools.JSONSchemaStore` handles reading a schema from a file.
  2. `be.ac.umons.jsonschematools.JSONSchema` implements the operations to manipulate a schema (i.e., retrieving constraints, applying the boolean operations, and so on).
  3. `be.ac.umons.jsonschematools.Keys` is an helper class implementing the actual merging of values. This class is used by `JSONSchema` to split the complexity into more readable parts.

## Generator
Thanks to our algorithms to apply the boolean operations, the idea behind the generator is straightforward.
Let $$schema$$ be the current schema the generator is processing.
  1. Let $$allOf$$ be the JSON schema induced by the "allOf" keyword of $$schema$$.
  2. Let $$anyOfList$$ be the list of JSON schemas induced by the "anyOf" keyword of $$schema$$.
  3. Let $$oneOfList$$ be the list of JSON schemas induced by the "oneOf" keyword of $$schema$$.
  4. Let $$notList$$ be the list of JSON schemas induced by the "not" keyword of $$schema$$.
  5. Let $$anyOf$$ be a JSON schema randomly selected in $$anyOfList$$.
  6. Let $$oneOf$$ be a JSON schema randomly selected in $$oneOfList$$.
  7. Let $$not$$ be a JSON schema randomly selected in $$notList$$.
  8. Let $$schema_{final}$$ be the JSON schema obtained by merging $$schema, allOf, anyOf, oneOf$$, and $$not$$ (as explained in [allOf](#allof) above).
  9. Generate a value according to $$schema_{final}$$.

In the implementation, generating a value according to $$schema_{final}$$ is delegated to handlers (one by type allowed in a JSON schema).
Default handlers are provided but can be replaced by your own handlers, if needed.
We now explain how our handlers work.

### Generating booleans
Generating a boolean is immediate.
We simply toss a coin with even probabilities and return whether the result is a tail.
That is, we return `true` with probability $$0.5$$.

### Generating numbers, integers, strings, and enumeration values
For numbers, integers, strings, and enumeration values, the default handlers return abstract values.
More precisely, the following table indicates which value is returned, depending on the type.

| Type        | Value |
|-------------|-------|
| Number      | \D    |
| Integer     | \I    |
| String      | \S    |
| Enumeration | \E    |

The reasons for these abstractions are explained on [the home page](index#goal-of-the-project).
An implication of this choice is that keywords that impose constraints on numbers, integers, strings, or enumerations are ignored, such as "multipleOf", "minimum", "maximum", and so on.

### Generating arrays
To generate arrays, we must take into account the minimal and the maximal number of items.
If "minItems" is not present, it is assumed to be zero.
Likewise, if "maxItems" is not present, it is assumed to be the maximum value an integer can take[^1].
Then, the actual number of items to generate is randomly selected between those bounds.
Finally, a correct number of values are recursively generated (i.e., we go back to the algorithm described [above](#generator)).

[^1]: It is possible to set any value as the default maximal number of items/properties, thanks to the constructor of the class.

### Generating objects
Generating an object is very similar to generating an array.
The main difference comes from the fact that properties can be made required in the resulting object.
Thus, the generation must always generate those properties.
Similarly to generating arrays, "minProperties" and "maxProperties" can restrict the number of generated properties.
If they are omitted, they are considered as 0 and the maximal value an integer can take[^1], respectively.

Since a property is defined by a JSON schema, generating appropriate values is done recursively.

## Validator
The validator is even more straightforward.
Indeed, we just need to apply the boolean operators and check whether the final result is true.
For instance, if we have an "anyOf" keyword, we simply check whether at least one of the schemas in the array is valid.

Like for the generator, one handler per type is defined.
We provide default handlers, operating on the abstractions defined [above](#generator).