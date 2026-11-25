package br.com.petshop.sistema;
// Item 9 - Pacotes. modelo reúne as regras do petshop. sistema cuida das entradas, dos cadastros e do histórico.

import br.com.petshop.modelo.Agendavel;
import br.com.petshop.modelo.Animal;
import br.com.petshop.modelo.BanhoETosa;
import br.com.petshop.modelo.Cachorro;
import br.com.petshop.modelo.Cliente;
import br.com.petshop.modelo.Consulta;
import br.com.petshop.modelo.Exame;
import br.com.petshop.modelo.Gato;
import br.com.petshop.modelo.Pagavel;
import br.com.petshop.modelo.Porte;
import br.com.petshop.modelo.ServicoPet;
import br.com.petshop.modelo.StatusConsulta;
import br.com.petshop.modelo.VacinaAtrasadaException;
import br.com.petshop.modelo.VendaProduto;
import br.com.petshop.modelo.Veterinario;
import br.com.petshop.modelo.VeterinarioCirurgiao;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class PetShopMain {
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter
            .ofPattern("dd/MM/uuuu HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {
        String modo = args.length == 0 ? "--tela" : args[0];
        if ("--ajuda".equalsIgnoreCase(modo)) {
            mostrarAjuda();
            return;
        }
        if (!"--tela".equalsIgnoreCase(modo) && !"--demo".equalsIgnoreCase(modo) && !"--menu".equalsIgnoreCase(modo)) {
            System.err.println("Modo invalido: " + modo);
            mostrarAjuda();
            return;
        }

        Path caminho = args.length >= 2
                ? Path.of(args[1])
                : Path.of("dados", "historico_consultas.txt");
        PetShopServico sistema = new PetShopServico(caminho);

        if ("--tela".equalsIgnoreCase(modo) && !GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> PetShopTela.abrir(sistema));
            return;
        }
        try {
            if ("--demo".equalsIgnoreCase(modo)) {
                demonstrar(sistema);
            } else {
                executarMenu(sistema);
            }
        } catch (IOException e) {
            System.err.println("Falha ao acessar o historico: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Operacao recusada: " + e.getMessage());
        }
    }

    private static void demonstrar(PetShopServico sistema) throws IOException {
        Cliente cliente = sistema.cadastrarCliente("Ana Souza", "(87) 99999-0000");
        Animal cachorro = new Cachorro("Rex", 4, Porte.MEDIO, LocalDate.now().plusMonths(6));
        Animal gato = new Gato("Luna", 2, Porte.PEQUENO, LocalDate.now().plusMonths(3));
        sistema.cadastrarAnimal(cachorro);
        sistema.cadastrarAnimal(gato);

        // Demonstra descreverCadastro() sobrescrito em Cliente (Item 7.3) e o atendimento do cirurgião.
        System.out.println(cliente.descreverCadastro());
        mostrarProfissional(new VeterinarioCirurgiao("Dra. Marina", "CRMV-PE 1234"));

        Consulta consulta = new Consulta(cliente, cachorro, "Dra. Marina", "CRMV-PE 1234",
                "Consulta clinica", 45, 120.0f, true);
        consulta.agendar(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0));
        consulta.adicionarSintomas("Falta de apetite", "Sonolencia");
        sistema.registrarConsulta(consulta);
        System.out.println(consulta.realizar("Animal hidratado e em observacao."));
        consulta.registrarPagamento(10.0f);
        System.out.println(consulta.emitirRecibo());

        executarServico(new BanhoETosa(gato));

        Exame exame = new Exame(cachorro, "Hemograma");
        Agendavel agendavel = exame;
        agendavel.agendar(LocalDateTime.now().plusDays(2).withSecond(0).withNano(0));
        exame.registrarLaudo("Resultado descritivo");
        exame.registrarMedida(6.5f, "kg");
        executarServico(exame);

        cobrar(new VendaProduto("Racao premium", 2, 44.95f, cliente));
        cobrar(new VendaProduto("Brinquedo", 1, 25.0f));

        // Item 13 - Gravação e leitura de arquivo. Outra sessão pode ler o TXT e mostrar o histórico das consultas realizadas.
        int total = sistema.salvarHistorico();
        System.out.println("Registros salvos: " + total);
        PetShopServico leitor = new PetShopServico(sistema.getArquivoHistorico());
        System.out.println("Historico recuperado:");
        for (String linha : leitor.lerHistorico()) {
            System.out.println(linha);
        }
    }

    private static void mostrarProfissional(Veterinario profissional) {
        System.out.println(profissional.atender());
    }

    private static void cobrar(Pagavel cobranca) {
        System.out.printf("Total: R$ %.2f%n", cobranca.calcularTotal());
        cobranca.registrarPagamento();
        System.out.println(cobranca.emitirRecibo());
    }

    private static void executarServico(ServicoPet servico) {
        try {
            System.out.println(servico.executar());
        } catch (VacinaAtrasadaException e) {
            System.out.println("Servico recusado: " + e.getMessage());
        }
    }

    private static void executarMenu(PetShopServico sistema) {
        try (Scanner entrada = new Scanner(System.in, StandardCharsets.UTF_8)) {
            boolean executando = true;
            while (executando) {
                mostrarMenu();
                try {
                    int opcao = inteiro(entrada, "Opcao: ");
                    switch (opcao) {
                        case 0 -> {
                            salvarAoEncerrar(sistema);
                            executando = false;
                        }
                        case 1 -> {
                            String nome = ler(entrada, "Nome: ");
                            String telefone = ler(entrada, "Telefone: ");
                            Cliente cliente = sistema.cadastrarCliente(nome, telefone);
                            System.out.println("Cliente cadastrado: " + cliente.descreverCadastro());
                        }
                        case 2 -> cadastrarAnimal(entrada, sistema);
                        case 3 -> agendarConsulta(entrada, sistema);
                        case 4 -> realizarOuPagarConsulta(entrada, sistema);
                        case 5 -> listar(sistema);
                        case 6 -> executarServico(new BanhoETosa(escolherAnimal(entrada, sistema)));
                        case 7 -> realizarExame(entrada, sistema);
                        case 8 -> vender(entrada, sistema);
                        case 9 -> System.out.println("Registros no historico: " + sistema.salvarHistorico());
                        case 10 -> mostrarHistorico(sistema);
                        case 11 -> {
                            Consulta consulta = escolherConsulta(entrada, sistema);
                            consulta.cancelar();
                            System.out.println("Consulta cancelada.");
                        }
                        case 12 -> cadastrarClientePorDialogo(sistema);
                        default -> System.out.println("Opcao invalida. Escolha um numero de 0 a 12.");
                    }
                } catch (NoSuchElementException e) {
                    System.out.println("Fim da entrada. Encerrando.");
                    salvarAoEncerrar(sistema);
                    executando = false;
                } catch (NumberFormatException | DateTimeParseException e) {
                    System.out.println("Entrada invalida. Confira o formato informado.");
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Operacao recusada: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println("Erro no historico: " + e.getMessage());
                }
            }
        }
    }

    private static void cadastrarClientePorDialogo(PetShopServico sistema) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("O cadastro por diálogo precisa de uma tela. Use a opção 1 para cadastrar pelo console.");
            return;
        }
        Cliente cliente = cadastrarClientePorDialogo(sistema, null);
        System.out.println(cliente == null ? "Cadastro cancelado." : "Cliente cadastrado: " + cliente.descreverCadastro());
    }

    // Item 12 - Entrada por caixa de diálogo. As duas janelas recolhem o nome e o telefone usados no cadastro do cliente.
    static Cliente cadastrarClientePorDialogo(PetShopServico sistema, Component janela) {
        String nome = JOptionPane.showInputDialog(janela, "Nome do cliente:",
                "Cadastro de cliente", JOptionPane.QUESTION_MESSAGE);
        if (nome == null) {
            return null;
        }

        String telefone = JOptionPane.showInputDialog(janela, "Telefone do cliente:",
                "Cadastro de cliente", JOptionPane.QUESTION_MESSAGE);
        if (telefone == null) {
            return null;
        }

        return sistema.cadastrarCliente(nome, telefone);
    }

    private static void cadastrarAnimal(Scanner entrada, PetShopServico sistema) {
        String especie = ler(entrada, "Especie (C cachorro / G gato): ");
        String nome = ler(entrada, "Nome: ");
        int idade = inteiro(entrada, "Idade: ");
        Porte porte = Porte.valueOf(ler(entrada, "Porte (PEQUENO, MEDIO ou GRANDE): ").toUpperCase());
        LocalDate validade = LocalDate.parse(ler(entrada, "Validade da vacina (aaaa-MM-dd): "));
        Animal animal;
        if ("C".equalsIgnoreCase(especie) || "CACHORRO".equalsIgnoreCase(especie)) {
            animal = new Cachorro(nome, idade, porte, validade);
        } else if ("G".equalsIgnoreCase(especie) || "GATO".equalsIgnoreCase(especie)) {
            animal = new Gato(nome, idade, porte, validade);
        } else {
            throw new IllegalArgumentException("Especie deve ser cachorro ou gato.");
        }
        sistema.cadastrarAnimal(animal);
        System.out.println("Animal cadastrado: " + animal.descrever());
    }

    private static void agendarConsulta(Scanner entrada, PetShopServico sistema) {
        Cliente cliente = escolherCliente(entrada, sistema);
        Animal animal = escolherAnimal(entrada, sistema);
        String medico = ler(entrada, "Nome do veterinario: ");
        String crmv = ler(entrada, "CRMV: ");
        String descricao = ler(entrada, "Descricao: ");
        int duracao = inteiro(entrada, "Duracao em minutos: ");
        float valor = decimal(entrada, "Valor da consulta: ");
        boolean urgente = sim(ler(entrada, "Urgente? (S/N): "));
        Consulta consulta = new Consulta(cliente, animal, medico, crmv, descricao,
                duracao, valor, urgente);
        consulta.agendar(dataHora(entrada));
        String sintomas = ler(entrada, "Sintomas separados por ponto e virgula (ou vazio): ");
        if (!sintomas.isBlank()) {
            consulta.adicionarSintomas(sintomas.split("\\s*;\\s*"));
        }
        sistema.registrarConsulta(consulta);
        System.out.println("Consulta agendada: " + consulta.descrever());
    }

    private static void realizarOuPagarConsulta(Scanner entrada, PetShopServico sistema) {
        Consulta consulta = escolherConsulta(entrada, sistema);
        if (consulta.getStatus() == StatusConsulta.AGENDADA) {
            System.out.println(consulta.realizar(ler(entrada, "Laudo: ")));
        }
        if (consulta.getStatus() != StatusConsulta.REALIZADA) {
            throw new IllegalStateException("A consulta deve estar agendada ou realizada.");
        }
        if (!consulta.isPaga() && sim(ler(entrada, "Registrar pagamento agora? (S/N): "))) {
            float desconto = decimal(entrada, "Desconto em reais (0 sem desconto): ");
            consulta.registrarPagamento(desconto);
            System.out.println(consulta.emitirRecibo());
        } else if (consulta.isPaga()) {
            System.out.println(consulta.emitirRecibo());
        }
    }

    private static void realizarExame(Scanner entrada, PetShopServico sistema) {
        Animal animal = escolherAnimal(entrada, sistema);
        Exame exame = new Exame(animal, ler(entrada, "Nome do exame: "));
        exame.agendar(dataHora(entrada));
        String laudo = ler(entrada, "Laudo textual (ou vazio): ");
        if (!laudo.isBlank()) {
            exame.registrarLaudo(laudo);
        }
        if (sim(ler(entrada, "Registrar medida? (S/N): "))) {
            exame.registrarMedida(decimal(entrada, "Valor da medida: "),
                    ler(entrada, "Unidade: "));
        }
        executarServico(exame);
    }

    private static void vender(Scanner entrada, PetShopServico sistema) {
        String produto = ler(entrada, "Produto: ");
        int quantidade = inteiro(entrada, "Quantidade: ");
        float valor = decimal(entrada, "Valor unitario: ");
        VendaProduto venda;
        if (sim(ler(entrada, "Venda identificada? (S/N): "))) {
            venda = new VendaProduto(produto, quantidade, valor,
                    escolherCliente(entrada, sistema));
        } else {
            venda = new VendaProduto(produto, quantidade, valor);
        }
        cobrar(venda);
    }

    private static void listar(PetShopServico sistema) {
        System.out.println("CLIENTES");
        sistema.listarClientes().forEach(c -> System.out.println(c.descreverCadastro()));
        System.out.println("ANIMAIS");
        sistema.listarAnimais().forEach(a -> System.out.println(a.descrever()));
        System.out.println("CONSULTAS");
        sistema.listarConsultas().forEach(c -> System.out.println(c.descrever()));
    }

    private static Cliente escolherCliente(Scanner entrada, PetShopServico sistema) {
        List<Cliente> lista = sistema.listarClientes();
        for (int i = 0; i < lista.size(); i++) {
            System.out.printf("%d - %s%n", i + 1, lista.get(i).descreverCadastro());
        }
        return lista.get(indice(entrada, lista.size()));
    }

    private static Animal escolherAnimal(Scanner entrada, PetShopServico sistema) {
        List<Animal> lista = sistema.listarAnimais();
        for (int i = 0; i < lista.size(); i++) {
            System.out.printf("%d - %s%n", i + 1, lista.get(i).descrever());
        }
        return lista.get(indice(entrada, lista.size()));
    }

    private static Consulta escolherConsulta(Scanner entrada, PetShopServico sistema) {
        List<Consulta> lista = sistema.listarConsultas();
        for (int i = 0; i < lista.size(); i++) {
            System.out.printf("%d - %s%n", i + 1, lista.get(i).descrever());
        }
        return lista.get(indice(entrada, lista.size()));
    }

    private static int indice(Scanner entrada, int tamanho) {
        if (tamanho == 0) {
            throw new IllegalStateException("Nao ha itens cadastrados para selecionar.");
        }
        int escolhido = inteiro(entrada, "Numero do item: ");
        if (escolhido < 1 || escolhido > tamanho) {
            throw new IllegalArgumentException("Escolha um numero entre 1 e " + tamanho + ".");
        }
        return escolhido - 1;
    }

    private static String ler(Scanner entrada, String mensagem) {
        System.out.print(mensagem);
        if (!entrada.hasNextLine()) {
            throw new NoSuchElementException("Fim da entrada.");
        }
        return entrada.nextLine().trim();
    }

    private static int inteiro(Scanner entrada, String mensagem) {
        return Integer.parseInt(ler(entrada, mensagem));
    }

    private static float decimal(Scanner entrada, String mensagem) {
        return Float.parseFloat(ler(entrada, mensagem).replace(',', '.'));
    }

    private static LocalDateTime dataHora(Scanner entrada) {
        return LocalDateTime.parse(
                ler(entrada, "Data e hora (dd/MM/aaaa HH:mm): "), DATA_HORA);
    }

    private static boolean sim(String valor) {
        if ("S".equalsIgnoreCase(valor) || "SIM".equalsIgnoreCase(valor)) {
            return true;
        }
        if ("N".equalsIgnoreCase(valor) || "NAO".equalsIgnoreCase(valor)
                || "NÃO".equalsIgnoreCase(valor)) {
            return false;
        }
        throw new IllegalArgumentException("Responda S ou N.");
    }

    private static void mostrarHistorico(PetShopServico sistema) throws IOException {
        List<String> linhas = sistema.lerHistorico();
        if (linhas.isEmpty()) {
            System.out.println("Historico vazio.");
        } else {
            linhas.forEach(System.out::println);
        }
    }

    private static void salvarAoEncerrar(PetShopServico sistema) {
        try {
            System.out.println("Registros no historico: " + sistema.salvarHistorico());
        } catch (IOException e) {
            System.out.println("Nao foi possivel salvar ao encerrar: " + e.getMessage());
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n0 - Salvar e sair");
        System.out.println("1 - Cadastrar cliente");
        System.out.println("2 - Cadastrar animal");
        System.out.println("3 - Agendar consulta");
        System.out.println("4 - Realizar/pagar consulta");
        System.out.println("5 - Listar cadastros");
        System.out.println("6 - Executar banho e tosa");
        System.out.println("7 - Realizar exame");
        System.out.println("8 - Realizar venda");
        System.out.println("9 - Salvar historico");
        System.out.println("10 - Ler historico");
        System.out.println("11 - Cancelar consulta");
        System.out.println("12 - Cadastrar cliente por caixa de diálogo");
    }

    private static void mostrarAjuda() {
        System.out.println("Uso: java br.com.petshop.sistema.PetShopMain [--tela|--menu|--demo|--ajuda] [caminho-do-txt]");
        System.out.println("Sem argumentos, abre a tela do PetCare. Em ambiente sem tela, abre o menu do console.");
        System.out.println("O arquivo padrao e dados/historico_consultas.txt.");
    }
}
