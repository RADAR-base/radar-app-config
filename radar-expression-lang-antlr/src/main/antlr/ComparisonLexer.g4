lexer grammar ComparisonLexer;

// Whitespace
WS            : ('\r\n' | 'r' | '\n' | [\t ]+) -> skip;


// reserved words
AND                : 'and' | '&&';
OR                 : 'or' | '||';
NOT                : 'not' | '!';
XOR                : 'xor' | '^';

// Literals
BOOLEAN_LITERAL    : 'true' | 'false';
NULL_LITERAL       : 'null';
INTEGER_LITERAL    : ('0' | [1-9][0-9]*) ;
DECIMAL_LITERAL    : INTEGER_LITERAL ('.' [0-9]+)? (('e' | 'E') '-'? INTEGER_LITERAL)? ;
STRING_LITERAL     : ('"' (~('"' | '\\' | '\r' | '\n') | '\\' ('"' | '\\'))* '"')
                   | ('\'' (~('\'' | '\\' | '\r' | '\n') | '\\' ('\'' | '\\'))* '\'');

PLUS               : '+' ;
MINUS              : '-' ;
TIMES              : '*' ;
DIVIDED            : '/' ;
LPAREN             : '(' ;
RPAREN             : ')' ;
LBRACK             : '[' ;
RBRACK             : ']' ;
LT                 : '<' ;
GT                 : '>' ;
LTE                : '<=' ;
GTE                : '>=' ;
EQ                 : '==' | '=' ;
NE                 : '!=' ;
COMMA              : ',';

// Identifiers
ID                 : [_]*[a-z][A-Za-z0-9_]* ;
SEP                : '.' ;
