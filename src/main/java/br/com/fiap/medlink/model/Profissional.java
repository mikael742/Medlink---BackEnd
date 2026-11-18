package br.com.fiap.medlink.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Profissional de saúde vinculado a uma clínica e a uma especialidade. */
public class Profissional extends Pessoa {

    @NotBlank(message = "O registro profissional (CRM) é obrigatório")
    @Size(max = 20)
    private String crm;

    @NotNull(message = "Informe a especialidade (especialidade.id)")
    private Especialidade especialidade;

    @NotNull(message = "Informe a clínica (clinica.id)")
    private Clinica clinica;

    public Profissional() {
    }

    public Profissional(Long id) {
        super(id);
    }

    @Override
    public String getTipo() {
        return "PROFISSIONAL";
    }

    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }
    public Especialidade getEspecialidade() { return especialidade; }
    public void setEspecialidade(Especialidade especialidade) { this.especialidade = especialidade; }
    public Clinica getClinica() { return clinica; }
    public void setClinica(Clinica clinica) { this.clinica = clinica; }
}
