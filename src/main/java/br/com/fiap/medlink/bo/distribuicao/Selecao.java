package br.com.fiap.medlink.bo.distribuicao;

import java.util.List;

/**
 * Resultado de uma estratégia: candidatos na ordem de prioridade
 * e o raio de busca usado (apenas no método de proximidade).
 */
public record Selecao(List<Candidato> ranking, Double raioBuscaKm) {

    public static Selecao semRaio(List<Candidato> ranking) {
        return new Selecao(ranking, null);
    }

    public boolean isVazia() {
        return ranking.isEmpty();
    }
}
