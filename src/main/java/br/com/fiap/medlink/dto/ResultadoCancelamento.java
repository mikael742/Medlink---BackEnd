package br.com.fiap.medlink.dto;

import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.Oferta;

/** Resposta do cancelamento: a consulta cancelada e a primeira oferta enviada (se houver candidato). */
public record ResultadoCancelamento(
        Consulta consultaCancelada,
        Oferta ofertaEnviada,
        String mensagem
) {
}
