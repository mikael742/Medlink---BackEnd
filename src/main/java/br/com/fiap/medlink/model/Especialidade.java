package br.com.fiap.medlink.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Especialidade {

    private Long id;

    @NotBlank(message = "O nome da especialidade é obrigatório")
    @Size(max = 80)
    private String nome;

    @Size(max = 255)
    private String descricao;

    public Especialidade() {
    }

    public Especialidade(Long id) {
        this.id = id;
    }

    public Especialidade(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
