// ============================================
// TESTE 13: Recursão Indireta
// ============================================
#include <stdio.h>

int par(int n);
int impar(int n);

int par(int n) {
    if (n == 0) {
        return 1;
    }
    return impar(n - 1);
}

int impar(int n) {
    if (n == 0) {
        return 0;
    }
    return par(n - 1);
}

int main(void) {
    int num = 7;

    if (par(num)) {
        printf("%d eh par\n", num);
    } else {
        printf("%d eh impar\n", num);
    }

    return 0;
}