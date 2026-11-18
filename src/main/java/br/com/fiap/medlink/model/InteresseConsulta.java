package br.com.fiap.medlink.model;

import br.com.fiap.medlink.model.enums.DiaSemana;
import br.com.fiap.medlink.model.enums.TipoAtendimento;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;


public class InteresseConsulta {

    private Long id;

    @NotNull(message = "Informe o paciente (paciente.id)")
    private Paciente paciente;

    @NotNull(message = "Informe a especialidade (especialidade.id)")
    private Especialidade especialidade;

    /** Opcional: profissional de preferência. */
    private Profissional profissional;

    /** Opcional: consulta já agendada que o paciente deseja antecipar. */
    private Consulta consultaAtual;

    /** Dias disponíveis separados por vírgula. Ex.: "SEG,QUA,SEX". */
    @NotBlank(message = "Informe os dias disponíveis. Ex.: SEG,QUA,SEX")
    @Pattern(regexp = "^(SEG|TER|QUA|QUI|SEX|SAB|DOM)(,(SEG|TER|QUA|QUI|SEX|SAB|DOM))*$",
            message = "Dias inválidos. Use SEG,TER,QUA,QUI,SEX,SAB,DOM separados por vírgula")
    private String diasSemana;

    @NotNull(message = "Informe o horário inicial (HH:mm)")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotNull(message = "Informe o horário final (HH:mm)")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFim;

    private TipoAtendimento tipoAtendimento;

    /** Momento de entrada na lista — define a ordem da fila. */
    private LocalDateTime dataEntrada;

    private Boolean ativo;

    public InteresseConsulta() {
    }

    public InteresseConsulta(Long id) {
        this.id = id;
    }

    // ---------------- Regras do domínio ----------------

    @JsonIgnore
    public Set<DiaSemana> getDias() {
        if (diasSemana == null || diasSemana.isBlank()) return EnumSet.noneOf(DiaSemana.class);
        return Arrays.stream(diasSemana.split(","))
                .map(String::trim)
                .map(DiaSemana::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DiaSemana.class)));
    }

    /** O paciente declarou disponibilidade para este dia e horário? */
    @JsonIgnore
    public boolean isDisponivelEm(LocalDateTime dataHora) {
        if (!getDias().contains(DiaSemana.de(dataHora.getDayOfWeek()))) return false;
        LocalTime hora = dataHora.toLocalTime();
        return !hora.isBefore(horaInicio) && !hora.isAfter(horaFim);
    }

    @JsonIgnore
    public boolean isAntecipacao() {
        return consultaAtual != null && consultaAtual.getId() != null;
    }

    // ---------------- Getters e setters ----------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Especialidade getEspecialidade() { return especialidade; }
    public void setEspecialidade(Especialidade especialidade) { this.especialidade = especialidade; }
    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional profissional) { this.profissional = profissional; }
    public Consulta getConsultaAtual() { return consultaAtual; }
    public void setConsultaAtual(Consulta consultaAtual) { this.consultaAtual = consultaAtual; }
    public String getDiasSemana() { return diasSemana; }
    public void setDiasSemana(String diasSemana) { this.diasSemana = diasSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public TipoAtendimento getTipoAtendimento() { return tipoAtendimento; }
    public void setTipoAtendimento(TipoAtendimento tipoAtendimento) { this.tipoAtendimento = tipoAtendimento; }
    public LocalDateTime getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDateTime dataEntrada) { this.dataEntrada = dataEntrada; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
