package br.com.fiap.medlink.bo.distribuicao;

import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.enums.MetodoDistribuicao;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * MÉTODO 1 — Cancelamento com antecedência (padrão: mais de 24h).
 * A vaga é oferecida seguindo a ordem de entrada na lista de interesse.
 */
@Component
@Order(1)
public class EstrategiaFilaEspera implements EstrategiaDistribuicao {

    /** Critério da fila: quem entrou primeiro tem prioridade; desempate pelo ID. */
    static final Comparator<Candidato> ORDEM_DE_ENTRADA = Comparator
            .comparing((Candidato c) -> c.interesse().getDataEntrada())
            .thenComparing(c -> c.interesse().getId());

    @Override
    public MetodoDistribuicao getMetodo() {
        return MetodoDistribuicao.FILA_ESPERA;
    }

    @Override
    public boolean seAplica(long minutosRestantes, Clinica clinica) {
        return minutosRestantes >= clinica.getHorasLimiteFila() * 60L;
    }

    @Override
    public Selecao selecionar(List<Candidato> compativeis, ContextoVaga contexto) {
        return Selecao.semRaio(compativeis.stream().sorted(ORDEM_DE_ENTRADA).toList());
    }

    @Override
    public int prazoRespostaMinutos(Clinica clinica) {
        return clinica.getPrazoRespostaMin();
    }
}
