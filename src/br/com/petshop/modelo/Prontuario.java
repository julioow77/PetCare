package br.com.petshop.modelo;

public class Prontuario<T> {

    private T laudo;

    public Prontuario() {
    }

    public void setLaudo(T laudo) {
        if (laudo == null) {
            throw new IllegalArgumentException("O laudo nao pode ser nulo.");
        }
        this.laudo = laudo;
    }

    public T getLaudo() {
        return this.laudo;
    }
}
