package br.com.petshop.modelo;

public class Cliente extends Pessoa {

    private final String telefone;
    private int pontosFidelidade;

    // Item 3 - Construtores de classes concretas. Cliente exige nome e telefone no cadastro. Veterinario exige nome e CRMV.
    public Cliente(String nome, String telefone) {
        super(nome);
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new IllegalArgumentException("O telefone não pode ser nulo ou vazio.");
        }
        this.telefone = telefone.trim();
        this.pontosFidelidade = 0;
    }

    public void adicionarPontos(int pontos) {
        if (pontos >= 0) {

            if (Integer.MAX_VALUE - this.pontosFidelidade >= pontos) {
                this.pontosFidelidade += pontos;
            } else {
                throw new IllegalStateException("O limite de pontos de fidelidade foi excedido.");
            }
        } else {
            throw new IllegalArgumentException("Os pontos a adicionar não podem ser negativos.");
        }
    }
    // Item 7.3 - Polimorfismo de subtipagem. Cliente sobrescreve descreverCadastro() da superclasse concreta Pessoa para incluir telefone e pontos.
    @Override
    public String descreverCadastro() {
        return super.descreverCadastro() + " - Telefone: " + this.telefone + " - Pontos: " + this.pontosFidelidade;
    }

    public String getTelefone() {
        return this.telefone;
    }

    public int getPontosFidelidade() {
        return this.pontosFidelidade;
    }
}
