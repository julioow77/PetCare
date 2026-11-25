package br.com.petshop.modelo;

import java.time.LocalDate;

public abstract class Animal {

    private final String nome;
    private final int idade;

    // Item 1.3 - Enum como atributo. O porte determina quantos minutos somar ao banho.
    private final Porte porte;
    private LocalDate validadeVacina;

    protected Animal(String nome, int idade, Porte porte, LocalDate validadeVacina) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do animal deve ser informado");
        }
        if (idade < 0 || idade > 40) {
            throw new IllegalArgumentException("A idade deve estar entre 0 e 40 anos.");
        }
        if (porte == null) {
            throw new IllegalArgumentException("O porte deve ser informado");
        }
        if (validadeVacina == null) {
            throw new IllegalArgumentException("A validade da vacina deve ser informada");
        }
        this.nome = nome.trim();
        this.idade = idade;
        this.porte = porte;
        this.validadeVacina = validadeVacina;
    }

    public String getNome() {
        return nome;
    }

    public int getIdade() {
        return idade;
    }

    public Porte getPorte() {
        return porte;
    }

    public LocalDate getValidadeVacina() {
        return validadeVacina;
    }

    // A vacina continua válida no dia do vencimento.
    public boolean vacinaEmDia() {
        return !validadeVacina.isBefore(LocalDate.now());
    }

    public void atualizarValidadeVacina(LocalDate validade) {
        if (validade == null) {
            throw new IllegalArgumentException("A nova validade deve ser informada");
        }
        this.validadeVacina = validade;
    }

    // Item 10 - Classes abstratas. O tempo de banho depende da espécie. Em ServicoPet, cada tipo de serviço define seu próprio executar().
    public abstract int estimarTempoBanho();

    public String descrever() {
        return "Nome : " + nome + " | Idade: " + idade + " anos"
                + " | Porte: " + porte + " | Banho estimado: "
                + estimarTempoBanho() + " minutos";
    }
}
