package br.com.petshop.modelo;

public enum Porte {
    PEQUENO(0),
    MEDIO(10),
    GRANDE(20);

    private final int minutosAdicionais;

    private Porte(int minutosAdicionais) {
        this.minutosAdicionais = minutosAdicionais;
    }

    public int getMinutosAdicionais() {
        return minutosAdicionais;
    }
}
