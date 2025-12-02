// ============================================
// TESTE 20: Ponteiros Básicos
// ============================================
#include <stdio.h>

int main(void) {
    int x = 42;
    int *ptr;

    ptr = &x;
    printf("Valor de x: %d\n", x);
    printf("Endereco simulado: %s\n", ptr);

    *ptr = 100;
    printf("Novo valor de x: %d\n", x);

    return 0;
}