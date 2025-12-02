// ============================================
// TESTE 4: Loop FOR
// ============================================
#include <stdio.h>

int main(void) {
    int i;

    // For básico
    for (i = 0; i < 5; i = i + 1) {
        printf("FOR: i = %d\n", i);
    }

    // For com declaração
    for (int j = 10; j > 5; j = j - 1) {
        printf("FOR decrescente: j = %d\n", j);
    }

    return 0;
}