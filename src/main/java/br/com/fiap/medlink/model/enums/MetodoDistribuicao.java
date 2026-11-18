package br.com.fiap.medlink.model.enums;

/**
 * Método usado pelo Medlink para escolher quem recebe uma vaga.
 * A escolha depende de quanto tempo falta até o horário da consulta.
 */
public enum MetodoDistribuicao {
    /** Cancelamento com bastante antecedência: ordem de entrada na lista de interesse. */
    FILA_ESPERA,
    /** Antecedência intermediária: disponibilidade + ordem da lista, com prazo de resposta reduzido. */
    DISPONIBILIDADE,
    /** Última hora: prioridade para quem chega mais rápido à clínica. */
    PROXIMIDADE
}
