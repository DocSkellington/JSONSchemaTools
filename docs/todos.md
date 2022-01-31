---
layout: page
title: List of unsupported keywords
---

This is a technical page listing the keywords that are not (yet) supported and ideas for their implementations.

## Partially supported keywords
  * `const`.
    For the generator, the implementation is working as intended.
    For the validator, a `const` inside a `not` can sometimes be considered as valid or invalid wrongly.
    The problem comes from the abstracted values.
    With the abstractions, we want to check whether the *type* is valid.
    This is problematic with `not` as a valid document then gets rejected.

    Update: I implemented the possibility to decide whether we check the type or the exact values. I have to verify that it is enough for all cases.

## Unsupported keywords
  * `dependentSchemas`, `if`, `then`, and `else` for any type.
  * `dependentRequired`, `patternProperties`, `propertyNames`, for objects.
  * `prefixItems`, `contains`, `minContains`, and `maxContains` for arrays.
  * `oneOf` inside a `not`. I must think about a clever way to merge the constraints.

## Keywords that will not be supported
  * With the default handlers:
    * any keyword that has an effect on numbers, integers, or strings.
    * `uniqueItems` for arrays.
  * In general:
    * `format` for strings is discarded.
    * `enum` values inside a `not` are not considered when processing the enumeration. This is due to the fact that we abstract enumeration values.

## Other remaining tasks
  * Write API documentation.
  * Configure continuous integration (for my own experience).