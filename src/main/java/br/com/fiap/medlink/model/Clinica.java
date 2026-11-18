package br.com.fiap.medlink.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Arrays;

/**
 * Clínica parceira. Além dos dados cadastrais, guarda os parâmetros
 * configuráveis que definem qual método de distribuição será usado.
 */
public class Clinica {

    private Long id;

    @NotBlank(message = "O nome da clínica é obrigatório")
    @Size(max = 120)
    private String nome;

    @NotBlank(message = "O CNPJ é obrigatório")
    @Pattern(regexp = "\\d{14}", message = "O CNPJ deve conter 14 dígitos numéricos")
    private String cnpj;

    @Size(max = 20)
    private String telefone;

    @Size(max = 120)
    private String email;

    @Size(max = 200)
    private String endereco;

    @NotNull(message = "A localização da clínica é obrigatória")
    @Valid
    private Localizacao localizacao;

    /** Convênios aceitos separados por vírgula. Ex.: "UNIMED,AMIL,PARTICULAR". */
    @Size(max = 500)
    private String conveniosAceitos;

    // ---------- Parâmetros de distribuição (configuráveis pela clínica) ----------

    /** Acima deste número de horas até a consulta, usa a FILA_ESPERA. Padrão: 24h (aplicado no BO se não informado). */
    @Min(1)
    private Integer horasLimiteFila;

    /** Abaixo deste número de horas, usa PROXIMIDADE. Entre os dois, DISPONIBILIDADE. Padrão: 6h. */
    @Min(1)
    private Integer horasLimiteUrgencia;

    /** Raio inicial de busca no método de proximidade. */
    @Positive
    private Double raioInicialKm;

    /** Raio máximo até onde a busca pode ser ampliada. */
    @Positive
    private Double raioMaximoKm;

    /** Prazo para o paciente responder uma oferta (fila de espera). */
    @Min(5)
    private Integer prazoRespostaMin;

    /** Prazo para responder uma oferta de última hora. */
    @Min(1)
    private Integer prazoRespostaUrgenteMin;

    public Clinica() {
    }

    public Clinica(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public boolean aceitaConvenio(String convenio) {
        if (conveniosAceitos == null || conveniosAceitos.isBlank()) return true;
        if (convenio == null || convenio.isBlank()) convenio = "PARTICULAR";
        String alvo = convenio.trim();
        return Arrays.stream(conveniosAceitos.split(","))
                .map(String::trim)
                .anyMatch(c -> c.equalsIgnoreCase(alvo));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public Localizacao getLocalizacao() { return localizacao; }
    public void setLocalizacao(Localizacao localizacao) { this.localizacao = localizacao; }
    public String getConveniosAceitos() { return conveniosAceitos; }
    public void setConveniosAceitos(String conveniosAceitos) { this.conveniosAceitos = conveniosAceitos; }
    public Integer getHorasLimiteFila() { return horasLimiteFila; }
    public void setHorasLimiteFila(Integer horasLimiteFila) { this.horasLimiteFila = horasLimiteFila; }
    public Integer getHorasLimiteUrgencia() { return horasLimiteUrgencia; }
    public void setHorasLimiteUrgencia(Integer horasLimiteUrgencia) { this.horasLimiteUrgencia = horasLimiteUrgencia; }
    public Double getRaioInicialKm() { return raioInicialKm; }
    public void setRaioInicialKm(Double raioInicialKm) { this.raioInicialKm = raioInicialKm; }
    public Double getRaioMaximoKm() { return raioMaximoKm; }
    public void setRaioMaximoKm(Double raioMaximoKm) { this.raioMaximoKm = raioMaximoKm; }
    public Integer getPrazoRespostaMin() { return prazoRespostaMin; }
    public void setPrazoRespostaMin(Integer prazoRespostaMin) { this.prazoRespostaMin = prazoRespostaMin; }
    public Integer getPrazoRespostaUrgenteMin() { return prazoRespostaUrgenteMin; }
    public void setPrazoRespostaUrgenteMin(Integer prazoRespostaUrgenteMin) { this.prazoRespostaUrgenteMin = prazoRespostaUrgenteMin; }
}
