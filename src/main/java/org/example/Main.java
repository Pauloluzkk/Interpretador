package org.example;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import parser.*;
// Importe o pacote onde o ANTLR gerou os arquivos (ex: br.uenp.compiler.parser)

public class Main {
    public static void main(String[] args) throws Exception {
        // O código fonte será passado como argumento (requisito do trabalho) [cite: 39]
        // Ex de uso: java -jar seuprograma.jar codigo_fonte.c
        String filename = "exemplo.c"; // Para teste no IDE

        // 1. Cria o Lexer (Análise Léxica)
        CharStream input = CharStreams.fromFileName(filename);
        CLexer lexer = new CLexer(input);

        // 2. Cria o fluxo de tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 3. Cria o Parser (Análise Sintática)
        CParser parser = new CParser(tokens);

        // 4. Começa a análise pela regra inicial 'prog'
        ParseTree tree = parser.prog();

        CVisitorImpl eval = new CVisitorImpl();
        eval.visit(tree);
    }
}