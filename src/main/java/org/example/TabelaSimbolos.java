package org.example;

import java.util.HashMap;
import java.util.Map;

public class TabelaSimbolos {
    private Map<String, Variavel> memoria = new HashMap<>();
    private Map<String, Funcao> funcoes = new HashMap<>();
    private Map<String, StructDefinition> structs = new HashMap<>();
    private Map<String, UnionDefinition> unions = new HashMap<>(); // NOVO

    private TabelaSimbolos escopoPai;

    public TabelaSimbolos(TabelaSimbolos pai) {
        this.escopoPai = pai;
    }

    public void declarar(String nome, String tipo, Object valor, boolean ehPonteiro) {
        memoria.put(nome, new Variavel(tipo, valor, ehPonteiro));
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
        if (v == null && escopoPai != null) {
            return escopoPai.buscar(nome);
        }
        return v;
    }

    public void atribuir(String nome, Object valor) {
        Variavel v = buscar(nome);
        if (v != null) {
            // Conversões automáticas
            if (v.tipo.equals("int") && !(valor instanceof Integer)) {
                if (valor instanceof Float) {
                    valor = ((Float) valor).intValue();
                } else if (valor instanceof Double) {
                    valor = ((Double) valor).intValue();
                } else if (valor instanceof Character) {
                    valor = (int) ((Character) valor);
                }
            }
            else if (v.tipo.equals("float") && !(valor instanceof Float)) {
                if (valor instanceof Integer) {
                    valor = ((Integer) valor).floatValue();
                } else if (valor instanceof Double) {
                    valor = ((Double) valor).floatValue();
                }
            }
            else if (v.tipo.equals("char") && !(valor instanceof Character)) {
                if (valor instanceof Integer) {
                    valor = (char)((Integer) valor).intValue();
                }
            }

            v.valor = valor;
        } else {
            throw new RuntimeException("Erro: Variável '" + nome + "' não declarada.");
        }
    }

    public void definirStruct(String nome, StructDefinition def) {
        structs.put(nome, def);
    }

    public StructDefinition buscarStruct(String nome) {
        StructDefinition s = structs.get(nome);
        if (s == null && escopoPai != null) {
            return escopoPai.buscarStruct(nome);
        }
        return s;
    }

    // NOVO: Union
    public void definirUnion(String nome, UnionDefinition def) {
        unions.put(nome, def);
    }

    public UnionDefinition buscarUnion(String nome) {
        UnionDefinition u = unions.get(nome);
        if (u == null && escopoPai != null) {
            return escopoPai.buscarUnion(nome);
        }
        return u;
    }
}

// Classe auxiliar
class Variavel {
    String tipo;
    Object valor;
    boolean ehPonteiro; // NOVO

    public Variavel(String tipo, Object valor, boolean ehPonteiro) {
        this.tipo = tipo;
        this.valor = valor;
        this.ehPonteiro = ehPonteiro;
    }
}

class Funcao {
    String nome;
    parser.CParser.FunctionContext ctx;
    java.util.List<String> parametros;

    public Funcao(String nome, parser.CParser.FunctionContext ctx, java.util.List<String> params) {
        this.nome = nome;
        this.ctx = ctx;
        this.parametros = params;
    }
}

class StructDefinition {
    String nome;
    Map<String, String> campos = new HashMap<>();

    public StructDefinition(String nome) {
        this.nome = nome;
    }
}

// NOVO
class UnionDefinition {
    String nome;
    Map<String, String> campos = new HashMap<>();

    public UnionDefinition(String nome) {
        this.nome = nome;
    }
}