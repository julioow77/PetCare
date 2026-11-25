package br.com.petshop.modelo;

public abstract class ServicoPet {

    private final String nome;

    protected ServicoPet(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O servico deve ser informado");
        }
        this.nome = nome.trim();
    }

    public String getNome() {
        return nome;
    }

    public abstract String executar() throws VacinaAtrasadaException;
}
