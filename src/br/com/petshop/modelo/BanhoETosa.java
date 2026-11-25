package br.com.petshop.modelo;

public class BanhoETosa extends ServicoPet {

    private final Animal animal;
    private boolean executado = false;

    public BanhoETosa(Animal animal) {
        super("Banho e tosa");
        if (animal == null) {
            throw new IllegalArgumentException("O animal deve ser informado");
        }
        this.animal = animal;
    }

    @Override
    public String executar() throws VacinaAtrasadaException {
        if (executado) {
            throw new IllegalStateException("O servico ja foi realizado");
        }
        if (!animal.vacinaEmDia()) {

            // Item 11 - Exceção própria. A vacina vencida interrompe o banho e a recepção mostra o motivo ao atendente.
            throw new VacinaAtrasadaException("A vacina do animal esta vencida");
        }

        executado = true;
        return "Servico: " + getNome() + " | Nome do animal: " + animal.getNome()
                + " | Tempo estimado: " + animal.estimarTempoBanho() + " minutos";
    }
}
