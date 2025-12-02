// ============================================
// TESTE 12: Recursão Direta
// ============================================
#include <stdio.h>

int fatorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * fatorial(n - 1);
}

int fibonacci(int n) {
    if (n <= 1) {
        return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
}

int main(void) {
    int fat5 = fatorial(5);
    printf("Fatorial de 5: %f\n", fat5);

    int fib6 = fibonacci(6);
    printf("Fibonacci de 6: %f\n", fib6);

    return 0;
}