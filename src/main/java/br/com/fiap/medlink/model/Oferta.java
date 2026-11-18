package br.com.fiap.medlink.model;

import br.com.fiap.medlink.model.enums.MetodoDistribuicao;
import br.com.fiap.medlink.model.enums.StatusOferta;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;


public class Oferta {

    private Long id;
    private Consulta vaga;
    private InteresseConsulta interesse;
    private Paciente paciente;
    private MetodoDistribuicao metodo;
    private StatusOferta status = StatusOferta.PENDENTE;

    /** Ordem desta oferta dentro da vaga (1ª, 2ª, 3ª tentativa...). */
    private int tentativa;

    /** Preenchidos apenas no método de proximidade. */
    private Double distanciaKm;
    private Integer tempoEstimadoMin;
    private Double raioBuscaKm;

    private LocalDateTime dataEnvio;
    private LocalDateTime dataExpiracao;
    private LocalDateTime dataResposta;

    /** Consulta criada quando o paciente aceita. */
    private Consulta consultaGerada;

    /** Quantos dias o paciente antecipou (quando já tinha consulta marcada). */
    private Integer diasAntecipados;

    public Oferta() {
    }

    // ---------------- Regras do domínio ----------------

    @JsonIgnore
    public boolean isPendente() {
        return status == StatusOferta.PENDENTE;
    }

    @JsonIgnore
    public boolean isExpirada(LocalDateTime agora) {
        return isPendente() && dataExpiracao != null && agora.isAfter(dataExpiracao);
    }

    public void aceitar(LocalDateTime agora, Consulta consultaGerada, Integer diasAntecipados) {
        garantirPendente();
        this.status = StatusOferta.ACEITA;
        this.dataResposta = agora;
        this.consultaGerada = consultaGerada;
        this.diasAntecipados = diasAntecipados;
    }

    public void recusar(LocalDateTime agora) {
        garantirPendente();
        this.status = StatusOferta.RECUSADA;
        this.dataResposta = agora;
    }

    public void expirar(LocalDateTime agora) {
        garantirPendente();
        this.status = StatusOferta.EXPIRADA;
        this.dataResposta = agora;
    }

    private void garantirPendente() {
        if (!isPendente()) {
            throw new IllegalStateException("A oferta já foi finalizada (status: " + status + ")");
        }
    }

    // ---------------- Getters e setters ----------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Consulta getVaga() { return vaga; }
    public void setVaga(Consulta vaga) { this.vaga = vaga; }
    public InteresseConsulta getInteresse() { return interesse; }
    public void setInteresse(InteresseConsulta interesse) { this.interesse = interesse; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public MetodoDistribuicao getMetodo() { return metodo; }
    public void setMetodo(MetodoDistribuicao metodo) { this.metodo = metodo; }
    public StatusOferta getStatus() { return status; }
    public void setStatus(StatusOferta status) { this.status = status; }
    public int getTentativa() { return tentativa; }
    public void setTentativa(int tentativa) { this.tentativa = tentativa; }
    public Double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(Double distanciaKm) { this.distanciaKm = distanciaKm; }
    public Integer getTempoEstimadoMin() { return tempoEstimadoMin; }
    public void setTempoEstimadoMin(Integer tempoEstimadoMin) { this.tempoEstimadoMin = tempoEstimadoMin; }
    public Double getRaioBuscaKm() { return raioBuscaKm; }
    public void setRaioBuscaKm(Double raioBuscaKm) { this.raioBuscaKm = raioBuscaKm; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }
    public LocalDateTime getDataExpiracao() { return dataExpiracao; }
    public void setDataExpiracao(LocalDateTime dataExpiracao) { this.dataExpiracao = dataExpiracao; }
    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }
    public Consulta getConsultaGerada() { return consultaGerada; }
    public void setConsultaGerada(Consulta consultaGerada) { this.consultaGerada = consultaGerada; }
    public Integer getDiasAntecipados() { return diasAntecipados; }
    public void setDiasAntecipados(Integer diasAntecipados) { this.diasAntecipados = diasAntecipados; }
}
