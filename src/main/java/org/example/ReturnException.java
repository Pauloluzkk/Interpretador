package org.example;

class ReturnException extends RuntimeException {
    Object valor;
    public ReturnException(Object valor) { this.valor = valor; }
}
