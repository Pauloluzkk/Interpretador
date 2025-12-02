// ============================================
// TESTE 19: Scanf (Entrada Interativa)
// ============================================
#include <stdio.h>

int main(void) {
    int numero;

    printf("Digite um numero: ");
    scanf("%d", &numero);

    printf("Voce digitou: %d\n", numero);
    printf("O dobro eh: %f\n", numero * 2);

    return 0;
}