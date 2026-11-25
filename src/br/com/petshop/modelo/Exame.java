package br.com.petshop.modelo;

import java.time.LocalDateTime;

public class Exame extends ServicoPet implements Agendavel {

    private final Animal animal;
    private LocalDateTime dataHora;
    private String unidade;
    private boolean executado;

    // Item 7.4 - Polimorfismo paramétrico. O exame usa Prontuario<String> para o laudo e Prontuario<Float> para a medida. Os resultados entram por setLaudo().
    private final Prontuario<String> laudoTextual;
    private final Prontuario<Float> medida;

    public Exame(Animal animal, String nome) {

        super(nome);
        if (animal == null) {
            throw new IllegalArgumentException("O animal deve ser informado.");
        }
        this.animal = animal;
        this.dataHora = null;
        this.unidade = null;
        this.executado = false;

        this.laudoTextual = new Prontuario<String>();
        this.medida = new Prontuario<Float>();
    }

    @Override
    public void agendar(LocalDateTime dataHora) {
        exigirAberto();
        if (dataHora == null) {
            throw new IllegalArgumentException("A data e a hora devem ser informadas.");
        }
        this.dataHora = dataHora;
    }

    public void registrarLaudo(String texto) {
        exigirAberto();
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("O texto do laudo deve ser informado.");
        }

        laudoTextual.setLaudo(texto.trim());
    }

    public void registrarMedida(float valor, String unidade) {
        exigirAberto();
        if (!Float.isFinite(valor)) {
            throw new IllegalArgumentException("A medida deve ser um numero finito.");
        }
        if (unidade == null || unidade.isBlank()) {
            throw new IllegalArgumentException("A unidade da medida deve ser informada.");
        }

        medida.setLaudo(valor);
        this.unidade = unidade.trim();
    }

    @Override
    public String executar() {
        exigirAberto();
        if (dataHora == null) {
            throw new IllegalStateException("O exame deve ser agendado antes de executar.");
        }
        if (laudoTextual.getLaudo() == null && medida.getLaudo() == null) {
            throw new IllegalStateException("Informe pelo menos um resultado do exame.");
        }

        String resultado = "Exame: " + getNome() + " | Animal: " + animal.getNome()
                + " | Data/Hora: " + dataHora;
        if (laudoTextual.getLaudo() != null) {
            resultado += " | Laudo: " + laudoTextual.getLaudo();
        }
        if (medida.getLaudo() != null) {
            resultado += " | Medida: " + medida.getLaudo() + " " + unidade;
        }
        executado = true;
        return resultado;
    }

    private void exigirAberto() {
        if (executado) {
            throw new IllegalStateException("O exame ja foi executado.");
        }
    }
}
