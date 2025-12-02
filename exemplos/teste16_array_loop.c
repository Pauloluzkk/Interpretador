// ============================================
// TESTE 16: Arrays com Loops
// ============================================
#include <stdio.h>

int main(void) {
    int numeros[5];
    int i;

    // Preenche array
    for (i = 0; i < 5; i = i + 1) {
        numeros[i] = i * 10;
    }

    // Imprime array
    printf("Array:\n");
    for (i = 0; i < 5; i = i + 1) {
        printf("numeros[%d] = %f\n", i, numeros[i]);
    }

    return 0;
}