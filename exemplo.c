#include <stdio.h>

struct Pessoa {
    int idade;
    int id;
};

int main(void) {
    struct Pessoa p;

    printf("Configurando pessoa...\n");
    p.idade = 25;
    p.id = 1;

    printf("Dados da Pessoa: ID=%d, Idade=%d\n", p.id, p.idade);

    // Teste de modificação
    p.idade = p.idade + 1;
    printf("Ano que vem ela tera: %d anos", p.idade);
}