package br.com.petshop.modelo;

import java.time.LocalDate;

public class Gato extends Animal {

    public Gato(String nome, int idade, Porte porte, LocalDate validadeVacina) {
        super(nome, idade, porte, validadeVacina);
    }

    @Override
    public int estimarTempoBanho() {
        return 45 + getPorte().getMinutosAdicionais();
    }
}
