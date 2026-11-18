package br.com.fiap.medlink.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Superclasse com os dados comuns a pacientes e profissionais de saúde. */
public abstract class Pessoa {

    protected Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    protected String nome;

    @Email(message = "E-mail inválido")
    @Size(max = 120)
    protected String email;

    @Size(max = 20)
    protected String telefone;

    protected Pessoa() {
    }

    protected Pessoa(Long id) {
        this.id = id;
    }

    /** Cada tipo de pessoa descreve seu papel no sistema (polimorfismo). */
    public abstract String getTipo();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
