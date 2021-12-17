---
title: Structure and abstractions
layout: pageFA
---

The project is split into three modules:
  1. Implementations of tools around JSON Schemas.
  2. The generator.
  3. The validator.

## Tools

### Special keywords
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
would get reduced to
```json
{
    "type": "integer",
    "multipleOf": 30
}
```
because we want the integer to be divisible by 2, 3, and 5 at the same time.

#### "anyOf"

#### "oneOf"

#### "not"


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

## Generator

## Validator
