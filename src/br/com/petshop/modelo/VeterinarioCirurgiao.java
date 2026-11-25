package br.com.petshop.modelo;

public class VeterinarioCirurgiao extends Veterinario {

    public VeterinarioCirurgiao(String nome, String crmv) {

        super(nome, crmv);
    }

    public String avaliarProcedimento(String procedimento) {
        if (procedimento == null || procedimento.trim().isEmpty()) {
            throw new IllegalArgumentException("O procedimento não pode ser nulo ou vazio.");
        }
        return "Avaliação do procedimento cirúrgico (" + procedimento.trim() + ") aprovada pelo médico " + getNome();
    }
}
