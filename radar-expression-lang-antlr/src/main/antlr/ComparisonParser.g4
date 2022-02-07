parser grammar ComparisonParser;

options { tokenVocab=ComparisonLexer; }

qualifiedId: ID (SEP ID)* ;

function: ID LPAREN (expression (COMMA expression)*)? RPAREN;

expression: operation=(NOT|MINUS) right=expression                               # unaryOperation
          | left=expression comparator=(LT|LTE|GT|GTE|EQ|NE) right=expression    # binaryOperation
          | left=expression comparator=(AND|OR|XOR) right=expression             # combinationOperation
          | BOOLEAN_LITERAL                                                      # booleanLiteral
          | NULL_LITERAL                                                         # nullLiteral
          | STRING_LITERAL                                                       # stringLiteral
          | LPAREN expression RPAREN                                             # parenExpression
          | INTEGER_LITERAL                                                      # integerLiteral
          | DECIMAL_LITERAL                                                      # decimalLiteral
          | qualifiedId                                                          # qualifiedIdExpression
          | function                                                             # functionExpression
          ;