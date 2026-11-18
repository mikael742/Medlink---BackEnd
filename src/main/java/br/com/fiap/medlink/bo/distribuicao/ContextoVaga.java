package br.com.fiap.medlink.bo.distribuicao;

import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.Profissional;

import java.time.LocalDateTime;

/**
 * Tudo o que as estratégias precisam saber sobre a vaga no momento da distribuição.
 *
 * @param raioJaUsadoKm maior raio de busca já usado em ofertas anteriores desta vaga (ou null)
 */
public record ContextoVaga(
        Consulta vaga,
        Profissional profissional,
        Clinica clinica,
        LocalDateTime agora,
        Double raioJaUsadoKm
) {
    public long minutosRestantes() {
        return vaga.minutosAte(agora);
    }
}
