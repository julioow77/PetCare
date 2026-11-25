package br.com.petshop.modelo;

public interface Pagavel {
    float calcularTotal();

    void registrarPagamento();

    String emitirRecibo();
}
