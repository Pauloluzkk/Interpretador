# 🖥️ Interpretador C - Trabalho de Compiladores

**Universidade Estadual do Norte do Paraná**  
**Campus Luiz Meneghel - CCT**  
**Disciplina:** Compiladores

---

## 📝 Descrição

Interpretador para um subconjunto da linguagem C desenvolvido com a linguagem Java como trabalho final da disciplina de Compiladores. O projeto implementa análise léxica, sintática, semântica e execução direta do código fonte.

---

## 👥 Integrantes

- **[Paulo Vítor da Luz Codognotto]**
- **[Isabela Stefanuto Ribeiro Ferreira]**

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java 11+
- **Gerador de Parser:** ANTLR 4
- **Paradigma:** Visitor Pattern
- **Estrutura:** Interpretador Tree-Walking

---

## ✨ Funcionalidades Implementadas

### 1️⃣ Declaração e Inicialização de Variáveis
- [x] `int x = 10;` - Variáveis inteiras
- [x] `float y = 5.5;` - Ponto flutuante
- [x] `char c = 'a';` - Caracteres
- [x] `int arr[5];` - Arrays

### 2️⃣ Estruturas de Controle
- [x] `if/else` - Condicionais
- [x] `switch/case/default` - Múltiplas escolhas
- [x] `for` - Loop com contador
- [x] `while` - Loop com condição
- [x] `do-while` - Loop que executa ao menos uma vez
- [x] `break` - Interrompe loops
- [x] `continue` - Pula iteração

### 3️⃣ Entrada e Saída
- [x] `printf()` - Saída formatada
- [x] `scanf()` - Entrada de dados
- [x] `gets()` - Leitura de string
- [x] `puts()` - Impressão de string

### 4️⃣ Operadores
- [x] Aritméticos: `+`, `-`, `*`, `/`, `%`
- [x] Relacionais: `==`, `!=`, `>`, `<`, `>=`, `<=`
- [x] Lógicos: `&&`, `||`, `!`

### 5️⃣ Manipulação de Ponteiros
- [x] `int *ptr;` - Declaração
- [x] `ptr = &x;` - Atribuição de endereço
- [x] `*ptr = 20;` - Desreferência

### 6️⃣ Funções
- [x] Declaração e chamada
- [x] Parâmetros e retorno
- [x] Recursão direta
- [x] Recursão indireta

### 7️⃣ Estruturas e Unions
- [x] `struct` - Estruturas de dados
- [x] `union` - Tipos união

### 8️⃣ Diretivas de Pré-processador
- [x] `#include <stdio.h>`
- [x] `#define PI 3.14`

### 9️⃣ Comentários
- [x] `//` - Linha única
- [x] `/* */` - Bloco

---

## 📂 Estrutura do Projeto

```
Interpretador/
├── src/main/java/
│   ├── org/example/
│   │   ├── Main.java              # Ponto de entrada
│   │   ├── CVisitorImpl.java      # Implementação do visitor
│   │   ├── TabelaSimbolos.java    # Gerenciamento de memória
│   │   └── ReturnException.java   # Controle de fluxo
│   ├── parser/                     # Arquivos gerados pelo ANTLR
│   └── C.g4                        # Gramática da linguagem
├── exemplos/                          # Arquivos de teste
│   ├── test1.c
│   ├── test11_funcoesparam.c
│   └── ...
└── README.md                       # Este arquivo
```

---

## 🚀 Como Executar

### Pré-requisitos

- Java JDK 11 ou superior
- ANTLR 4.9+ instalado

### Instalação

1. **Clone o repositório:**
```bash
git clone https://github.com/Pauloluzkk/Interpretador.git
cd Interpretador
```
### 🚀 Como Executar

### **Windows:**

**Sintaxe:**
```batch
run.bat <arquivo.c>
```

**Exemplos:**
```batch
REM Executar um arquivo específico
run.bat exemplo.c

REM Executar teste de recursão
run.bat tests\test12_recursao_direta.c

REM Executar calculadora
run.bat tests\test25_calculadora.c
```

**Ou diretamente com o JAR:**
```batch
java -jar Interpretador.jar exemplo.c
```

---

### **Linux/Mac:**

**Sintaxe:**
```bash
./run.sh <arquivo.c>
# ou
bash run.sh <arquivo.c>
```
---

## 📖 Exemplos de Código

### Hello World
```c
#include <stdio.h>

int main(void) {
    printf("Hello, World!\n");
    return 0;
}
```

### Fatorial Recursivo
```c
#include <stdio.h>

int fatorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * fatorial(n - 1);
}

int main(void) {
    int resultado = fatorial(5);
    printf("Fatorial de 5: %d\n", resultado);
    return 0;
}
```

### Structs
```c
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
    return 0;
}
```

Mais exemplos na pasta `exemplos/`.

---

## 🏗️ Arquitetura

### Fluxo de Execução

```
Código Fonte (.c)
      ↓
Análise Léxica (CLexer)
      ↓
Tokens
      ↓
Análise Sintática (CParser)
      ↓
Árvore Sintática (ParseTree)
      ↓
Análise Semântica + Execução (CVisitorImpl)
      ↓
Resultado
```

### Componentes Principais

#### 1. **C.g4 (Gramática)**
Define a sintaxe da linguagem usando ANTLR. Contém:
- Regras léxicas (tokens)
- Regras sintáticas (estrutura do programa)
- Precedência de operadores

#### 2. **CVisitorImpl.java**
Implementa a lógica de execução visitando a árvore sintática:
- Avalia expressões
- Executa comandos
- Gerencia chamadas de função
- Controla fluxo de execução

#### 3. **TabelaSimbolos.java**
Gerencia memória e escopo:
- Armazena variáveis, funções, structs e unions
- Implementa hierarquia de escopos (global/local)
- Realiza conversões de tipo

#### 4. **ReturnException.java**
Exceção customizada para implementar `return`:
- Interrompe execução de função
- Transporta valor de retorno
- Permite unwind da pilha de chamadas

---

## 🧪 Testes

### Testes Implementados

| ID | Funcionalidade | Arquivo | Status |
|----|----------------|---------|--------|
| 01 | Declarações | `test01_declaracoes.c` | ✅      |
| 03 | While | `test03_while.c` | ✅      |
| 04 | For | `test04_for.c` | ✅      |
| 05 | Do-While | `test05_do_while.c` | ✅      |
| 06 | Switch | `test06_switch.c` | ✅      |
| 08 | Operadores | `test08_aritmeticos.c` | ✅      |
| 10 | Lógicos | `test10_logicos.c` | ⚠️     |
| 12 | Recursão | `test12_recursao_direta.c` | ✅      |
| 14 | Structs | `test14_struct.c` | ✅      |
| 15 | Unions | `test15_union.c` | ✅      |
| 28 | Primos | `test28_primos.c` | ✅      |

**Total:** 28 testes.

**Desenvolvido com ☕ e 💻**