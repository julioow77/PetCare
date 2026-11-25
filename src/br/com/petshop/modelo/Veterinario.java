package br.com.petshop.modelo;

public class Veterinario extends Pessoa {

    private final String crmv;

    public Veterinario(String nome, String crmv) {
        super(nome);
        if (crmv == null || crmv.trim().isEmpty()) {
            throw new IllegalArgumentException("O CRMV não pode ser nulo ou vazio.");
        }
        this.crmv = crmv.trim();
    }

    public String atender() {
        return "Atendimento realizado pelo veterinário " + getNome() + " (CRMV: " + this.crmv + ")";
    }

    public String getCrmv() {
        return this.crmv;
    }
}
