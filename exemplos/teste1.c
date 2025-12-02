// ============================================
// TESTE 1: Declarações e Inicializações
// ============================================
#include <stdio.h>

int main(void) {
    // Variáveis básicas
    int x = 10;
    float y = 5.5;
    char c = 'A';

    printf("int: %d\n", x);
    printf("float: %f\n", y);
    printf("char: %c\n", c);

    // Arrays
    int arr[5];
    arr[0] = 100;
    arr[1] = 200;
    printf("arr[0]: %d\n", arr[0]);
    printf("arr[1]: %d\n", arr[1]);

    return 0;
}