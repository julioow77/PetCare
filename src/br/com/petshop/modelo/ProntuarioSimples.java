package br.com.petshop.modelo;

import java.util.ArrayList;

public final class ProntuarioSimples {

    private final ArrayList<String> sintomas;
    private String laudo;

    public ProntuarioSimples() {
        this.sintomas = new ArrayList<String>();
        this.laudo = "Ainda nao registrado";
    }

    public void adicionarSintoma(String sintoma) {
        if (sintoma == null || sintoma.isBlank()) {
            throw new IllegalArgumentException("O sintoma deve ser informado.");
        }
        sintomas.add(sintoma.trim());
    }

    public void registrarLaudo(String laudo) {
        if (laudo == null || laudo.isBlank()) {
            throw new IllegalArgumentException("O laudo deve ser informado.");
        }
        this.laudo = laudo.trim();
    }

    public String descrever() {
        String textoSintomas = sintomas.isEmpty()
                ? "Nao informados" : String.join(", ", sintomas);
        return "Sintomas: " + textoSintomas + " | Laudo: " + laudo;
    }
}
