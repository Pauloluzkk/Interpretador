package org.example;

import java.util.HashMap;
import java.util.Map;

public class TabelaSimbolos {
    // Armazena nome -> Variável (valor + tipo)
    private Map<String, Variavel> memoria = new HashMap<>();
    private Map<String, Funcao> funcoes = new HashMap<>();
    // Suporte a escopos (para funções futuramente)
    private TabelaSimbolos escopoPai;

    public TabelaSimbolos(TabelaSimbolos pai) {
        this.escopoPai = pai;
    }

    public void declarar(String nome, String tipo, Object valor) {
        // Aqui você pode adicionar verificação se já existe (Erro Semântico)
        memoria.put(nome, new Variavel(tipo, valor));
    }

    public void declararFuncao(String nome, Funcao func) {
        funcoes.put(nome, func);
    }

    public Funcao buscarFuncao(String nome) {
        Funcao f = funcoes.get(nome);
        if (f == null && escopoPai != null) {
            return escopoPai.buscarFuncao(nome);
        }
        return f;
    }

    public Variavel buscar(String nome) {
        Variavel v = memoria.get(nome);
        // Se não achou aqui e tem pai, busca no pai (Escopo Global)
        if (v == null && escopoPai != null) {
            return escopoPai.buscar(nome);
        }
        return v;
    }

    public void atribuir(String nome, Object valor) {
        Variavel v = buscar(nome);
        if (v != null) {
            // Validação de Tipos (Garante os 7 pontos da planilha)
            if (v.tipo.equals("int") && !(valor instanceof Integer)) {
                // Tenta converter automaticamente (cast) se for float para int
                if (valor instanceof Float) {
                    valor = ((Float) valor).intValue();
                } else if (valor instanceof Double) {
                    valor = ((Double) valor).intValue();
                } else {
                    throw new RuntimeException("Erro Semântico: Atribuindo valor incompatível a int '" + nome + "'");
                }
            }

            v.valor = valor;
        } else {
            throw new RuntimeException("Erro: Variável '" + nome + "' não declarada.");
        }
    }
}

// Classe auxiliar para guardar Tipo e Valor juntos
class Variavel {
    String tipo;
    Object valor;

    public Variavel(String tipo, Object valor) {
        this.tipo = tipo;
        this.valor = valor;
    }
}
class Funcao {
    String nome;
    parser.CParser.FunctionContext ctx; // O código da função (Árvore do ANTLR)
    java.util.List<String> parametros;  // Nomes dos parâmetros (ex: ["a", "b"])

    public Funcao(String nome, parser.CParser.FunctionContext ctx, java.util.List<String> params) {
        this.nome = nome;
        this.ctx = ctx;
        this.parametros = params;
    }
}