package br.com.fiap.medlink.dto;

import jakarta.validation.constraints.Size;


public record CancelamentoRequest(
        @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres")
        String motivo
) {
}
