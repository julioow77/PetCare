package br.com.petshop.sistema;

import br.com.petshop.modelo.Animal;
import br.com.petshop.modelo.Cliente;
import br.com.petshop.modelo.Consulta;
import br.com.petshop.modelo.StatusConsulta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PetShopServico {
    private final ArrayList<Cliente> clientes;
    private final ArrayList<Animal> animais;
    // Item 1.4 - ArrayList como atributo. A lista cresce a cada agendamento e guarda as consultas para atender e cobrar depois.
    private final ArrayList<Consulta> listaConsultas;
    private final Path arquivoHistorico;

    public PetShopServico(Path arquivoHistorico) {
        if (arquivoHistorico == null) {
            throw new IllegalArgumentException("O caminho do historico deve ser informado.");
        }
        this.clientes = new ArrayList<>();
        this.animais = new ArrayList<>();
        this.listaConsultas = new ArrayList<>();
        this.arquivoHistorico = arquivoHistorico.toAbsolutePath().normalize();
    }
    
    public Cliente cadastrarCliente(String nome, String telefone) {
        Cliente cliente = new Cliente(nome, telefone);
        clientes.add(cliente);
        return cliente;
    }

    public void cadastrarAnimal(Animal animal) {
        if (animal == null) {
            throw new IllegalArgumentException("O animal deve ser informado.");
        }
        if (contemMesmaInstancia(animais, animal)) {
            throw new IllegalStateException("Este animal ja foi cadastrado.");
        }
        animais.add(animal);
    }

    public void registrarConsulta(Consulta consulta) {
        if (consulta == null) {
            throw new IllegalArgumentException("A consulta deve ser informada.");
        }
        if (!contemMesmaInstancia(clientes, consulta.getResponsavel())) {
            throw new IllegalStateException("O responsavel deve estar cadastrado nesta sessao.");
        }
        if (!contemMesmaInstancia(animais, consulta.getPaciente())) {
            throw new IllegalStateException("O paciente deve estar cadastrado nesta sessao.");
        }
        if (contemMesmaInstancia(listaConsultas, consulta)) {
            throw new IllegalStateException("Esta consulta ja foi registrada.");
        }
        listaConsultas.add(consulta);
    }

    public int salvarHistorico() throws IOException {
        LinkedHashMap<String, String> registros = new LinkedHashMap<>();

        if (Files.exists(arquivoHistorico)) {
            if (!Files.isRegularFile(arquivoHistorico)) {
                throw new IOException("O caminho do historico nao representa um arquivo regular.");
            }
            for (String linha : Files.readAllLines(arquivoHistorico, StandardCharsets.UTF_8)) {
                validarEAdicionar(registros, linha);
            }
        }

        for (Consulta consulta : listaConsultas) {
            if (consulta.getStatus() == StatusConsulta.REALIZADA) {
                String linha = consulta.gerarRegistroHistorico();
                validarEAdicionar(registros, linha);
            }
        }

        Path diretorio = arquivoHistorico.getParent();
        if (diretorio != null) {
            Files.createDirectories(diretorio);
        }

        Path temporario = Files.createTempFile(
                diretorio != null ? diretorio : Path.of(".").toAbsolutePath(),
                arquivoHistorico.getFileName().toString(), ".tmp");
        boolean movido = false;
        try {
            Files.write(temporario, registros.values(), StandardCharsets.UTF_8);
            try {
                Files.move(temporario, arquivoHistorico,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, arquivoHistorico,
                        StandardCopyOption.REPLACE_EXISTING);
            }
            movido = true;
        } finally {
            if (!movido) {
                Files.deleteIfExists(temporario);
            }
        }
        return registros.size();
    }

    public List<String> lerHistorico() throws IOException {
        if (!Files.exists(arquivoHistorico)) {
            return List.of();
        }
        if (!Files.isRegularFile(arquivoHistorico)) {
            throw new IOException("O caminho do historico nao representa um arquivo regular.");
        }
        List<String> linhas = Files.readAllLines(arquivoHistorico, StandardCharsets.UTF_8);
        for (String linha : linhas) {
            validarLinha(linha);
        }
        return List.copyOf(linhas);
    }

    public List<Cliente> listarClientes() {
        return List.copyOf(clientes);
    }

    public List<Animal> listarAnimais() {
        return List.copyOf(animais);
    }

    public List<Consulta> listarConsultas() {
        return List.copyOf(listaConsultas);
    }

    public Path getArquivoHistorico() {
        return arquivoHistorico;
    }

    private static void validarEAdicionar(Map<String, String> registros, String linha)
            throws IOException {
        int separador = validarLinha(linha);
        String id = linha.substring(0, separador);
        registros.put(id, linha);
    }

    private static int validarLinha(String linha) throws IOException {
        if (linha == null || linha.isBlank()) {
            throw new IOException("O historico contem uma linha vazia ou invalida.");
        }
        int separador = linha.indexOf('\t');
        if (separador <= 0 || separador == linha.length() - 1
                || linha.indexOf('\t', separador + 1) >= 0) {
            throw new IOException("Registro de historico malformado: ID e texto devem ser separados por uma unica tabulacao.");
        }
        if (linha.substring(0, separador).isBlank()) {
            throw new IOException("O ID da consulta no historico esta em branco.");
        }
        return separador;
    }

    private static boolean contemMesmaInstancia(List<?> lista, Object procurado) {
        for (Object item : lista) {
            if (item == procurado) {
                return true;
            }
        }
        return false;
    }
}
