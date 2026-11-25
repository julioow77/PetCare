package br.com.petshop.modelo;

public class VacinaAtrasadaException extends Exception {

    private static final long serialVersionUID = 1L;

    public VacinaAtrasadaException(String mensagem) {

        super(mensagem);
    }
}
