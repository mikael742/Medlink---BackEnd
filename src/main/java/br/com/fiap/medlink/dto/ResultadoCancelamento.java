package br.com.fiap.medlink.dto;

import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.Oferta;

public record ResultadoCancelamento(
        Consulta consultaCancelada,
        Oferta ofertaEnviada,
        String mensagem
) {
}
