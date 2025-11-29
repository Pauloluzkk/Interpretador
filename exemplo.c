#include <stdio.h>

int main(void) {
    int numeros[5];
    int i = 0;

    // Preenchendo o array
    while (i < 5) {
        numeros[i] = i * 10;
        i = i + 1;
    }

    // Lendo do array
    printf("O valor na posicao 3 eh: %d", numeros[3]); // Deve ser 30

    // Teste de alteração
    numeros[3] = 999;
    printf("\nNovo valor na posicao 3 eh: %d", numeros[3]); // Deve ser 999
}