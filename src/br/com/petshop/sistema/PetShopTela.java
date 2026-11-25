package br.com.petshop.sistema;

import br.com.petshop.modelo.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public final class PetShopTela extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter HORARIO = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private final PetShopServico sistema;
    private final JTabbedPane abas = new JTabbedPane();
    private final JTable clientes = tabela("Clientes", "Nome", "Telefone", "Pontos de fidelidade");
    private final JTable animais = tabela("Animais", "Nome", "Espécie", "Idade (anos)", "Porte", "Vacina válida até", "Banho (min)");
    private final JTable consultas = tabela("Consultas", "Data e hora", "Animal", "Responsável", "Situação", "Pagamento");
    private final JTextArea ficha = leitura("Prontuário da consulta");
    private final JTextArea comprovante = leitura("Comprovante");
    private final JTextArea historico = leitura("Histórico de consultas");
    private final JLabel rodape = new JLabel(" ");
    private final JLabel rotuloBusca = new JLabel("Localizar");
    private final JTextField busca = new JTextField(18);
    private final JPanel pesquisa = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    private final JButton realizar = botao("Atender", null, this::realizarConsulta);
    private final JButton receber = botao("Receber pagamento", null, this::receberConsulta);
    private final JButton cancelar = botao("Cancelar", null, this::cancelarConsulta);
    private final JButton atualizarVacina = botao("Atualizar vacina", null, this::atualizarVacina);
    private List<Consulta> consultasExibidas = List.of();

    PetShopTela(PetShopServico sistema) {
        super(new BorderLayout());
        this.sistema = sistema;
        atualizarVacina.setEnabled(false);
        add(cabecalho(), BorderLayout.NORTH);

        Icon novaFicha = UIManager.getIcon("FileView.fileIcon");
        abas.addTab("Clientes", pagina(barra(botao("Cadastrar cliente", novaFicha, this::cadastrarCliente)),
                new JScrollPane(clientes)));
        abas.addTab("Animais", pagina(barra(botao("Cadastrar animal", novaFicha, this::cadastrarAnimal), atualizarVacina),
                new JScrollPane(animais)));
        JPanel agenda = pagina(barra(botao("Agendar consulta", novaFicha, this::agendarConsulta), realizar, receber, cancelar),
                new JScrollPane(consultas));
        JScrollPane prontuario = new JScrollPane(ficha);
        prontuario.setBorder(BorderFactory.createTitledBorder("Prontuário"));
        prontuario.setPreferredSize(new Dimension(300, 97));
        agenda.add(prontuario, BorderLayout.SOUTH);
        abas.addTab("Consultas", agenda);
        comprovante.setText("O comprovante aparece aqui após o atendimento.");
        abas.addTab("Serviços e vendas", pagina(barra(botao("Banho e tosa", null, this::banho),
                botao("Registrar exame", null, this::exame), botao("Vender produto", null, this::venda)),
                comTitulo("Comprovante", comprovante)));
        abas.addTab("Histórico", pagina(barra(botao("Salvar histórico", UIManager.getIcon("FileView.floppyDriveIcon"), () -> atualizarHistorico(true)),
                botao("Ler histórico", UIManager.getIcon("FileView.directoryIcon"), () -> atualizarHistorico(false))),
                new JScrollPane(historico)));
        abas.setBorder(BorderFactory.createEmptyBorder(6, 7, 3, 7));
        add(abas, BorderLayout.CENTER);

        rodape.setBorder(BorderFactory.createEmptyBorder(3, 12, 5, 12));
        add(rodape, BorderLayout.SOUTH);
        consultas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) atualizarSelecao();
        });
        animais.getSelectionModel().addListSelectionListener(e ->
                atualizarVacina.setEnabled(animais.getSelectedRow() >= 0));
        abas.addChangeListener(e -> trocarAba());
        busca.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        carregarListas();
        trocarAba();
    }

    static void aparenciaDoSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            // O Swing usa a aparência padrão quando o tema do sistema está indisponível.
        }
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
    }

    static void abrir(PetShopServico sistema) {
        aparenciaDoSistema();
        JFrame janela = new JFrame("PetCare");
        PetShopTela tela = new PetShopTela(sistema);
        janela.setContentPane(tela);
        BufferedImage original = marca();
        if (original != null && original.getWidth() == 1080 && original.getHeight() == 1080) {
            janela.setIconImage(original.getSubimage(704, 393, 232, 291));
        }
        janela.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        janela.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                try {
                    sistema.salvarHistorico();
                } catch (IOException falha) {
                    int resposta = JOptionPane.showConfirmDialog(janela,
                            "Não foi possível salvar o histórico.\n" + falha.getMessage()
                                    + "\nDeseja sair mesmo assim?",
                            "Fechar PetCare", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (resposta != JOptionPane.YES_OPTION) return;
                }
                janela.dispose();
            }
        });
        Rectangle limite = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        janela.setMinimumSize(new Dimension(Math.min(710, limite.width), Math.min(445, limite.height)));
        janela.setSize(Math.min(850, limite.width), Math.min(545, limite.height));
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }

    private static BufferedImage marca() {
        URL url = PetShopTela.class.getResource("pet.png");
        if (url == null) return null;
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            return null;
        }
    }

    private JPanel cabecalho() {
        JPanel topo = new JPanel(new BorderLayout(10, 0));
        topo.setBackground(Color.WHITE);
        topo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 12, 5, 14)));
        BufferedImage arte = marca();
        JLabel logo = new JLabel("PetCare");
        if (arte != null) {
            if (arte.getWidth() == 1080 && arte.getHeight() == 1080) arte = arte.getSubimage(145, 389, 795, 303);
            logo = new JLabel(new ImageIcon(arte.getScaledInstance(154, -1, Image.SCALE_SMOOTH)));
        }
        logo.getAccessibleContext().setAccessibleName("PetCare, Petshop e Clínica Veterinária");
        topo.add(logo, BorderLayout.WEST);
        pesquisa.setOpaque(false);
        rotuloBusca.setLabelFor(busca);
        pesquisa.add(rotuloBusca);
        pesquisa.add(busca);
        JPanel lado = new JPanel(new GridBagLayout());
        lado.setOpaque(false);
        lado.add(pesquisa);
        topo.add(lado, BorderLayout.EAST);
        return topo;
    }

    private static JTable tabela(String nome, String... colunas) {
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int linha, int coluna) { return false; }
        };
        JTable t = new JTable(modelo);
        t.getAccessibleContext().setAccessibleName(nome);
        t.setFillsViewportHeight(true);
        t.setRowHeight(Math.max(24, t.getRowHeight()));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        TableRowSorter<DefaultTableModel> filtro = new TableRowSorter<>(modelo);
        for (int i = 0; i < colunas.length; i++) filtro.setSortable(i, false);
        t.setRowSorter(filtro);
        return t;
    }

    private static JTextArea leitura(String nome) {
        JTextArea t = new JTextArea();
        t.setEditable(false);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setMargin(new Insets(6, 8, 6, 8));
        t.getAccessibleContext().setAccessibleName(nome);
        return t;
    }

    private static JScrollPane comTitulo(String titulo, JTextArea area) {
        JScrollPane caixa = new JScrollPane(area);
        caixa.setBorder(BorderFactory.createTitledBorder(titulo));
        return caixa;
    }

    private static JPanel pagina(JPanel comandos, JComponent conteudo) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createEmptyBorder(6, 7, 7, 7));
        p.add(comandos, BorderLayout.NORTH);
        p.add(conteudo, BorderLayout.CENTER);
        return p;
    }

    private static JPanel barra(JButton... botoes) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        for (JButton b : botoes) p.add(b);
        return p;
    }

    private JButton botao(String texto, Icon icone, Runnable acao) {
        JButton b = new JButton(texto, icone);
        b.addActionListener(e -> {
            try {
                acao.run();
            } catch (IllegalArgumentException | IllegalStateException falha) {
                avisar(falha.getMessage());
            }
        });
        return b;
    }

    private JTable tabelaAtual() {
        return switch (abas.getSelectedIndex()) {
            case 0 -> clientes;
            case 1 -> animais;
            case 2 -> consultas;
            default -> null;
        };
    }

    private void trocarAba() {
        busca.setText("");
        for (JTable tabela : List.of(clientes, animais, consultas)) {
            if (tabela.getRowSorter() instanceof TableRowSorter<?> filtro) filtro.setRowFilter(null);
        }
        pesquisa.setVisible(tabelaAtual() != null);
        if (tabelaAtual() != null) {
            String nome = switch (abas.getSelectedIndex()) {
                case 0 -> "Localizar cliente";
                case 1 -> "Localizar animal";
                default -> "Localizar consulta";
            };
            rotuloBusca.setText(nome);
            busca.getAccessibleContext().setAccessibleName(nome);
            contarLinhas();
        } else if (abas.getSelectedIndex() == 4) {
            atualizarHistorico(false);
        } else {
            rodape.setText(" ");
        }
    }

    private void filtrar() {
        JTable tabela = tabelaAtual();
        if (tabela != null && tabela.getRowSorter() instanceof TableRowSorter<?> filtro) {
            String termo = busca.getText().trim();
            filtro.setRowFilter(termo.isEmpty() ? null : RowFilter.regexFilter("(?iu)" + Pattern.quote(termo)));
            contarLinhas();
            atualizarSelecao();
        }
    }

    private void contarLinhas() {
        JTable tabela = tabelaAtual();
        if (tabela == null) return;
        String aba = abas.getTitleAt(abas.getSelectedIndex());
        int visiveis = tabela.getRowCount(), total = tabela.getModel().getRowCount();
        rodape.setText(aba + ": " + (visiveis == total ? String.valueOf(total) : visiveis + " de " + total));
    }

    private static DefaultTableModel modelo(JTable tabela) {
        return (DefaultTableModel) tabela.getModel();
    }

    private void carregarListas() {
        Consulta marcada = consultaMarcada();
        DefaultTableModel mc = modelo(clientes);
        mc.setRowCount(0);
        for (Cliente c : sistema.listarClientes()) mc.addRow(new Object[]{c.getNome(), c.getTelefone(), c.getPontosFidelidade()});
        DefaultTableModel ma = modelo(animais);
        ma.setRowCount(0);
        for (Animal a : sistema.listarAnimais()) ma.addRow(new Object[]{a.getNome(), a.getClass().getSimpleName(),
                a.getIdade(), a.getPorte(), DIA.format(a.getValidadeVacina()) + (a.vacinaEmDia() ? "" : " (vencida)"), a.estimarTempoBanho()});
        DefaultTableModel mt = modelo(consultas);
        mt.setRowCount(0);
        consultasExibidas = sistema.listarConsultas();
        for (Consulta c : consultasExibidas) {
            String situacao = c.getStatus().name().toLowerCase(java.util.Locale.ROOT);
            String pagamento = c.isPaga() ? reais(c.getValorPago()) : (c.getStatus() == StatusConsulta.CANCELADA ? "—" : "Em aberto");
            mt.addRow(new Object[]{c.getDataHora() == null ? "—" : HORARIO.format(c.getDataHora()), c.getPaciente().getNome(),
                    c.getResponsavel().getNome(), Character.toUpperCase(situacao.charAt(0)) + situacao.substring(1), pagamento});
        }
        if (marcada != null) selecionar(consultas, consultasExibidas.indexOf(marcada));
        atualizarSelecao();
    }

    private static void selecionar(JTable tabela, int indice) {
        if (indice < 0) return;
        int linha = tabela.convertRowIndexToView(indice);
        if (linha >= 0) {
            tabela.setRowSelectionInterval(linha, linha);
            tabela.scrollRectToVisible(tabela.getCellRect(linha, 0, true));
        }
    }

    private Consulta consultaMarcada() {
        int linha = consultas.getSelectedRow();
        if (linha < 0) return null;
        int i = consultas.convertRowIndexToModel(linha);
        return i < consultasExibidas.size() ? consultasExibidas.get(i) : null;
    }

    private void atualizarSelecao() {
        Consulta c = consultaMarcada();
        realizar.setEnabled(c != null && c.getStatus() == StatusConsulta.AGENDADA);
        receber.setEnabled(c != null && c.getStatus() == StatusConsulta.REALIZADA && !c.isPaga());
        cancelar.setEnabled(c != null && (c.getStatus() == StatusConsulta.CRIADA || c.getStatus() == StatusConsulta.AGENDADA));
        String texto = c == null ? "Selecione uma consulta para ver o prontuário." : c.descreverProntuario().replace(" | ", "\n");
        if (c != null && c.isPaga()) texto += "\n" + c.emitirRecibo();
        ficha.setText(texto);
        ficha.setCaretPosition(0);
    }

    private static JLabel identificar(Consulta c) {
        return new JLabel(c.getPaciente().getNome() + " — " + c.getResponsavel().getNome()
                + (c.getDataHora() == null ? "" : " — " + HORARIO.format(c.getDataHora())));
    }

    private void atendimento(String titulo, String texto) {
        carregarListas();
        comprovante.setText(texto);
        comprovante.setCaretPosition(0);
        rodape.setText(titulo);
    }

    private void avisar(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "PetCare", JOptionPane.WARNING_MESSAGE);
    }

    private void cadastrarCliente() {
        Cliente c = PetShopMain.cadastrarClientePorDialogo(sistema, this);
        if (c == null) return;
        busca.setText("");
        carregarListas();
        selecionar(clientes, sistema.listarClientes().size() - 1);
        rodape.setText("Cliente cadastrado: " + c.descreverCadastro());
    }

    private void cadastrarAnimal() {
        JComboBox<String> especie = new JComboBox<>(new String[]{"Cachorro", "Gato"});
        JTextField nome = new JTextField(18);
        JTextField idade = new JTextField();
        JComboBox<Porte> porte = new JComboBox<>(Porte.values());
        JTextField vacina = new JTextField();
        formulario("Cadastrar animal", campos(new String[]{"Espécie", "Nome do animal", "Idade em anos",
                "Porte", "Validade da vacina (dd/mm/aaaa)"}, especie, nome, idade, porte, vacina), () -> {
            int anos = inteiro(idade, "Idade");
            LocalDate validade = data(vacina);
            Porte p = (Porte) porte.getSelectedItem();
            Animal a = especie.getSelectedIndex() == 0
                    ? new Cachorro(nome.getText(), anos, p, validade) : new Gato(nome.getText(), anos, p, validade);
            sistema.cadastrarAnimal(a);
            busca.setText("");
            carregarListas();
            selecionar(animais, sistema.listarAnimais().size() - 1);
            rodape.setText("Animal cadastrado: " + a.getNome());
        });
    }

    private void atualizarVacina() {
        int linha = animais.getSelectedRow();
        if (linha < 0) return;
        int indice = animais.convertRowIndexToModel(linha);
        Animal animal = sistema.listarAnimais().get(indice);
        JLabel nome = new JLabel(animal.getNome() + " · " + animal.getClass().getSimpleName());
        JTextField validade = new JTextField(DIA.format(animal.getValidadeVacina()), 12);
        formulario("Atualizar vacina", campos(new String[]{"Animal", "Validade da vacina (dd/mm/aaaa)"},
                nome, validade), () -> {
            animal.atualizarValidadeVacina(data(validade));
            carregarListas();
            selecionar(animais, indice);
            rodape.setText("Validade da vacina de " + animal.getNome() + " atualizada para "
                    + DIA.format(animal.getValidadeVacina()) + ".");
        });
    }

    private void agendarConsulta() {
        List<Cliente> clientes = sistema.listarClientes();
        List<Animal> animais = sistema.listarAnimais();
        JComboBox<String> cliente = opcoes(clientes, c -> c.getNome() + " · " + c.getTelefone(), "Cadastre um cliente para agendar.");
        JComboBox<String> animal = escolherAnimal(animais);
        JTextField medico = new JTextField(18), crmv = new JTextField(), descricao = new JTextField();
        JTextField horario = campoHorario(), duracao = new JTextField("30"), valor = new JTextField();
        JCheckBox urgente = new JCheckBox("Consulta urgente");
        JTextField sintomas = new JTextField();
        JPanel dados = campos(new String[]{"Cliente", "Animal", "Veterinário", "CRMV", "Motivo da consulta",
                "Data e hora (dd/mm/aaaa hh:mm)", "Duração em minutos", "Valor em R$",
                "Urgência", "Sintomas (separe com ;)"}, cliente, animal, medico, crmv, descricao, horario, duracao, valor, urgente, sintomas);
        formulario("Agendar consulta", dados, () -> {
            Consulta c = new Consulta(clientes.get(cliente.getSelectedIndex()), animais.get(animal.getSelectedIndex()),
                    medico.getText(), crmv.getText(), descricao.getText(), inteiro(duracao, "Duração"), decimal(valor, "Valor"), urgente.isSelected());
            c.agendar(dataHora(horario));
            if (!sintomas.getText().isBlank()) c.adicionarSintomas(sintomas.getText().trim().split("\\s*;\\s*", -1));
            sistema.registrarConsulta(c);
            busca.setText("");
            carregarListas();
            selecionar(consultas, consultasExibidas.indexOf(c));
            rodape.setText("Consulta agendada: " + c.getPaciente().getNome());
        });
    }

    private void realizarConsulta() {
        Consulta c = consultaMarcada();
        if (c == null) return;
        JTextField laudo = new JTextField(22);
        formulario("Realizar consulta", campos(new String[]{"Consulta", "Laudo"}, identificar(c), laudo), () -> {
            String atendimento = c.realizar(laudo.getText());
            carregarListas();
            ficha.setText(atendimento + "\n" + ficha.getText());
            ficha.setCaretPosition(0);
            rodape.setText("Consulta realizada: " + c.getPaciente().getNome());
        });
    }

    private void receberConsulta() {
        Consulta c = consultaMarcada();
        if (c == null) return;
        JTextField desconto = new JTextField("0");
        formulario("Receber pagamento", campos(new String[]{"Consulta", "Total", "Desconto em R$"},
                identificar(c), new JLabel(reais(c.calcularTotal())), desconto), () -> {
            c.registrarPagamento(decimal(desconto, "Desconto"));
            carregarListas();
            rodape.setText("Pagamento recebido: " + reais(c.getValorPago()));
        });
    }

    private void cancelarConsulta() {
        Consulta c = consultaMarcada();
        if (c == null) return;
        formulario("Cancelar consulta", campos(new String[]{"Consulta a cancelar"}, identificar(c)), () -> {
            c.cancelar();
            carregarListas();
            rodape.setText("Consulta cancelada: " + c.getPaciente().getNome());
        });
    }

    private void banho() {
        List<Animal> animais = sistema.listarAnimais();
        JComboBox<String> animal = escolherAnimal(animais);
        formulario("Banho e tosa", campos(new String[]{"Animal"}, animal), () -> {
            try {
                atendimento("Banho realizado", new BanhoETosa(animais.get(animal.getSelectedIndex())).executar());
            } catch (VacinaAtrasadaException falha) {
                atendimento("Banho recusado", falha.getMessage());
            }
        });
    }

    private void exame() {
        List<Animal> animais = sistema.listarAnimais();
        JComboBox<String> animal = escolherAnimal(animais);
        JTextField nome = new JTextField(18), horario = campoHorario(), laudo = new JTextField();
        JTextField medida = new JTextField(), unidade = new JTextField();
        formulario("Registrar exame", campos(new String[]{"Animal", "Nome do exame", "Data e hora (dd/mm/aaaa hh:mm)",
                "Laudo", "Medida (opcional)", "Unidade da medida"}, animal, nome, horario, laudo, medida, unidade), () -> {
            Exame exame = new Exame(animais.get(animal.getSelectedIndex()), nome.getText());
            exame.agendar(dataHora(horario));
            if (!laudo.getText().isBlank()) exame.registrarLaudo(laudo.getText());
            if (!medida.getText().isBlank() || !unidade.getText().isBlank()) {
                exame.registrarMedida(decimal(medida, "Medida"), unidade.getText());
            }
            atendimento("Resultado do exame", exame.executar());
        });
    }

    private void venda() {
        List<Cliente> clientes = sistema.listarClientes();
        JComboBox<String> cliente = new JComboBox<>();
        cliente.addItem("Venda avulsa");
        for (Cliente c : clientes) cliente.addItem(c.getNome() + " · " + c.getTelefone());
        JTextField produto = new JTextField(20), quantidade = new JTextField("1"), preco = new JTextField();
        formulario("Vender produto", campos(new String[]{"Cliente", "Produto", "Quantidade", "Preço unitário em R$"},
                cliente, produto, quantidade, preco), () -> {
            int n = inteiro(quantidade, "Quantidade");
            float valor = decimal(preco, "Preço unitário");
            int i = cliente.getSelectedIndex();
            VendaProduto venda = i == 0 ? new VendaProduto(produto.getText(), n, valor)
                    : new VendaProduto(produto.getText(), n, valor, clientes.get(i - 1));
            venda.registrarPagamento();
            atendimento("Recibo da venda", venda.emitirRecibo());
        });
    }

    private void atualizarHistorico(boolean gravarAntes) {
        try {
            if (gravarAntes) sistema.salvarHistorico();
            List<String> linhas = sistema.lerHistorico();
            StringBuilder texto = new StringBuilder();
            for (String linha : linhas) {
                int separador = linha.indexOf('\t');
                texto.append(linha.substring(separador + 1).replace(" | ", "\n")).append("\n\n");
            }
            historico.setText(linhas.isEmpty() ? "Nenhuma consulta salva." : texto.toString().stripTrailing());
            historico.setCaretPosition(0);
            rodape.setText(gravarAntes ? "Histórico salvo em " + sistema.getArquivoHistorico()
                    : "Registros no histórico: " + linhas.size());
        } catch (IOException falha) {
            String mensagem = "Falha ao " + (gravarAntes ? "salvar" : "ler") + " o histórico.";
            historico.setText(mensagem);
            rodape.setText(mensagem);
            avisar(mensagem + "\n" + falha.getMessage());
        }
    }

    private static JComboBox<String> escolherAnimal(List<Animal> animais) {
        return opcoes(animais, a -> a.getNome() + " · " + a.getClass().getSimpleName() + " · " + a.getPorte(),
                "Cadastre um animal antes de iniciar o atendimento.");
    }

    private static <T> JComboBox<String> opcoes(List<T> itens, Function<T, String> rotulo, String vazio) {
        if (itens.isEmpty()) throw new IllegalStateException(vazio);
        JComboBox<String> escolha = new JComboBox<>();
        for (int i = 0; i < itens.size(); i++) escolha.addItem((i + 1) + ". " + rotulo.apply(itens.get(i)));
        escolha.setPrototypeDisplayValue("000. Nome do cadastro · 00/00/0000 00:00");
        return escolha;
    }

    private static JPanel campos(String[] rotulos, JComponent... entradas) {
        JPanel painel = new JPanel(new GridBagLayout());
        for (int i = 0; i < rotulos.length; i++) {
            JLabel rotulo = new JLabel(rotulos[i]);
            rotulo.setLabelFor(entradas[i]);
            entradas[i].getAccessibleContext().setAccessibleName(rotulos[i]);
            GridBagConstraints g = new GridBagConstraints();
            g.gridy = i;
            g.anchor = GridBagConstraints.LINE_START;
            g.insets = new Insets(3, 0, 3, 10);
            g.gridx = 0;
            painel.add(rotulo, g);
            g.gridx = 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            painel.add(entradas[i], g);
        }
        return painel;
    }

    private void formulario(String titulo, JPanel dados, Runnable confirmar) {
        while (JOptionPane.showConfirmDialog(this, dados, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                confirmar.run();
                return;
            } catch (IllegalArgumentException | IllegalStateException falha) {
                avisar(falha.getMessage());
            }
        }
    }

    private static JTextField campoHorario() {
        return new JTextField(HORARIO.format(LocalDateTime.now()));
    }

    private static LocalDate data(JTextField campo) {
        try {
            return LocalDate.parse(campo.getText().trim(), DIA);
        } catch (DateTimeParseException falha) {
            throw new IllegalArgumentException("Use uma data válida no formato dd/mm/aaaa.");
        }
    }

    private static LocalDateTime dataHora(JTextField campo) {
        try {
            return LocalDateTime.parse(campo.getText().trim(), HORARIO);
        } catch (DateTimeParseException falha) {
            throw new IllegalArgumentException("Use uma data e hora válidas no formato dd/mm/aaaa hh:mm.");
        }
    }

    private static int inteiro(JTextField campo, String nome) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException falha) {
            throw new IllegalArgumentException(nome + " deve ser um número inteiro.");
        }
    }

    private static float decimal(JTextField campo, String nome) {
        try {
            return Float.parseFloat(campo.getText().trim().replace(',', '.'));
        } catch (NumberFormatException falha) {
            throw new IllegalArgumentException("Confira o número no campo " + nome + ".");
        }
    }

    private static String reais(float valor) {
        return String.format(java.util.Locale.forLanguageTag("pt-BR"), "R$ %.2f", valor);
    }
}
