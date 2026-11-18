package br.com.fiap.medlink.dto;

import br.com.fiap.medlink.bo.distribuicao.AnalisadorCompatibilidade.Exclusao;
import br.com.fiap.medlink.model.enums.MetodoDistribuicao;

import java.time.LocalDateTime;
import java.util.List;


public record SimulacaoDistribuicao(
        Long idVaga,
        LocalDateTime dataHoraVaga,
        long minutosRestantes,
        MetodoDistribuicao metodo,
        String explicacaoMetodo,
        Double raioBuscaKm,
        int prazoRespostaMin,
        List<PosicaoRanking> ranking,
        List<Exclusao> excluidos
) {
    public record PosicaoRanking(
            int posicao,
            Long idPaciente,
            String nomePaciente,
            Long idInteresse,
            LocalDateTime entradaNaLista,
            Double distanciaKm,
            Integer tempoEstimadoMin
    ) {
    }
}
