grammar C;

// Regra inicial: um programa é uma lista de includes, funções ou declarações globais
prog: (include | function | declaration)* ;

include: '#include' '<' ID '.h' '>' ;

// Ex: int main(void) { ... } ou int soma(int a, int b) { ... }
// AQUI estava o erro: agora paramList está definido abaixo
function: type ID '(' (paramList | 'void')? ')' block ;

// Definição dos parâmetros (NOVO)
paramList
    : param (',' param)*
    ;

param
    : type ID
    ;

block: '{' statement* '}' ;

statement
    : declaration ';'                       # DeclStmt
    | ID ('[' expr ']')? '=' expr ';'       # AssignStmt
    | expr ';'                              # ExprStmt
    | 'if' '(' expr ')' statement ('else' statement)? # IfStmt
    | 'while' '(' expr ')' statement        # WhileStmt
    | 'printf' '(' STRING (',' expr)* ')' ';' # PrintStmt
    | 'return' expr? ';'                    # ReturnStmt
    | block                                 # BlockStmt
    ;

declaration:
 type ID ('[' INT ']' | '=' expr)? ;

type: 'int' | 'float' | 'char' | 'void' ;

// Expressões básicas
expr
    : ID '[' expr ']'             # ArrayExpr
    | ID '(' (expr (',' expr)*)? ')' # CallExpr
    | op=('&'|'-'|'!') expr       # UnaryExpr
    | expr op=('*'|'/') expr      # MulDiv
    | expr op=('+'|'-') expr      # AddSub
    | expr op=('=='|'!='|'<'|'>'|'<='|'>=') expr # Relation
    | ID                       # IdExpr
    | INT                      # IntExpr
    | FLOAT                    # FloatExpr
    | STRING                   # StringExpr
    | '(' expr ')'             # ParenExpr
    ;

// --- LÉXICO (Tokens Nomeados) ---

// Operadores Matemáticos (Isso resolve o erro CParser.PLUS)
PLUS : '+' ;
MINUS : '-' ;
MULT : '*' ;
DIV : '/' ;
ADDR : '&' ;
NOT  : '!' ;

// Operadores Relacionais
EQ : '==' ;
NEQ : '!=' ;
GT : '>' ;
LT : '<' ;
GTE : '>=' ;
LTE : '<=' ;

// Outros Símbolos
ASSIGN : '=' ;
SEMI : ';' ;
COMMA : ',' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '{' ;
RBRACE : '}' ;

// Tipos de Dados
TYPE_INT : 'int' ;
TYPE_FLOAT : 'float' ;
TYPE_VOID : 'void' ;

// Palavras Chave
IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;
RETURN : 'return' ;
PRINT : 'printf' ;

LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;

// Identificadores e Literais (Mantenha no final, pois a ordem importa!)
ID: [a-zA-Z_] [a-zA-Z_0-9]* ;
INT: [0-9]+ ;
FLOAT: [0-9]+ '.' [0-9]+ ;
STRING: '"' .*? '"' ;
WS: [ \t\r\n]+ -> skip ;