package br.com.fiap.medlink.dto;

import br.com.fiap.medlink.model.enums.MetodoDistribuicao;

import java.util.Map;

/** Indicadores de resultado do Medlink para uma clínica (seção 8 da proposta). */
public record Indicadores(
        Long idClinica,
        String nomeClinica,
        long consultasCanceladas,
        long vagasRecuperadas,
        double percentualRecuperado,
        Double mediaDiasAntecipados,
        Double tempoMedioAteConfirmacaoMin,
        long ofertasEnviadas,
        long ofertasAceitas,
        long ofertasRecusadas,
        long ofertasExpiradas,
        double percentualOfertasAceitas,
        Map<MetodoDistribuicao, Long> vagasRecuperadasPorMetodo
) {
}
