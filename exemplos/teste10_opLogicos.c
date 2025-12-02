// ============================================
// TESTE 10: Operadores Lógicos && e
// ============================================
#include <stdio.h>

int main(void) {
    int idade = 25;
    int salario = 3000;

    // AND (&&)
    if (idade >= 18 && salario >= 2000) {
        printf("Aprovado no credito (AND)\n");
    }

    // OR ()
    int desconto = 0;
    if (idade > 60 || idade < 18) {
        desconto = 1;
    }

    if (desconto == 1) {
        printf("Tem desconto\n");
    } else {
        printf("Sem desconto\n");
    }

    // NOT (!)
    int ativo = 0;
    if (!ativo) {
        printf("Usuario inativo\n");
    }

    return 0;
}