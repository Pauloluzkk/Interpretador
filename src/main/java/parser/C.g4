grammar C;

// Regra inicial - ADICIONADO: define, unionDecl
prog: (include | define | function | structDecl | unionDecl | declaration)* ;

structDecl
    : 'struct' ID '{' (type ID ';')* '}' ';'
    ;

// NOVO: Union
unionDecl
    : 'union' ID '{' (type ID ';')* '}' ';'
    ;

include: '#include' '<' ID '.h' '>' ;

// NOVO: #define
define: '#define' ID (INT | FLOAT | STRING) ;

function: type ID '(' (paramList | 'void')? ')' block ;

paramList
    : param (',' param)*
    ;

param
    : type ID
    ;

block: '{' statement* '}' ;

statement
    : declaration ';'                                           # DeclStmt
    | structDecl                                                # StructDefStmt
    | unionDecl                                                 # UnionDefStmt
    | expr ';'                                                  # ExprStmt
    | 'if' '(' expr ')' statement ('else' statement)?           # IfStmt
    | 'while' '(' expr ')' statement                            # WhileStmt
    | 'for' '(' (declaration | expr)? ';' expr? ';' expr? ')' statement # ForStmt
    | 'do' statement 'while' '(' expr ')' ';'                   # DoWhileStmt
    | 'switch' '(' expr ')' '{' switchCase* '}'                 # SwitchStmt
    | 'printf' '(' STRING (',' expr)* ')' ';'                   # PrintStmt
    | 'return' expr? ';'                                        # ReturnStmt
    | 'break' ';'                                               # BreakStmt
    | 'continue' ';'                                            # ContinueStmt
    | block                                                     # BlockStmt
    ;

// NOVO: Switch cases
switchCase
    : 'case' INT ':' statement*
    | 'default' ':' statement*
    ;

// MODIFICADO: Suporte a ponteiros (int *ptr)
declaration
    : type ('*')? ID ('[' INT ']' | '=' expr)?
    ;

type: 'int' | 'float' | 'char' | 'void' | 'struct' ID | 'union' ID ;

// MODIFICADO: Adicionado % (módulo) e && || (lógicos)
expr
    : expr op=('&&'|'||')                     expr              # LogicalExpr
    | expr op=('=='|'!='|'<'|'>'|'<='|'>=')   expr              # Relation
    | expr op=('+'|'-')                       expr              # AddSub
    | expr op=('*'|'/'|'%')                   expr              # MulDiv
    | <assoc=right> expr '=' expr                               # AssignExpr
    | <assoc=right> '*' expr '=' expr                           # PointerAssignExpr
    | expr '.' ID                                               # MemberAccessExpr
    | ID '[' expr ']'                                           # ArrayExpr
    | ID '(' (expr (',' expr)*)? ')'                            # CallExpr
    | op=('&'|'-'|'!'|'*') expr                                 # UnaryExpr
    | ID                                                        # IdExpr
    | INT                                                       # IntExpr
    | FLOAT                                                     # FloatExpr
    | CHAR                                                      # CharExpr
    | STRING                                                    # StringExpr
    | '(' expr ')'                                              # ParenExpr
    ;

// --- LÉXICO ---

// Operadores Matemáticos
PLUS : '+' ;
MINUS : '-' ;
MULT : '*' ;
DIV : '/' ;
MOD : '%' ;           // NOVO
ADDR : '&' ;
NOT  : '!' ;

// Operadores Relacionais
EQ : '==' ;
NEQ : '!=' ;
GT : '>' ;
LT : '<' ;
GTE : '>=' ;
LTE : '<=' ;

// Operadores Lógicos - NOVO
AND : '&&' ;
OR : '||' ;

// Outros Símbolos
ASSIGN : '=' ;
SEMI : ';' ;
COMMA : ',' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '{' ;
RBRACE : '}' ;
DOT : '.' ;
LBRACK : '[' ;
RBRACK : ']' ;
COLON : ':' ;         // NOVO (para switch)

// Tipos de Dados
TYPE_INT : 'int' ;
TYPE_FLOAT : 'float' ;
TYPE_CHAR : 'char' ;  // NOVO
TYPE_VOID : 'void' ;
TYPE_STRUCT : 'struct' ;
TYPE_UNION : 'union' ; // NOVO

// Palavras Chave
IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;
FOR : 'for' ;
DO : 'do' ;            // NOVO
SWITCH : 'switch' ;    // NOVO
CASE : 'case' ;        // NOVO
DEFAULT : 'default' ;  // NOVO
BREAK : 'break' ;      // NOVO
CONTINUE : 'continue' ; // NOVO
RETURN : 'return' ;
PRINT : 'printf' ;
DEFINE : '#define' ;   // NOVO

// Comentários
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;

// Identificadores e Literais
ID: [a-zA-Z_] [a-zA-Z_0-9]* ;
INT: [0-9]+ ;
FLOAT: [0-9]+ '.' [0-9]+ ;
CHAR: '\'' . '\'' ;    // NOVO: 'a', 'b', etc
STRING: '"' .*? '"' ;
WS: [ \t\r\n]+ -> skip ;