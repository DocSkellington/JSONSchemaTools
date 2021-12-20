---
layout: page
title: List of unsupported keywords
---

This is a technical page listing the keywords that are not (yet) supported and ideas for their implementations.

## Partially supported keywords

## Unsupported keywords
  * `const`, `dependentSchemas`, `if`, `then`, and `else` for any type.
  * `dependentRequired`, `patternProperties`, `propertyNames`, for objects.
  * `prefixItems`, `contains`, `minContains`, and `maxContains` for arrays.
  * `oneOf` inside a `not`. I must think about a clever way to merge the constraints.

## Keywords that will not be supported
  * With the default handlers:
    * any keyword that have an effect on numbers, integers, or strings.
    * `uniqueItems` for arrays.
  * In general:
    * `format` for strings is discarded.

## Other remaining tasks
  * Write API documentation.
  * Configure continuous integration (for my own experience).