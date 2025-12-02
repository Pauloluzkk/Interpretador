// ============================================
// TESTE 2: Estruturas de Controle - IF/ELSE
// ============================================
#include <stdio.h>

int main(void) {
    int idade = 18;

    if (idade >= 18) {
        printf("Maior de idade\n");
    } else {
        printf("Menor de idade\n");
    }

    int nota = 85;
    if (nota >= 90) {
        printf("A\n");
    } else {
        if (nota >= 80) {
            printf("B\n");
        } else {
            printf("C\n");
        }
    }

    return 0;
}