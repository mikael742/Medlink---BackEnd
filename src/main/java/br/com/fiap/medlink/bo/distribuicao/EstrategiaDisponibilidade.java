package br.com.fiap.medlink.bo.distribuicao;

import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.enums.MetodoDistribuicao;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Antecedência intermediária (padrão: entre 6h e 24h).
 * Considera apenas quem declarou disponibilidade para o dia/horário
 * (já garantido na análise de compatibilidade) e respeita a ordem da lista,
 * mas com um prazo de resposta menor para a vaga girar mais rápido.
 */
@Component
@Order(2)
public class EstrategiaDisponibilidade implements EstrategiaDistribuicao {

    /** Prazo máximo de resposta neste método. */
    private static final int PRAZO_MAXIMO_MIN = 30;

    @Override
    public MetodoDistribuicao getMetodo() {
        return MetodoDistribuicao.DISPONIBILIDADE;
    }

    @Override
    public boolean seAplica(long minutosRestantes, Clinica clinica) {
        return minutosRestantes >= clinica.getHorasLimiteUrgencia() * 60L
                && minutosRestantes < clinica.getHorasLimiteFila() * 60L;
    }

    @Override
    public Selecao selecionar(List<Candidato> compativeis, ContextoVaga contexto) {
        return Selecao.semRaio(compativeis.stream().sorted(EstrategiaFilaEspera.ORDEM_DE_ENTRADA).toList());
    }

    @Override
    public int prazoRespostaMinutos(Clinica clinica) {
        return Math.max(clinica.getPrazoRespostaUrgenteMin(), Math.min(clinica.getPrazoRespostaMin(), PRAZO_MAXIMO_MIN));
    }
}
