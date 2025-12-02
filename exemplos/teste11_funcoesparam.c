// ============================================
// TESTE 11: Funções com Parâmetros
// ============================================
#include <stdio.h>

int soma(int a, int b) {
    return a + b;
}

int multiplica(int x, int y) {
    int resultado = x * y;
    return resultado;
}

int main(void) {
    int resultado1 = soma(10, 20);
    printf("Soma: %f\n", resultado1);

    int resultado2 = multiplica(5, 7);
    printf("Multiplicacao: %f\n", resultado2);

    return 0;
}