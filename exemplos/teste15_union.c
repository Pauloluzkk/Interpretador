// ============================================
// TESTE 15: UNION
// ============================================
#include <stdio.h>

union Valor {
    int inteiro;
    float flutuante;
};

int main(void) {
    union Valor v;

    v.inteiro = 42;
    printf("Inteiro: %d\n", v.inteiro);

    v.flutuante = 3.14;
    printf("Float: %f\n", v.flutuante);

    return 0;
   }