package br.com.fiap.medlink.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public class Paciente extends Pessoa {

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos numéricos")
    private String cpf;

    @Past(message = "A data de nascimento deve estar no passado")
    private LocalDate dataNascimento;

    /** Ex.: UNIMED, AMIL, SULAMERICA, PARTICULAR. */
    private String convenio;

    @Valid
    private Localizacao localizacao;

    /** LGPD: a localização só é usada no método de proximidade com o consentimento do paciente. */
    private Boolean consenteLocalizacao;

    public Paciente() {
    }

    public Paciente(Long id) {
        super(id);
    }

    @Override
    public String getTipo() {
        return "PACIENTE";
    }

    /** O paciente só participa da distribuição por proximidade se consentiu e tem coordenadas. */
    public boolean podeUsarLocalizacao() {
        return Boolean.TRUE.equals(consenteLocalizacao) && localizacao != null && localizacao.temCoordenadas();
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getConvenio() { return convenio; }
    public void setConvenio(String convenio) { this.convenio = convenio; }
    public Localizacao getLocalizacao() { return localizacao; }
    public void setLocalizacao(Localizacao localizacao) { this.localizacao = localizacao; }
    public Boolean getConsenteLocalizacao() { return consenteLocalizacao; }
    public void setConsenteLocalizacao(Boolean consenteLocalizacao) { this.consenteLocalizacao = consenteLocalizacao; }
}
