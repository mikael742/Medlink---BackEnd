package br.com.fiap.medlink.dto;

import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.Oferta;

/**
 * Resposta de um aceite ou recusa de oferta.
 *
 * @param consultaGerada         consulta criada para o paciente (somente no aceite)
 * @param proximaOferta          nova oferta enviada para a mesma vaga (somente na recusa)
 * @param consultaLiberada       consulta antiga que o paciente antecipou e que virou uma nova vaga
 * @param ofertaDaVagaLiberada   primeira oferta enviada para essa nova vaga (efeito cascata)
 */
public record ResultadoResposta(
        Oferta oferta,
        Consulta consultaGerada,
        Oferta proximaOferta,
        Consulta consultaLiberada,
        Oferta ofertaDaVagaLiberada,
        String mensagem
) {
}
