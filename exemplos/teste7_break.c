// ============================================
// TESTE 7: BREAK e CONTINUE
// ============================================
#include <stdio.h>

int main(void) {
    // Teste BREAK
    printf("=== Teste BREAK ===\n");
    int i = 0;
    while (i < 10) {
        if (i == 5) {
            break;
        }
        printf("i = %d\n", i);
        i = i + 1;
    }

    // Teste CONTINUE
    printf("=== Teste CONTINUE ===\n");
    for (int j = 0; j < 5; j = j + 1) {
        if (j == 2) {
            continue;
        }
        printf("j = %d\n", j);
    }

    return 0;
}