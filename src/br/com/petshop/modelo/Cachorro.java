package br.com.petshop.modelo;

import java.time.LocalDate;

public class Cachorro extends Animal {

    public Cachorro(String nome, int idade, Porte porte, LocalDate validadeVacina) {
        super(nome, idade, porte, validadeVacina);
    }

    @Override
    public int estimarTempoBanho() {
        return 30 + getPorte().getMinutosAdicionais();
    }
}
