package br.com.petshop.modelo;
import java.time.Year;

public class Pessoa {

    // Item 1.1 - Encapsulamento. O cadastro valida o nome na criação e as outras classes o consultam por getNome().
    private final String nome;

    // Item 6.1 - Atributo final em superclasse concreta. O ano registra quando o cadastro foi criado e fica fixo para clientes e veterinários.
    private final int anoCadastro;

    public Pessoa(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome não pode ser nulo ou vazio.");
        }
        this.nome = nome.trim();
        this.anoCadastro = Year.now().getValue();
    }

    public String descreverCadastro() {
        return "Nome: " + this.nome + " - Cadastrado no ano: " + this.anoCadastro;
    }

    public String getNome() {
        return this.nome;
    }

    public int getAnoCadastro() {
        return this.anoCadastro;
    }
}
