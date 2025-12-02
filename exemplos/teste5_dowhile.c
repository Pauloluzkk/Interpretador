// ============================================
// TESTE 5: Loop DO-WHILE
// ============================================
#include <stdio.h>

int main(void) {
    int contador = 0;

    do {
        printf("Contador: %d\n", contador);
        contador = contador + 1;
    } while (contador < 3);

    printf("Saiu do do-while\n");
    return 0;
}