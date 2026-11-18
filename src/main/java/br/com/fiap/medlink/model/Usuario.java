package br.com.fiap.medlink.model;

import br.com.fiap.medlink.model.enums.PerfilUsuario;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

public class Usuario {

    private Long id;
    private String email;

    @JsonIgnore
    private String senhaHash;

    @JsonIgnore
    private String salt;

    private PerfilUsuario perfil;
    private Long idPaciente;
    private Long idClinica;
    private LocalDateTime dataCriacao;

    public Usuario() {
    }

    public boolean isPaciente() {
        return perfil == PerfilUsuario.PACIENTE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    public PerfilUsuario getPerfil() { return perfil; }
    public void setPerfil(PerfilUsuario perfil) { this.perfil = perfil; }
    public Long getIdPaciente() { return idPaciente; }
    public void setIdPaciente(Long idPaciente) { this.idPaciente = idPaciente; }
    public Long getIdClinica() { return idClinica; }
    public void setIdClinica(Long idClinica) { this.idClinica = idClinica; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
