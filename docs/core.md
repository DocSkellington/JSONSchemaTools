---
title: Core module
layout: pageMath
---

* 
{:toc}

Let us discuss the tools that are used to manipulate a JSON schema, in order to ease the implementation of the generators and the validator.
More precisely, we need to introduce how we handle the keywords that are defined as Boolean operations, and how we handle references inside a schema.

## Boolean keywords
### `allOf`
To handle `allOf` keywords (i.e., AND operators), we consider the array as a big conjunction and we merge together all the terms in the conjunction.
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

### `anyOf`
Since an `anyOf` is equivalent to the OR operation, we do not have anything to do when retrieving the value.
Indeed, what must be done with the array depends on whether the generator or the validator is running.
Therefore, more information is given later.

### `not`
Since our goal is to get as much constraints as possible for the generator, we handle `not` by propagating it as much as possible.
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
First, we apply the `not` over the conjunction induced by the sub-schema.
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
It is then easier to apply the `not` over each item.
Typically, the properties imposing a minimum number of values to be present become a limit over the maximum number of values.
In the case of `properties` or `items`, the `not` is propagated even further.
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
Notice that we can not propagate the `not` over `type` for now.
When the generator or the validator will process the key `key`, the `not` will be taken into account to restrict the range of allowed types.

### `oneOf`
The `oneOf` keyword is basically a XOR operation over the elements in the array.
Let us denote the XOR operation by $$\oplus$$ and assume the array contains the elements $$A, B$$, and $$C$$.
Then,

$$
A \oplus B \oplus C
\equiv (A \land \neg B \land \neg C) \lor (\neg A \land B \land \neg C) \lor (\neg A \land \neg B \land C).
$$

That is, we can transform the `oneOf` into an `anyOf` containing `allOf`.
For instance, the schema
```json
{
    "oneOf": [
        A,
        B,
        C
    ]
}
```
(where A, B, and C are valid JSON schemas) becomes
```json
{
    "anyOf": [
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
We then apply our procedures over the `anyOf`, `allOf`, and `not` keywords to ensure that all constraints are correctly taken into account.

## References
When encountering the keyword `$ref`, the implementation needs to load the required schema.
In the case where that other schema is in a completely different file, we must open this new file and read the schema in it.
In the case where the schema is actually a sub-schema of the current file, we simply have to "jump" to that part of the file.
In both cases, this operation is invisible for the generator and the validator.
That is, whenever a call to a function retrieving a sub-schema (so, the boolean operators, `properties`, `items`, and so on), the function immediately returns the referenced schema.
In other words, the generator and the validator do not see the `$ref` keywords.
This allows the generation and validation to always behave the same way, regardless of `$ref`.

## Abstractions induced by how the keywords are handled
The way `not` and `allOf` (for instance) are handled already abstracts the schema. Indeed, consider the following schema (let us called it A)
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

It is possible to generate documents that are valid against B (since `minItems` and `maxItems` do not impose any kind of constraints upon integers).
However, all of these generate documents are invalid against A since the sub-schema
```json
{
    "not": {
        "minItems": 5
    }
}
```
returns false for any document (again, as `minItems` do not impose restrictions upon integers).

{% include note.html content="Thus, the way we handle keywords when retrieving the constraints from the schema abstracts the schema. It is to be noted that these abstraction only make the schema accepts more documents." %}

## Implementation
The principles described here are implemented in three classes:
  1. [`be.ac.umons.jsonschematools.JSONSchemaStore`](api/apidocs/be/ac/umons/jsonschematools/JSONSchemaStore.html) handles reading a schema from a file.
  2. [`be.ac.umons.jsonschematools.JSONSchema`](api/apidocs/be/ac/umons/jsonschematools/JSONSchema.html) implements the operations to manipulate a schema (i.e., retrieving constraints, applying the boolean operations, and so on).
  3. `be.ac.umons.jsonschematools.MergeKeys` is an helper class (thus, only accessible in the library code) implementing the actual merging of values. This class is used by `JSONSchema` to split the complexity into more readable parts.

On top of these classes, the classes `JSONObject` and `JSONArray` from `org.json` are extended to provide an implementation of the `hashCode` method: [`be.ac.umons.jsonschematools.HashableJSONObject`](api/apidocs/be/ac/umons/jsonschematools/HashableJSONObject.html) and [`be.ac.umons.jsonschematools.HashableJSONArray`](api/apidocs/be/ac/umons/jsonschematools/HashableJSONArray.html).
This allows us to use sets and maps to implement many parts of the library in an efficient.
In order to guarantee that the results are always the same (to have reproducible results), we rely on `LinkedHashSet` and `LinkedHashMap`.