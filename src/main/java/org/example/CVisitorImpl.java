package org.example;

import parser.CBaseVisitor;
import parser.CParser;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class CVisitorImpl extends CBaseVisitor<Object> {

    // Começa com um escopo global
    private TabelaSimbolos escopoAtual = new TabelaSimbolos(null);

    // --- 1. Declarações e Atribuições ---

    @Override
    public Object visitDeclStmt(CParser.DeclStmtContext ctx) {
        String nome = ctx.declaration().ID().getText();
        String tipo = ctx.declaration().type().getText();
        Object valor = null;

        // CASO 1: Declaração de STRUCT (ex: struct Ponto p;)
        if (tipo.startsWith("struct")) {
            // O parser retorna "struct Ponto" ou "structPonto" (depende dos espaços)
            // Vamos extrair apenas o nome da struct (o segundo token)
            String nomeStruct = ctx.declaration().type().ID().getText();

            StructDefinition def = escopoAtual.buscarStruct(nomeStruct);
            if (def == null) {
                throw new RuntimeException("Erro: Struct '" + nomeStruct + "' não foi definida.");
            }

            // Instancia a struct como um Map (NomeCampo -> Valor)
            Map<String, Object> instancia = new HashMap<>();
            for (String campo : def.campos.keySet()) {
                instancia.put(campo, 0); // Inicializa tudo com 0
            }
            valor = instancia;
        }
        // CASO 2: Declaração de ARRAY (ex: int arr[5];)
        else if (ctx.declaration().INT() != null) {
            int tamanho = Integer.parseInt(ctx.declaration().INT().getText());
            valor = new Object[tamanho];
            for(int i=0; i<tamanho; i++) ((Object[])valor)[i] = 0;
        }
        // CASO 3: Declaração COMUM (ex: int a = 10;)
        else if (ctx.declaration().expr() != null) {
            valor = visit(ctx.declaration().expr());
        }

        escopoAtual.declarar(nome, tipo, valor);
        return null;
    }

    @Override
    public Object visitAssignStmt(CParser.AssignStmtContext ctx) {
        String nome = ctx.ID().getText();

        // Verifica se tem colchetes (é atribuição de array?)
        // A regra é: ID ('[' expr ']')? '=' expr ';'
        // Se tiver 2 expressões, a primeira é o índice e a segunda é o valor.
        // Se tiver 1 expressão, é variável normal.

        if (ctx.expr().size() > 1) {
            // Caso Array: arr[indice] = valor;
            Object indiceObj = visit(ctx.expr(0));
            Object valorObj  = visit(ctx.expr(1));

            Variavel var = escopoAtual.buscar(nome);
            if (var == null) throw new RuntimeException("Erro: Variável " + nome + " não existe.");

            // Trata o array
            Object[] array = (Object[]) var.valor;
            int idx = (int) indiceObj;

            if (idx < 0 || idx >= array.length) {
                throw new RuntimeException("Erro de Runtime: Índice " + idx + " fora dos limites do vetor " + nome);
            }

            array[idx] = valorObj;
        } else {
            // Caso Normal: x = valor;
            Object valor = visit(ctx.expr(0));
            escopoAtual.atribuir(nome, valor);
        }
        return null;
    }

    // --- 2. Expressões Matemáticas e Lógicas (10 pts) ---

    @Override
    public Object visitIntExpr(CParser.IntExprContext ctx) {
        return Integer.parseInt(ctx.getText());
    }

    @Override
    public Object visitIdExpr(CParser.IdExprContext ctx) {
        Variavel v = escopoAtual.buscar(ctx.getText());
        if (v == null) throw new RuntimeException("Variavel " + ctx.getText() + " nao existe!");
        return v.valor;
    }

    // Exemplo de Soma/Subtração
    @Override
    public Object visitAddSub(CParser.AddSubContext ctx) {
        Object esq = visit(ctx.expr(0));
        Object dir = visit(ctx.expr(1));

        // Dica: Faça verificações aqui se é int ou float
        if (ctx.op.getType() == CParser.PLUS) {
            return (int)esq + (int)dir; // Simplificado para int
        } else {
            return (int)esq - (int)dir;
        }
    }

    // --- 3. Entrada e Saída (10 pts) ---

    @Override
    public Object visitPrintStmt(CParser.PrintStmtContext ctx) {
        // 1. Pega o texto do printf (ex: "Valor: %d") e remove as aspas
        String rawText = ctx.STRING().getText();
        String format = rawText.substring(1, rawText.length() - 1);

        // 2. Pega os argumentos (variáveis após a vírgula)
        Object[] args = new Object[ctx.expr().size()];

        for (int i = 0; i < ctx.expr().size(); i++) {
            // Visita cada expressão para obter o valor real (ex: busca variável 'a')
            args[i] = visit(ctx.expr(i));
        }

        // 3. Imprime formatado usando o próprio Java
        // O try-catch evita que o programa quebre se o aluno errar os tipos no C
        try {
            System.out.printf(format, args);
        } catch (Exception e) {
            System.err.println("Erro de runtime no printf: " + e.getMessage());
        }

        return null;
    }

    private boolean isTrue(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) {
            // Em C, 0 é falso, qualquer outro número é verdadeiro
            return ((Number) value).doubleValue() != 0;
        }
        return false; // Por segurança
    }

    // Implementação do IF
    @Override
    public Object visitIfStmt(CParser.IfStmtContext ctx) {
        // 1. Avalia a condição entre parênteses
        Object condicao = visit(ctx.expr());

        // 2. Verifica se é verdadeira
        if (isTrue(condicao)) {
            // Executa a primeira declaração (o bloco do IF)
            return visit(ctx.statement(0));
        } else {
            // Se tiver ELSE (o segundo statement), executa ele
            if (ctx.statement().size() > 1) {
                return visit(ctx.statement(1));
            }
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(CParser.WhileStmtContext ctx) {
        // Enquanto a condição for verdadeira...
        while (isTrue(visit(ctx.expr()))) {
            // ...executa o corpo do while
            visit(ctx.statement());
        }
        return null;
    }

    @Override
    public Object visitRelation(CParser.RelationContext ctx) {
        // 1. Pega os valores da esquerda e direita
        // (Assumindo Inteiros por enquanto para simplificar)
        int esq = (int)visit(ctx.expr(0));
        int dir = (int)visit(ctx.expr(1));

        // 2. Verifica qual é o operador e faz a conta
        switch (ctx.op.getType()) {
            case CParser.LT:  return esq < dir;  // Menor que (<)
            case CParser.GT:  return esq > dir;  // Maior que (>)
            case CParser.LTE: return esq <= dir; // Menor ou igual (<=)
            case CParser.GTE: return esq >= dir; // Maior ou igual (>=)
            case CParser.EQ:  return esq == dir; // Igual (==)
            case CParser.NEQ: return esq != dir; // Diferente (!=)
            default: return false;
        }
    }

    @Override
    public Object visitFunction(CParser.FunctionContext ctx) {
        String nomeFuncao = ctx.ID().getText();

        // 1. Captura os nomes dos parâmetros
        java.util.List<String> params = new java.util.ArrayList<>();
        if (ctx.paramList() != null) {
            for (CParser.ParamContext p : ctx.paramList().param()) {
                params.add(p.ID().getText());
            }
        }

        // 2. Cria o objeto função e guarda na memória GLOBAL
        // Nota: Funções em C são sempre globais
        Funcao novaFuncao = new Funcao(nomeFuncao, ctx, params);

        // Hack: Se for 'main', executa agora. Se não, só guarda.
        if (nomeFuncao.equals("main")) {
            return visitBlock(ctx.block());
        } else {
            // Assume que escopoAtual aqui é o Global
            escopoAtual.declararFuncao(nomeFuncao, novaFuncao);
        }
        return null;
    }

    @Override
    public Object visitCallExpr(CParser.CallExprContext ctx) {
        String nomeFuncao = ctx.ID().getText();

        // --- IMPLEMENTAÇÃO DO SCANF (NATIVO) ---
        if (nomeFuncao.equals("scanf")) {
            // Exemplo: scanf("%d", &x);
            // Arg 0: String de formatação "%d"
            String formatStr = ctx.expr(0).getText().replace("\"", ""); // Remove aspas

            // Arg 1: Variável com & (ex: &x). Precisamos do nome 'x'.
            // O parser vê "&x" como uma UnaryExpr. Precisamos pegar o texto do filho.
            // A estrutura na árvore será: UnaryExpr -> '&' e ID

            // Cuidado: O aluno pode passar algo que não é &x. Vamos assumir que ele acertou por enquanto.
            CParser.ExprContext arg1 = ctx.expr(1);
            String nomeVariavel = arg1.getText().replace("&", ""); // Hack rápido para pegar o nome

            Scanner sc = new Scanner(System.in);
            Object valorLido = null;

            if (formatStr.equals("%d")) {
                // Lê um inteiro
                System.out.print("Aguardando input (int): "); // Feedback visual opcional
                valorLido = sc.nextInt();
            } else if (formatStr.equals("%f")) {
                System.out.print("Aguardando input (float): ");
                valorLido = sc.nextDouble();
            } else if (formatStr.equals("%s")) {
                System.out.print("Aguardando input (string): ");
                valorLido = sc.next();
            }

            // Atualiza na memória
            escopoAtual.atribuir(nomeVariavel, valorLido);
            return 1; // scanf retorna o número de itens lidos
        }
        // 1. Busca a função na memória
        Funcao funcao = escopoAtual.buscarFuncao(nomeFuncao);
        if (funcao == null) {
            throw new RuntimeException("Erro: Função '" + nomeFuncao + "' não declarada.");
        }

        // 2. Avalia os argumentos passados (ex: soma(10+5, 20))
        // ctx.expr() contém a lista de argumentos passados
        java.util.List<Object> valoresArgs = new java.util.ArrayList<>();
        if (ctx.expr() != null) {
            for (CParser.ExprContext argExpr : ctx.expr()) {
                valoresArgs.add(visit(argExpr));
            }
        }

        // Validação básica de quantidade de argumentos
        if (valoresArgs.size() != funcao.parametros.size()) {
            throw new RuntimeException("Erro: " + nomeFuncao + " espera " + funcao.parametros.size() + " argumentos, mas recebeu " + valoresArgs.size());
        }

        // 3. PREPARAÇÃO DO NOVO ESCOPO (STACK FRAME)
        // O pai do novo escopo deve ser o GLOBAL (para ver vars globais),
        // e não o escopo de quem chamou (C tem escopo estático).
        // Simplificação: Aqui usaremos escopoAtual como pai por enquanto,
        // mas o ideal para nota máxima seria buscar o "Global" raiz.
        TabelaSimbolos escopoAntigo = this.escopoAtual;
        TabelaSimbolos novoEscopo = new TabelaSimbolos(escopoAntigo); // Cria novo ambiente vazio

        // 4. Declara os parâmetros no novo escopo com os valores passados
        for (int i = 0; i < funcao.parametros.size(); i++) {
            String nomeParam = funcao.parametros.get(i);
            Object valorArg = valoresArgs.get(i);
            // Assume tipo 'int' genérico ou pega da definição da função se quiser ser estrito
            novoEscopo.declarar(nomeParam, "int", valorArg);
        }

        // 5. TROCA O ESCOPO E EXECUTA
        this.escopoAtual = novoEscopo;
        Object retorno = null;
        try {
            // Executa o corpo da função guardada
            visit(funcao.ctx.block());
        } catch (ReturnException e) {
            // Captura o valor do return (veremos isso abaixo)
            retorno = e.valor;
        } finally {
            // 6. RESTAURA O ESCOPO ORIGINAL (Pop Stack)
            this.escopoAtual = escopoAntigo;
        }

        return retorno;
    }
    @Override
    public Object visitReturnStmt(CParser.ReturnStmtContext ctx) {
        Object resultado = null;
        if (ctx.expr() != null) {
            resultado = visit(ctx.expr());
        }
        // Lança a exceção para "cortar" a execução até o catch do visitCallExpr
        throw new ReturnException(resultado);
    }
    @Override
    public Object visitUnaryExpr(CParser.UnaryExprContext ctx) {
        String operador = ctx.op.getText();
        Object valor = visit(ctx.expr()); // Visita o filho (ex: o '5' de '-5')

        if (operador.equals("-")) {
            // Inverte sinal (assumindo int ou float)
            if (valor instanceof Integer) return -(int)valor;
            if (valor instanceof Double) return -(double)valor;
        }
        else if (operador.equals("!")) {
            // Negação lógica (0 vira 1, outros viram 0)
            boolean val = isTrue(valor);
            return val ? 0 : 1;
        }
        else if (operador.equals("&")) {
            // Em um interpretador real, retornaríamos o endereço de memória.
            // Como estamos simulando, podemos ignorar e retornar o próprio valor
            // ou lidar com isso apenas no scanf (como fizemos acima com o .replace("&", "")).
            // Para simplificar, vamos apenas retornar o valor da variável.
            return valor;
        }
        return valor;
    }
    @Override
    public Object visitExprStmt(CParser.ExprStmtContext ctx) {
        // Apenas visita a expressão (ex: chama a função scanf ou soma)
        // O valor de retorno é ignorado porque é um statement
        visit(ctx.expr());
        return null;
    }

    // Implementação de Multiplicação e Divisão
    @Override
    public Object visitMulDiv(CParser.MulDivContext ctx) {
        Object esq = visit(ctx.expr(0));
        Object dir = visit(ctx.expr(1));

        // Simplificação: assumindo inteiros (pode expandir para float se quiser)
        int valEsq = (int) esq;
        int valDir = (int) dir;

        if (ctx.op.getType() == CParser.MULT) {
            return valEsq * valDir;
        } else {
            // Cuidado com divisão por zero em um interpretador real!
            return valEsq / valDir;
        }
    }


    @Override
    public Object visitArrayExpr(CParser.ArrayExprContext ctx) {
        String nome = ctx.ID().getText();
        Object indiceObj = visit(ctx.expr());

        Variavel var = escopoAtual.buscar(nome);
        if (var == null) throw new RuntimeException("Vetor " + nome + " nao declarado.");

        Object[] array = (Object[]) var.valor;
        int idx = (int) indiceObj;

        return array[idx];
    }

    @Override
    public Object visitStructDecl(CParser.StructDeclContext ctx) {
        // A lógica é a mesma que você já tinha, mas aplicada direto no contexto Decl

        // Pega o nome (índice 0, pois é o primeiro ID da linha)
        String nomeStruct = ctx.ID(0).getText();

        StructDefinition def = new StructDefinition(nomeStruct);

        // Itera pelos campos
        // O índice dos IDs de campo começa em 1
        int indexIDCampo = 1;
        for(int i=0; i < ctx.type().size(); i++) {
            String tipoCampo = ctx.type(i).getText();
            String nomeCampo = ctx.ID(indexIDCampo).getText();

            def.campos.put(nomeCampo, tipoCampo);
            indexIDCampo++;
        }

        escopoAtual.definirStruct(nomeStruct, def);
        return null;
    }
    @Override
    public Object visitStructDefStmt(CParser.StructDefStmtContext ctx) {
        return visit(ctx.structDecl());
    }

    @Override
    public Object visitStructAssignStmt(CParser.StructAssignStmtContext ctx) {
        // Regra: expr '.' ID '=' expr ';'

        Object objeto = visit(ctx.expr(0)); // O lado esquerdo (a struct)
        String nomeCampo = ctx.ID().getText();
        Object valor = visit(ctx.expr(1));  // O lado direito (o valor)

        if (objeto instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> structInstancia = (Map<String, Object>) objeto;

            if (!structInstancia.containsKey(nomeCampo)) {
                throw new RuntimeException("Erro: Campo '" + nomeCampo + "' não existe.");
            }

            structInstancia.put(nomeCampo, valor);
        } else {
            throw new RuntimeException("Erro: Atribuição de campo em variável que não é struct.");
        }
        return null;
    }

    @Override
    public Object visitMemberAccessExpr(CParser.MemberAccessExprContext ctx) {
        // 1. Visita quem está à esquerda do ponto (o objeto 'p')
        Object esquerda = visit(ctx.expr());

        // 2. Pega o nome do campo (o 'x')
        String nomeCampo = ctx.ID().getText();

        // 3. Verifica e acessa
        if (esquerda instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> structInstancia = (Map<String, Object>) esquerda;

            if (!structInstancia.containsKey(nomeCampo)) {
                throw new RuntimeException("Erro: Campo '" + nomeCampo + "' nao existe na struct.");
            }
            return structInstancia.get(nomeCampo);
        } else {
            throw new RuntimeException("Erro: Tentando acessar campo '.' em algo que nao eh struct.");
        }
    }
}