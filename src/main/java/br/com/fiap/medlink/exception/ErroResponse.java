package br.com.fiap.medlink.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/** Corpo padrão de todas as respostas de erro da API. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<CampoInvalido> campos
) {
    public record CampoInvalido(String campo, String mensagem) {
    }
}
