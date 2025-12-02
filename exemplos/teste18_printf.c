// ============================================
// TESTE 18: Printf com formatação
// ============================================
#include <stdio.h>

int main(void) {
    int idade = 25;
    float altura = 1.75;
    char inicial = 'J';

    printf("Nome: %c.\n", inicial);
    printf("Idade: %d anos\n", idade);
    printf("Altura: %f metros\n", altura);
    printf("Multiplos valores: %d %f %c\n", idade, altura, inicial);

    return 0;
}