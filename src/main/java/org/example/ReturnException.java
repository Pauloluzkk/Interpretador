package org.example;

public class ReturnException extends RuntimeException {
    public Object valor;

    public ReturnException(Object valor) {
        super(); // Não precisa de mensagem
        this.valor = valor;
    }
}