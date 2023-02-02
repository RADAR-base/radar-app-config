# RADAR expression language

The RADAR expression language used in the app-config service is described by a [lexer](https://github.com/RADAR-base/radar-app-config/blob/store-protocol/radar-expression-lang-antlr/src/main/antlr/ComparisonLexer.g4) and [parser](https://github.com/RADAR-base/radar-app-config/blob/store-protocol/radar-expression-lang-antlr/src/main/antlr/ComparisonParser.g4). Together, these allow simple numeric and logical expressions as well as simple variable lookups and function calls. Example expressions:

Test the value of a variable. Each line is a separate expression, and each evaluates to true.
```
my.variable = 5
my.other.variable = 0.3 && my.text.variable = "test"
count([1, 2, 3, 4, 5]) = 3 + count(1, 2)
sum([1, 2, 3, 3]) = 3 * count(other.list)
includes([1, 2, 3, 4], 3)
contains("ababa", "bab")
startsWith(my.text.variable, "te")
endsWith(my.text.variable, "st")
user.birthDate = "1970-01-01"
```

Text variables that are separated with commas, spaces, semicolons or tabs (one of ` \t,;`) can be parsed as lists.

List of functions:

| Function signature     | Description                                                                                              |
|------------------------|----------------------------------------------------------------------------------------------------------|
| sum(number...)         | Computes the sum of all arguments and lists.                                                             |
| count(any...)          | The number of elements in all arguments and lists.                                                       |
| contains(text, text)   | Whether the first variable contains the value of the second variable. The match is case insensitive.     |
| startsWith(text, text) | Whether the first variable starts with the value of the second variable.  The match is case insensitive. |
| endsWith(text, text)   | Whether the first variable ends with the value of the second variable.  The match is case insensitive.   |
| includes(list, any)    | Whether the first list variable includes an entry for the second variable.                               |

The `user.<attribute>` variables are parsed from the ManagementPortal subject attribute values.
