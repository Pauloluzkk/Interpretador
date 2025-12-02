// ============================================
// TESTE 14: STRUCT
// ============================================
#include <stdio.h>

struct Ponto {
    int x;
    int y;
};

int main(void) {
    struct Ponto p;
    p.x = 10;
    p.y = 20;

    printf("Ponto: (%d, %d)\n", p.x, p.y);

    // Modificando
    p.x = (p.x + 5);
    p.y = (p.y - 3);

    printf("Novo Ponto: (%f, %f)\n", p.x, p.y);

    return 0;
}