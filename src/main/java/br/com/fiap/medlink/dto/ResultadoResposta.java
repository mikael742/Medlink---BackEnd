package br.com.fiap.medlink.dto;

import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.Oferta;


public record ResultadoResposta(
        Oferta oferta,
        Consulta consultaGerada,
        Oferta proximaOferta,
        Consulta consultaLiberada,
        Oferta ofertaDaVagaLiberada,
        String mensagem
) {
}
