---
title: Unsupported keywords
layout: pageMath
---

* 
{:toc}

This page lists the keywords that are partially supported or unsupported.

## Partially supported keywords
  * `patternProperties`.
    The regular expression used as a pattern is directly used as a key.
    This approach only works under the assumption that the `patternProperties` adds *new* keys.
    That is, if a pattern is supposed to add more constraints to an already defined key, the implementation will not merge the constraints but will keep the two keys as separate.

## Unsupported keywords
  * `dependentSchemas`, `if`, `then`, and `else` for any type.
  * `dependentRequired`, `propertyNames`, for objects.
  * `prefixItems`, `contains`, `minContains`, and `maxContains` for arrays.
  * `oneOf` inside a `not`. I must think about a clever way to merge the constraints.

### Keywords that will not be supported
  * With the default handlers:
    * any keyword that has an effect on numbers, integers, or strings.
    * `uniqueItems` for arrays.
  * In general:
    * `format` for strings is discarded.
    * `enum` values inside a `not` are not considered when processing the enumeration. This is due to the fact that we abstract enumeration values.