package br.com.petshop.modelo;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

// Item 8 - Interfaces. A consulta tem agenda e cobrança, por isso implementa Agendavel e Pagavel.
public class Consulta implements Agendavel, Pagavel {

    private final String id;

    // Item 1.2 - Atributos String, int e float. Cada consulta tem seu motivo, sua duração em minutos e seu preço base.
    private final String descricao;
    private final int duracaoMinutos;
    private final float valorConsulta;
    private final boolean urgente;

    private final Cliente responsavel;
    private final Animal paciente;

    private final ProntuarioSimples prontuario;

    private final VeterinarioCirurgiao medico;

    private StatusConsulta status;
    private LocalDateTime dataHora;
    private boolean paga;
    private float valorPago;

    public Consulta(Cliente responsavel, Animal paciente,
            String nomeMedico, String crmv, String descricao,
            int duracaoMinutos, float valorConsulta, boolean urgente) {
        if (responsavel == null) {
            throw new IllegalArgumentException("O responsavel deve ser informado.");
        }
        if (paciente == null) {
            throw new IllegalArgumentException("O paciente deve ser informado.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descricao deve ser informada.");
        }
        if (duracaoMinutos < 1 || duracaoMinutos > 480) {
            throw new IllegalArgumentException("A duracao deve estar entre 1 e 480 minutos.");
        }

        this.id = UUID.randomUUID().toString();
        this.responsavel = responsavel;
        this.paciente = paciente;
        this.descricao = descricao.trim();
        this.duracaoMinutos = duracaoMinutos;
        this.valorConsulta = TabelaPrecos.validarValor(valorConsulta);
        this.urgente = urgente;

        // Item 4 - Composição. Cada consulta cria sua própria ficha para guardar os sintomas e o laudo daquela visita.
        this.prontuario = new ProntuarioSimples();

        this.medico = new VeterinarioCirurgiao(nomeMedico, crmv);

        this.status = StatusConsulta.CRIADA;
        this.dataHora = null;
        this.paga = false;
        this.valorPago = 0.0f;
    }

    @Override
    public void agendar(LocalDateTime dataHora) {
        exigirEditavel();
        if (dataHora == null) {
            throw new IllegalArgumentException("A data e a hora devem ser informadas.");
        }
        this.dataHora = dataHora;
        this.status = StatusConsulta.AGENDADA;
    }

    // Item 2.1 - Varargs com for. Cada animal pode ter vários sintomas. Um laço confere os textos e o outro os registra no prontuário.
    public void adicionarSintomas(String... sintomas) {
        exigirEditavel();
        if (sintomas == null) {
            throw new IllegalArgumentException("A lista de sintomas nao pode ser nula.");
        }

        // Valida todos os sintomas antes de registrar qualquer um.
        for (String sintoma : sintomas) {
            if (sintoma == null || sintoma.isBlank()) {
                throw new IllegalArgumentException("Cada sintoma deve ser informado.");
            }
        }
        for (String sintoma : sintomas) {
            prontuario.adicionarSintoma(sintoma);
        }
    }

    public String realizar(String laudo) {
        if (status != StatusConsulta.AGENDADA) {
            throw new IllegalStateException("Somente uma consulta agendada pode ser realizada.");
        }
        if (laudo == null || laudo.isBlank()) {
            throw new IllegalArgumentException("O laudo deve ser informado.");
        }

        // Item 6.2 - Acoplamento dinâmico. A consulta guarda um VeterinarioCirurgiao e usa o atender() herdado de Veterinario para identificar quem atendeu.
        String atendimento = medico.atender();
        String avaliacao = medico.avaliarProcedimento(descricao);
        prontuario.registrarLaudo(laudo.trim() + " | " + avaliacao);
        status = StatusConsulta.REALIZADA;
        return atendimento + " | Paciente: " + paciente.getNome()
                + " | Responsavel: " + responsavel.getNome();
    }

    public void cancelar() {
        exigirEditavel();
        status = StatusConsulta.CANCELADA;
    }

    @Override
    public float calcularTotal() {
        return TabelaPrecos.arredondar(
                valorConsulta + TabelaPrecos.adicionalUrgencia(urgente));
    }

    // Item 7.1 - Polimorfismo de sobrecarga. A cobrança pode calcular o total da consulta com ou sem desconto.
    public float calcularTotal(float desconto) {
        float total = calcularTotal();
        if (!Float.isFinite(desconto) || desconto < 0.0f || desconto > total) {
            throw new IllegalArgumentException("O desconto deve estar entre zero e o total.");
        }
        return TabelaPrecos.arredondar(total - desconto);
    }

    @Override
    public void registrarPagamento() {
        registrarPagamento(0.0f);
    }

    public void registrarPagamento(float desconto) {
        if (status != StatusConsulta.REALIZADA) {
            throw new IllegalStateException("Somente uma consulta realizada pode ser paga.");
        }
        if (paga) {
            throw new IllegalStateException("O pagamento desta consulta ja foi registrado.");
        }

        float totalComDesconto = calcularTotal(desconto);
        valorPago = totalComDesconto;
        paga = true;
    }

    @Override
    public String emitirRecibo() {
        if (!paga) {
            throw new IllegalStateException("O pagamento ainda nao foi registrado.");
        }

        return String.format(Locale.forLanguageTag("pt-BR"),
                "Recibo | Paciente: %s | Responsavel: %s | Valor pago: R$ %.2f",
                paciente.getNome(), responsavel.getNome(), valorPago);
    }

    public String descrever() {
        String horario = dataHora == null ? "Ainda nao agendada"
                : "Inicio: " + dataHora + " | Fim: " + dataHora.plusMinutes(duracaoMinutos);

        return String.format(Locale.forLanguageTag("pt-BR"),
                "Paciente: %s | Responsavel: %s | Descricao: %s | %s"
                + " | Duracao: %d min | Valor base: R$ %.2f | Total: R$ %.2f"
                + " | Status: %s | Paga: %s",
                paciente.getNome(), responsavel.getNome(), descricao, horario,
                duracaoMinutos, valorConsulta, calcularTotal(), status, paga ? "Sim" : "Nao");
    }

    public String descreverProntuario() {
        return prontuario.descrever();
    }

    public String gerarRegistroHistorico() {
        if (status != StatusConsulta.REALIZADA) {
            throw new IllegalStateException("Somente consultas realizadas entram no historico.");
        }
        String texto = descrever() + " | " + descreverProntuario();
        if (paga) {
            texto += " | " + emitirRecibo();
        }

        texto = texto.replaceAll("\\R|\\t", " ");
        return id + "\t" + texto;
    }

    private void exigirEditavel() {
        if (status != StatusConsulta.CRIADA && status != StatusConsulta.AGENDADA) {
            throw new IllegalStateException("A consulta realizada ou cancelada nao pode ser alterada.");
        }
    }

    public String getId() {
        return id;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public boolean isPaga() {
        return paga;
    }

    public float getValorPago() {
        return valorPago;
    }

    public Cliente getResponsavel() {
        return responsavel;
    }

    public Animal getPaciente() {
        return paciente;
    }
}
