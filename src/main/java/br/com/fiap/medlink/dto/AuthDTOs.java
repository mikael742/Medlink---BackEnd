package br.com.fiap.medlink.dto;

import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.Paciente;
import br.com.fiap.medlink.model.enums.PerfilUsuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public final class AuthDTOs {

    private AuthDTOs() {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido") String email,
            @NotBlank(message = "Informe a senha") String senha
    ) {
    }

    public record CadastroPacienteRequest(
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido") String email,
            @NotBlank(message = "Informe a senha") @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres") String senha,
            @NotNull(message = "Informe os dados do paciente") @Valid Paciente paciente
    ) {
    }

    public record CadastroClinicaRequest(
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido") String email,
            @NotBlank(message = "Informe a senha") @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres") String senha,
            @NotNull(message = "Informe os dados da clínica") @Valid Clinica clinica
    ) {
    }

    /** O que o front guarda depois do login para saber quem é a pessoa e o que ela pode ver. */
    public record Sessao(
            Long idUsuario,
            String email,
            PerfilUsuario perfil,
            String nome,
            Long idPaciente,
            Long idClinica
    ) {
    }
}
