package br.com.fiap.medlink.bo.distribuicao;

import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.Localizacao;
import br.com.fiap.medlink.model.enums.MetodoDistribuicao;
import br.com.fiap.medlink.model.enums.TipoAtendimento;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * MÉTODO 2 — Cancelamento de última hora (padrão: menos de 6h).
 * Prioriza quem tem o menor tempo estimado de deslocamento até a clínica
 * e consegue chegar a tempo. Se ninguém estiver no raio inicial, a busca
 * é ampliada progressivamente (o raio dobra) até o raio máximo da clínica.
 */
@Component
@Order(3)
public class EstrategiaProximidade implements EstrategiaDistribuicao {

    /** Velocidade média urbana considerada para estimar o deslocamento (km/h). */
    static final double VELOCIDADE_MEDIA_KMH = 20.0;
    /** Minutos fixos somados à estimativa (sair de casa, estacionar, recepção). */
    static final int MINUTOS_PREPARACAO = 10;

    @Override
    public MetodoDistribuicao getMetodo() {
        return MetodoDistribuicao.PROXIMIDADE;
    }

    @Override
    public boolean seAplica(long minutosRestantes, Clinica clinica) {
        return minutosRestantes < clinica.getHorasLimiteUrgencia() * 60L;
    }

    @Override
    public Selecao selecionar(List<Candidato> compativeis, ContextoVaga ctx) {
        Localizacao localClinica = ctx.clinica().getLocalizacao();

        // Teleconsulta (ou clínica sem coordenadas): distância não faz sentido -> ordem da fila
        if (ctx.vaga().getTipoAtendimento() == TipoAtendimento.TELECONSULTA
                || localClinica == null || !localClinica.temCoordenadas()) {
            return Selecao.semRaio(compativeis.stream().sorted(EstrategiaFilaEspera.ORDEM_DE_ENTRADA).toList());
        }

        // 1) Calcula o deslocamento de quem autorizou o uso da localização
        long minutosRestantes = ctx.minutosRestantes();
        List<Candidato> alcancaveis = new ArrayList<>();
        for (Candidato c : compativeis) {
            if (!c.paciente().podeUsarLocalizacao()) continue;
            double km = localClinica.distanciaKmAte(c.paciente().getLocalizacao());
            int minutos = estimarMinutos(km);
            if (minutos <= minutosRestantes) {          // consegue chegar antes do horário
                alcancaveis.add(c.comDeslocamento(km, minutos));
            }
        }

        // 2) Ampliação progressiva do raio
        Clinica clinica = ctx.clinica();
        double raio = Math.max(clinica.getRaioInicialKm(), ctx.raioJaUsadoKm() == null ? 0 : ctx.raioJaUsadoKm());
        while (true) {
            final double raioAtual = raio;
            List<Candidato> noRaio = alcancaveis.stream()
                    .filter(c -> c.distanciaKm() <= raioAtual)
                    .sorted(Comparator.comparing(Candidato::tempoEstimadoMin)
                            .thenComparing(EstrategiaFilaEspera.ORDEM_DE_ENTRADA))
                    .toList();
            if (!noRaio.isEmpty() || raio >= clinica.getRaioMaximoKm()) {
                return new Selecao(noRaio, raio);
            }
            raio = Math.min(raio * 2, clinica.getRaioMaximoKm());
        }
    }

    static int estimarMinutos(double km) {
        return (int) Math.ceil(km / VELOCIDADE_MEDIA_KMH * 60) + MINUTOS_PREPARACAO;
    }

    @Override
    public int prazoRespostaMinutos(Clinica clinica) {
        return clinica.getPrazoRespostaUrgenteMin();
    }
}
