package org.example;

import parser.CBaseVisitor;
import parser.CParser;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class CVisitorImpl extends CBaseVisitor<Object> {

    private TabelaSimbolos escopoAtual = new TabelaSimbolos(null);
    private Scanner scanner = new Scanner(System.in); // Reutilizar scanner

    // Flags para controle de fluxo
    private boolean breakFlag = false;
    private boolean continueFlag = false;

    // --- 1. Declarações e Atribuições ---

    @Override
    public Object visitDeclStmt(CParser.DeclStmtContext ctx) {
        return visit(ctx.declaration());
    }

    @Override
    public Object visitDeclaration(CParser.DeclarationContext ctx) {
        String nome = ctx.ID().getText();
        String tipo = ctx.type().getText();
        boolean ehPonteiro = ctx.MULT() != null; // Verifica se tem *
        Object valor = null;

        // CASO 1: STRUCT
        if (tipo.startsWith("struct")) {
            String nomeStruct = ctx.type().ID().getText();
            StructDefinition def = escopoAtual.buscarStruct(nomeStruct);
            if (def == null) throw new RuntimeException("Erro: Struct '" + nomeStruct + "' não definida.");

            Map<String, Object> instancia = new HashMap<>();
            for (String campo : def.campos.keySet()) instancia.put(campo, 0);
            valor = instancia;
        }
        // CASO 2: UNION
        else if (tipo.startsWith("union")) {
            String nomeUnion = ctx.type().ID().getText();
            UnionDefinition def = escopoAtual.buscarUnion(nomeUnion);
            if (def == null) throw new RuntimeException("Erro: Union '" + nomeUnion + "' não definida.");

            Map<String, Object> instancia = new HashMap<>();
            // Union: todos os campos compartilham a mesma memória, inicializa vazio
            for (String campo : def.campos.keySet()) instancia.put(campo, null);
            valor = instancia;
        }
        // CASO 3: ARRAY
        else if (ctx.INT() != null) {
            int tamanho = Integer.parseInt(ctx.INT().getText());
            valor = new Object[tamanho];
            for(int i=0; i<tamanho; i++) ((Object[])valor)[i] = 0;
        }
        // CASO 4: PONTEIRO (int *ptr = ...)
        else if (ehPonteiro) {
            if (ctx.expr() != null) {
                valor = visit(ctx.expr()); // Pode ser um endereço (&x)
            } else {
                valor = null; // Ponteiro não inicializado
            }
        }
        // CASO 5: INICIALIZAÇÃO NORMAL
        else if (ctx.expr() != null) {
            valor = visit(ctx.expr());
        }
        // CASO 6: SEM INICIALIZAÇÃO
        else {
            // Valores padrão por tipo
            if (tipo.equals("int")) valor = 0;
            else if (tipo.equals("float")) valor = 0.0f;
            else if (tipo.equals("char")) valor = '\0';
            else valor = null;
        }

        escopoAtual.declarar(nome, tipo, valor, ehPonteiro);
        return null;
    }

    @Override
    public Object visitAssignExpr(CParser.AssignExprContext ctx) {
        Object valorDireita = visit(ctx.expr(1));
        CParser.ExprContext ladoEsquerdo = ctx.expr(0);

        // CASO 1: Variável Simples (x = 10)
        if (ladoEsquerdo instanceof CParser.IdExprContext) {
            String nome = ladoEsquerdo.getText();
            escopoAtual.atribuir(nome, valorDireita);
        }
        // CASO 2: Vetor (v[0] = 10)
        else if (ladoEsquerdo instanceof CParser.ArrayExprContext) {
            CParser.ArrayExprContext arrCtx = (CParser.ArrayExprContext) ladoEsquerdo;
            String nome = arrCtx.ID().getText();
            int indice = (int) visit(arrCtx.expr());

            Variavel var = escopoAtual.buscar(nome);
            Object[] array = (Object[]) var.valor;
            array[indice] = valorDireita;
        }
        // CASO 3: Struct/Union (p.x = 10)
        else if (ladoEsquerdo instanceof CParser.MemberAccessExprContext) {
            CParser.MemberAccessExprContext memCtx = (CParser.MemberAccessExprContext) ladoEsquerdo;
            Object objeto = visit(memCtx.expr());
            String campo = memCtx.ID().getText();

            if (objeto instanceof Map) {
                ((Map<String, Object>) objeto).put(campo, valorDireita);
            }
        }

        return valorDireita;
    }

    // NOVO: Atribuição de ponteiro (*ptr = 20)
    @Override
    public Object visitPointerAssignExpr(CParser.PointerAssignExprContext ctx) {
        Object endereco = visit(ctx.expr(0)); // Deve ser um nome de variável
        Object valor = visit(ctx.expr(1));

        // Simplificação: tratamos como atribuição direta
        if (endereco instanceof String) {
            escopoAtual.atribuir((String) endereco, valor);
        }
        return valor;
    }

    // --- 2. Estruturas de Controle ---

    @Override
    public Object visitIfStmt(CParser.IfStmtContext ctx) {
        Object condicao = visit(ctx.expr());

        if (isTrue(condicao)) {
            return visit(ctx.statement(0));
        } else {
            if (ctx.statement().size() > 1) {
                return visit(ctx.statement(1));
            }
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(CParser.WhileStmtContext ctx) {
        while (isTrue(visit(ctx.expr()))) {
            visit(ctx.statement());

            if (breakFlag) {
                breakFlag = false;
                break;
            }
            if (continueFlag) {
                continueFlag = false;
                continue;
            }
        }
        return null;
    }

    // NOVO: Do-While
    @Override
    public Object visitDoWhileStmt(CParser.DoWhileStmtContext ctx) {
        do {
            visit(ctx.statement());

            if (breakFlag) {
                breakFlag = false;
                break;
            }
            if (continueFlag) {
                continueFlag = false;
                // Não usar continue aqui, senão pula a verificação da condição
            }
        } while (isTrue(visit(ctx.expr())));
        return null;
    }

    @Override
    public Object visitForStmt(CParser.ForStmtContext ctx) {
        // 1. Inicialização
        if (ctx.declaration() != null) {
            visit(ctx.declaration());
        } else if (ctx.expr().size() > 0) {
            // Primeira expressão é a inicialização
            visit(ctx.expr(0));
        }

        // 2. Determina índices das expressões
        int condIdx = (ctx.declaration() != null || ctx.expr().size() > 0) ?
                (ctx.declaration() != null ? 0 : 1) : 0;
        int incrIdx = condIdx + 1;

        // 3. Loop
        while (true) {
            // Verifica condição
            if (ctx.expr().size() > condIdx && ctx.expr(condIdx) != null) {
                if (!isTrue(visit(ctx.expr(condIdx)))) break;
            }

            // Executa corpo
            visit(ctx.statement());

            if (breakFlag) {
                breakFlag = false;
                break;
            }
            if (continueFlag) {
                continueFlag = false;
                // Continue vai para o incremento
            }

            // Incremento
            if (ctx.expr().size() > incrIdx && ctx.expr(incrIdx) != null) {
                visit(ctx.expr(incrIdx));
            }
        }
        return null;
    }

    // NOVO: Switch-Case
    @Override
    public Object visitSwitchStmt(CParser.SwitchStmtContext ctx) {
        Object valorSwitch = visit(ctx.expr());
        boolean matched = false;
        boolean executeDefault = true;

        for (CParser.SwitchCaseContext caseCtx : ctx.switchCase()) {
            // CASE
            if (caseCtx.INT() != null) {
                int caseValue = Integer.parseInt(caseCtx.INT().getText());

                if (!matched && valorSwitch.equals(caseValue)) {
                    matched = true;
                }

                if (matched) {
                    executeDefault = false;
                    for (CParser.StatementContext stmt : caseCtx.statement()) {
                        visit(stmt);
                        if (breakFlag) {
                            breakFlag = false;
                            return null;
                        }
                    }
                }
            }
            // DEFAULT
            else {
                if (!matched && executeDefault) {
                    for (CParser.StatementContext stmt : caseCtx.statement()) {
                        visit(stmt);
                        if (breakFlag) {
                            breakFlag = false;
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    // NOVO: Break
    @Override
    public Object visitBreakStmt(CParser.BreakStmtContext ctx) {
        breakFlag = true;
        return null;
    }

    // NOVO: Continue
    @Override
    public Object visitContinueStmt(CParser.ContinueStmtContext ctx) {
        continueFlag = true;
        return null;
    }

    // --- 3. Expressões Matemáticas ---

    @Override
    public Object visitIntExpr(CParser.IntExprContext ctx) {
        return Integer.parseInt(ctx.getText());
    }

    @Override
    public Object visitFloatExpr(CParser.FloatExprContext ctx) {
        return Float.parseFloat(ctx.getText());
    }

    // NOVO: Char
    @Override
    public Object visitCharExpr(CParser.CharExprContext ctx) {
        String text = ctx.getText();
        // Remove as aspas simples: 'a' -> a
        return text.charAt(1);
    }

    @Override
    public Object visitStringExpr(CParser.StringExprContext ctx) {
        String str = ctx.STRING().getText();
        return str.substring(1, str.length() - 1);
    }

    @Override
    public Object visitIdExpr(CParser.IdExprContext ctx) {
        Variavel v = escopoAtual.buscar(ctx.getText());
        if (v == null) throw new RuntimeException("Variavel " + ctx.getText() + " nao existe!");
        return v.valor;
    }

    @Override
    public Object visitAddSub(CParser.AddSubContext ctx) {
        Object esq = visit(ctx.expr(0));
        Object dir = visit(ctx.expr(1));

        // Suporte a int e float
        double valEsq = toDouble(esq);
        double valDir = toDouble(dir);

        if (ctx.op.getType() == CParser.PLUS) {
            return valEsq + valDir;
        } else {
            return valEsq - valDir;
        }
    }

    @Override
    public Object visitMulDiv(CParser.MulDivContext ctx) {
        Object esq = visit(ctx.expr(0));
        Object dir = visit(ctx.expr(1));

        double valEsq = toDouble(esq);
        double valDir = toDouble(dir);

        switch (ctx.op.getType()) {
            case CParser.MULT: return valEsq * valDir;
            case CParser.DIV:
                if (valDir == 0) throw new RuntimeException("Erro: Divisão por zero!");
                return valEsq / valDir;
            case CParser.MOD: // NOVO: Módulo
                return (int)valEsq % (int)valDir;
            default: return 0;
        }
    }

    @Override
    public Object visitRelation(CParser.RelationContext ctx) {
        Object esq = visit(ctx.expr(0));
        Object dir = visit(ctx.expr(1));

        double valEsq = toDouble(esq);
        double valDir = toDouble(dir);

        boolean resultado;
        switch (ctx.op.getType()) {
            case CParser.LT:  resultado = valEsq < valDir; break;
            case CParser.GT:  resultado = valEsq > valDir; break;
            case CParser.LTE: resultado = valEsq <= valDir; break;
            case CParser.GTE: resultado = valEsq >= valDir; break;
            case CParser.EQ:  resultado = valEsq == valDir; break;
            case CParser.NEQ: resultado = valEsq != valDir; break;
            default: resultado = false;
        }

        // Em C, false = 0, true = 1 (retorna int, não boolean)
        return resultado ? 1 : 0;
    }

    // NOVO: Operadores Lógicos && e ||
    @Override
    public Object visitLogicalExpr(CParser.LogicalExprContext ctx) {
        Object esqObj = visit(ctx.expr(0));
        boolean esq = isTrue(esqObj);

        if (ctx.op.getType() == CParser.AND) {
            // Short-circuit: se esquerda é falsa, não avalia direita
            if (!esq) return 0; // Retorna 0 (false em C)
            Object dirObj = visit(ctx.expr(1));
            boolean dir = isTrue(dirObj);
            return dir ? 1 : 0; // Retorna 1 ou 0
        } else { // OR
            // Short-circuit: se esquerda é verdadeira, não avalia direita
            if (esq) return 1; // Retorna 1 (true em C)
            Object dirObj = visit(ctx.expr(1));
            boolean dir = isTrue(dirObj);
            return dir ? 1 : 0; // Retorna 1 ou 0
        }
    }

    @Override
    public Object visitUnaryExpr(CParser.UnaryExprContext ctx) {
        String operador = ctx.op.getText();

        if (operador.equals("-")) {
            Object valor = visit(ctx.expr());
            double val = toDouble(valor);
            return -val;
        }
        else if (operador.equals("!")) {
            boolean val = isTrue(visit(ctx.expr()));
            return val ? 0 : 1;
        }
        else if (operador.equals("&")) {
            // Retorna o nome da variável (endereço simulado)
            if (ctx.expr() instanceof CParser.IdExprContext) {
                return ((CParser.IdExprContext) ctx.expr()).getText();
            }
            return visit(ctx.expr());
        }
        else if (operador.equals("*")) {
            // Desreferência: *ptr
            Object endereco = visit(ctx.expr());
            if (endereco instanceof String) {
                Variavel v = escopoAtual.buscar((String) endereco);
                if (v != null) return v.valor;
            }
            return endereco;
        }
        return null;
    }

    @Override
    public Object visitParenExpr(CParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    // --- 4. Entrada e Saída ---

    @Override
    public Object visitPrintStmt(CParser.PrintStmtContext ctx) {
        String rawText = ctx.STRING().getText();
        String format = rawText.substring(1, rawText.length() - 1);

        format = format.replace("\\n", "\n");
        format = format.replace("\\t", "\t");

        Object[] args = new Object[ctx.expr().size()];
        for (int i = 0; i < ctx.expr().size(); i++) {
            args[i] = visit(ctx.expr(i));
        }

        try {
            System.out.printf(format, args);
        } catch (Exception e) {
            System.err.println("Erro de runtime no printf: " + e.getMessage());
        }

        return null;
    }

    @Override
    public Object visitCallExpr(CParser.CallExprContext ctx) {
        String nomeFuncao = ctx.ID().getText();

        // SCANF
        if (nomeFuncao.equals("scanf")) {
            String formatStr = ctx.expr(0).getText().replace("\"", "");
            CParser.ExprContext arg1 = ctx.expr(1);
            String nomeVariavel = arg1.getText().replace("&", "");

            Object valorLido = null;

            if (formatStr.equals("%d")) {
                System.out.print("Digite um inteiro: ");
                valorLido = scanner.nextInt();
            } else if (formatStr.equals("%f")) {
                System.out.print("Digite um float: ");
                valorLido = scanner.nextFloat();
            } else if (formatStr.equals("%c")) {
                System.out.print("Digite um char: ");
                valorLido = scanner.next().charAt(0);
            } else if (formatStr.equals("%s")) {
                System.out.print("Digite uma string: ");
                valorLido = scanner.next();
            }

            escopoAtual.atribuir(nomeVariavel, valorLido);
            return 1;
        }

        // NOVO: GETS
        if (nomeFuncao.equals("gets")) {
            String nomeVar = ctx.expr(0).getText();
            System.out.print("Digite uma linha: ");
            scanner.nextLine(); // Limpa buffer
            String linha = scanner.nextLine();
            escopoAtual.atribuir(nomeVar, linha);
            return linha;
        }

        // NOVO: PUTS
        if (nomeFuncao.equals("puts")) {
            Object valor = visit(ctx.expr(0));
            System.out.println(valor);
            return 0;
        }

        // CHAMADA DE FUNÇÃO NORMAL
        Funcao funcao = escopoAtual.buscarFuncao(nomeFuncao);
        if (funcao == null) {
            throw new RuntimeException("Erro: Função '" + nomeFuncao + "' não declarada.");
        }

        java.util.List<Object> valoresArgs = new java.util.ArrayList<>();
        if (ctx.expr() != null) {
            for (CParser.ExprContext argExpr : ctx.expr()) {
                valoresArgs.add(visit(argExpr));
            }
        }

        if (valoresArgs.size() != funcao.parametros.size()) {
            throw new RuntimeException("Erro: " + nomeFuncao + " espera " + funcao.parametros.size() + " argumentos.");
        }

        TabelaSimbolos escopoAntigo = this.escopoAtual;
        TabelaSimbolos novoEscopo = new TabelaSimbolos(escopoAntigo);

        for (int i = 0; i < funcao.parametros.size(); i++) {
            String nomeParam = funcao.parametros.get(i);
            Object valorArg = valoresArgs.get(i);
            novoEscopo.declarar(nomeParam, "int", valorArg, false);
        }

        this.escopoAtual = novoEscopo;
        Object retorno = null;
        try {
            visit(funcao.ctx.block());
        } catch (ReturnException e) {
            retorno = e.valor;
        } finally {
            this.escopoAtual = escopoAntigo;
        }

        return retorno;
    }

    // --- 5. Funções ---

    @Override
    public Object visitFunction(CParser.FunctionContext ctx) {
        String nomeFuncao = ctx.ID().getText();
        java.util.List<String> params = new java.util.ArrayList<>();

        if (ctx.paramList() != null) {
            for (CParser.ParamContext p : ctx.paramList().param()) {
                params.add(p.ID().getText());
            }
        }

        Funcao novaFuncao = new Funcao(nomeFuncao, ctx, params);

        if (nomeFuncao.equals("main")) {
            try {
                return visitBlock(ctx.block());
            } catch (ReturnException e) {
                return e.valor;
            }
        } else {
            escopoAtual.declararFuncao(nomeFuncao, novaFuncao);
        }
        return null;
    }

    @Override
    public Object visitReturnStmt(CParser.ReturnStmtContext ctx) {
        Object resultado = null;
        if (ctx.expr() != null) {
            resultado = visit(ctx.expr());
        }
        throw new ReturnException(resultado);
    }

    // --- 6. Structs e Unions ---

    @Override
    public Object visitStructDecl(CParser.StructDeclContext ctx) {
        String nomeStruct = ctx.ID(0).getText();
        StructDefinition def = new StructDefinition(nomeStruct);

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

    // NOVO: Union
    @Override
    public Object visitUnionDecl(CParser.UnionDeclContext ctx) {
        String nomeUnion = ctx.ID(0).getText();
        UnionDefinition def = new UnionDefinition(nomeUnion);

        int indexIDCampo = 1;
        for(int i=0; i < ctx.type().size(); i++) {
            String tipoCampo = ctx.type(i).getText();
            String nomeCampo = ctx.ID(indexIDCampo).getText();
            def.campos.put(nomeCampo, tipoCampo);
            indexIDCampo++;
        }

        escopoAtual.definirUnion(nomeUnion, def);
        return null;
    }

    @Override
    public Object visitUnionDefStmt(CParser.UnionDefStmtContext ctx) {
        return visit(ctx.unionDecl());
    }

    @Override
    public Object visitMemberAccessExpr(CParser.MemberAccessExprContext ctx) {
        Object esquerda = visit(ctx.expr());
        String nomeCampo = ctx.ID().getText();

        if (esquerda instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> instancia = (Map<String, Object>) esquerda;

            if (!instancia.containsKey(nomeCampo)) {
                throw new RuntimeException("Erro: Campo '" + nomeCampo + "' não existe.");
            }
            return instancia.get(nomeCampo);
        } else {
            throw new RuntimeException("Erro: Tentando acessar campo '.' em não-struct/union.");
        }
    }

    // --- 7. Arrays ---

    @Override
    public Object visitArrayExpr(CParser.ArrayExprContext ctx) {
        String nome = ctx.ID().getText();
        Object indiceObj = visit(ctx.expr());

        Variavel var = escopoAtual.buscar(nome);
        if (var == null) throw new RuntimeException("Vetor " + nome + " não declarado.");

        Object[] array = (Object[]) var.valor;
        int idx = (int) toDouble(indiceObj);

        if (idx < 0 || idx >= array.length) {
            throw new RuntimeException("Erro: Índice " + idx + " fora dos limites.");
        }

        return array[idx];
    }

    // --- 8. Pré-processador ---

    // NOVO: #define
    @Override
    public Object visitDefine(CParser.DefineContext ctx) {
        String nome = ctx.ID().getText();
        Object valor;

        if (ctx.INT() != null) {
            valor = Integer.parseInt(ctx.INT().getText());
        } else if (ctx.FLOAT() != null) {
            valor = Float.parseFloat(ctx.FLOAT().getText());
        } else {
            String str = ctx.STRING().getText();
            valor = str.substring(1, str.length() - 1);
        }

        escopoAtual.declarar(nome, "const", valor, false);
        return null;
    }

    @Override
    public Object visitExprStmt(CParser.ExprStmtContext ctx) {
        visit(ctx.expr());
        return null;
    }

    // --- Funções Auxiliares ---

    private boolean isTrue(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Integer) return ((Integer) value) != 0;
        if (value instanceof Double) return ((Double) value) != 0.0;
        if (value instanceof Float) return ((Float) value) != 0.0f;
        if (value instanceof Character) return ((Character) value) != '\0';
        return false;
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Float) return ((Float) value).doubleValue();
        if (value instanceof Double) return (Double) value;
        if (value instanceof Character) return (double)((Character) value);
        if (value instanceof Boolean) return ((Boolean) value) ? 1.0 : 0.0;
        return 0.0;
    }
}