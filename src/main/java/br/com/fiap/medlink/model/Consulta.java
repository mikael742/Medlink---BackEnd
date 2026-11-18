package br.com.fiap.medlink.model;

import br.com.fiap.medlink.model.enums.OrigemConsulta;
import br.com.fiap.medlink.model.enums.StatusConsulta;
import br.com.fiap.medlink.model.enums.TipoAtendimento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;


public class Consulta {

    private Long id;

    @NotNull(message = "Informe o paciente (paciente.id)")
    private Paciente paciente;

    @NotNull(message = "Informe o profissional (profissional.id)")
    private Profissional profissional;

    @NotNull(message = "A data/hora da consulta é obrigatória")
    @Future(message = "A consulta deve ser marcada para uma data futura")
    private LocalDateTime dataHora;

    private TipoAtendimento tipoAtendimento;
    private StatusConsulta status;
    private OrigemConsulta origem;

    /** Quando a consulta veio de um encaixe, aponta para a consulta cancelada que gerou a vaga. */
    private Long idConsultaOrigem;

    private String motivoCancelamento;
    private LocalDateTime dataCancelamento;
    private LocalDateTime dataCriacao;

    public Consulta() {
    }

    public Consulta(Long id) {
        this.id = id;
    }

    // ---------------- Regras do domínio ----------------

    public void cancelar(String motivo, LocalDateTime agora) {
        if (status != StatusConsulta.AGENDADA) {
            throw new IllegalStateException("Só é possível cancelar consultas com status AGENDADA (atual: " + status + ")");
        }
        this.status = StatusConsulta.CANCELADA;
        this.motivoCancelamento = motivo;
        this.dataCancelamento = agora;
    }

    @JsonIgnore
    public boolean isFutura(LocalDateTime agora) {
        return dataHora != null && dataHora.isAfter(agora);
    }

    public long minutosAte(LocalDateTime agora) {
        return Duration.between(agora, dataHora).toMinutes();
    }

    @JsonIgnore
    public boolean isVagaAberta() {
        return status == StatusConsulta.CANCELADA;
    }

    // ---------------- Getters e setters ----------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional profissional) { this.profissional = profissional; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public TipoAtendimento getTipoAtendimento() { return tipoAtendimento; }
    public void setTipoAtendimento(TipoAtendimento tipoAtendimento) { this.tipoAtendimento = tipoAtendimento; }
    public StatusConsulta getStatus() { return status; }
    public void setStatus(StatusConsulta status) { this.status = status; }
    public OrigemConsulta getOrigem() { return origem; }
    public void setOrigem(OrigemConsulta origem) { this.origem = origem; }
    public Long getIdConsultaOrigem() { return idConsultaOrigem; }
    public void setIdConsultaOrigem(Long idConsultaOrigem) { this.idConsultaOrigem = idConsultaOrigem; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }
    public LocalDateTime getDataCancelamento() { return dataCancelamento; }
    public void setDataCancelamento(LocalDateTime dataCancelamento) { this.dataCancelamento = dataCancelamento; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
