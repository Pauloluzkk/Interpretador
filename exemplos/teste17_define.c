// ============================================
// TESTE 17: #define
// ============================================
#include <stdio.h>
#define PI 3.14
#define MAX 100
#define NOME "Sistema"

int main(void) {
    printf("PI = %f\n", PI);
    printf("MAX = %d\n", MAX);
    printf("Nome: %s\n", NOME);

    int raio = 5;
    float area = PI * raio * raio;
    printf("Area do circulo: %f\n", area);

    return 0;
}