package br.com.fiap.medlink.dto;

import jakarta.validation.constraints.Size;

/** Corpo opcional do cancelamento de uma consulta. */
public record CancelamentoRequest(
        @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres")
        String motivo
) {
}
