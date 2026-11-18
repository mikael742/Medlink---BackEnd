package br.com.fiap.medlink.bo;

import br.com.fiap.medlink.bo.distribuicao.AnalisadorCompatibilidade;
import br.com.fiap.medlink.bo.distribuicao.Candidato;
import br.com.fiap.medlink.bo.distribuicao.ContextoVaga;
import br.com.fiap.medlink.bo.distribuicao.EstrategiaDistribuicao;
import br.com.fiap.medlink.bo.distribuicao.Selecao;
import br.com.fiap.medlink.dao.ClinicaDAO;
import br.com.fiap.medlink.dao.ConsultaDAO;
import br.com.fiap.medlink.dao.OfertaDAO;
import br.com.fiap.medlink.dao.ProfissionalDAO;
import br.com.fiap.medlink.dto.SimulacaoDistribuicao;
import br.com.fiap.medlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.medlink.exception.RegraNegocioException;
import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.Oferta;
import br.com.fiap.medlink.model.Profissional;
import br.com.fiap.medlink.model.enums.StatusConsulta;
import br.com.fiap.medlink.model.enums.StatusOferta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Coração do Medlink: escolhe o método de distribuição conforme o tempo
 * restante até a consulta e envia a oferta da vaga ao candidato prioritário.
 * Garante que cada vaga tenha apenas uma oferta ativa por vez.
 */
@Service
public class DistribuicaoBO {

    private static final Logger log = LoggerFactory.getLogger(DistribuicaoBO.class);

    private final ConsultaDAO consultaDAO;
    private final ProfissionalDAO profissionalDAO;
    private final ClinicaDAO clinicaDAO;
    private final OfertaDAO ofertaDAO;
    private final AnalisadorCompatibilidade analisador;
    private final List<EstrategiaDistribuicao> estrategias;
    private final Clock clock;

    public DistribuicaoBO(ConsultaDAO consultaDAO, ProfissionalDAO profissionalDAO, ClinicaDAO clinicaDAO,
                          OfertaDAO ofertaDAO, AnalisadorCompatibilidade analisador,
                          List<EstrategiaDistribuicao> estrategias, Clock clock) {
        this.consultaDAO = consultaDAO;
        this.profissionalDAO = profissionalDAO;
        this.clinicaDAO = clinicaDAO;
        this.ofertaDAO = ofertaDAO;
        this.analisador = analisador;
        this.estrategias = estrategias;
        this.clock = clock;
    }

    /**
     * Envia a próxima oferta de uma vaga (consulta CANCELADA).
     *
     * @return a oferta enviada, ou vazio se não há candidatos / a vaga já passou / já foi preenchida
     */
    @Transactional
    public Optional<Oferta> distribuir(Long idVaga) {
        ContextoVaga ctx = montarContexto(idVaga, true);
        Consulta vaga = ctx.vaga();

        if (!vaga.isFutura(ctx.agora())) {
            log.info("Vaga {} não será distribuída: o horário já passou", idVaga);
            return Optional.empty();
        }
        if (ofertaDAO.vagaJaPreenchida(idVaga)) {
            return Optional.empty();
        }
        Optional<Oferta> pendente = ofertaDAO.buscarPendentePorVaga(idVaga);
        if (pendente.isPresent()) {
            return pendente; // regra: apenas uma oferta ativa por vaga
        }

        EstrategiaDistribuicao estrategia = escolherEstrategia(ctx);
        AnalisadorCompatibilidade.Resultado analise = analisador.analisar(ctx);
        Selecao selecao = estrategia.selecionar(analise.compativeis(), ctx);

        if (selecao.isVazia()) {
            log.info("Vaga {} ({}): nenhum candidato compatível disponível", idVaga, estrategia.getMetodo());
            return Optional.empty();
        }

        Candidato escolhido = selecao.ranking().get(0);
        Oferta oferta = new Oferta();
        oferta.setVaga(vaga);
        oferta.setInteresse(escolhido.interesse());
        oferta.setPaciente(escolhido.paciente());
        oferta.setMetodo(estrategia.getMetodo());
        oferta.setStatus(StatusOferta.PENDENTE);
        oferta.setTentativa(ofertaDAO.contarPorVaga(idVaga) + 1);
        oferta.setDistanciaKm(escolhido.distanciaKm());
        oferta.setTempoEstimadoMin(escolhido.tempoEstimadoMin());
        oferta.setRaioBuscaKm(selecao.raioBuscaKm());
        oferta.setDataEnvio(ctx.agora());
        oferta.setDataExpiracao(calcularExpiracao(ctx, estrategia, escolhido));
        ofertaDAO.inserir(oferta);

        // No MVP a "notificação" é registrada em log; em produção seria push/SMS/WhatsApp.
        log.info("[NOTIFICAÇÃO] Oferta {} enviada a {} para a vaga {} em {} (método {}, responder até {})",
                oferta.getId(), escolhido.paciente().getNome(), idVaga, vaga.getDataHora(),
                estrategia.getMetodo(), oferta.getDataExpiracao());
        return Optional.of(oferta);
    }

    /**
     * Mostra como a vaga seria distribuída agora, sem enviar oferta.
     * Funciona também para consultas AGENDADAS ("e se ela fosse cancelada agora?").
     */
    public SimulacaoDistribuicao simular(Long idConsulta) {
        ContextoVaga ctx = montarContexto(idConsulta, false);
        if (!ctx.vaga().isFutura(ctx.agora())) {
            throw new RegraNegocioException("O horário desta consulta já passou");
        }
        EstrategiaDistribuicao estrategia = escolherEstrategia(ctx);
        AnalisadorCompatibilidade.Resultado analise = analisador.analisar(ctx);
        Selecao selecao = estrategia.selecionar(analise.compativeis(), ctx);

        List<SimulacaoDistribuicao.PosicaoRanking> ranking = new ArrayList<>();
        int posicao = 1;
        for (Candidato c : selecao.ranking()) {
            ranking.add(new SimulacaoDistribuicao.PosicaoRanking(posicao++, c.paciente().getId(),
                    c.paciente().getNome(), c.interesse().getId(), c.interesse().getDataEntrada(),
                    c.distanciaKm(), c.tempoEstimadoMin()));
        }
        // Candidatos compatíveis que a estratégia descartou (ex.: longe demais ou sem localização)
        List<AnalisadorCompatibilidade.Exclusao> excluidos = new ArrayList<>(analise.excluidos());
        for (Candidato c : analise.compativeis()) {
            boolean noRanking = selecao.ranking().stream()
                    .anyMatch(r -> r.interesse().getId().equals(c.interesse().getId()));
            if (!noRanking) {
                excluidos.add(new AnalisadorCompatibilidade.Exclusao(c.interesse().getId(), c.paciente().getId(),
                        c.paciente().getNome(), c.paciente().podeUsarLocalizacao()
                        ? "Fora do raio de busca ou não chega a tempo"
                        : "Não autorizou o uso da localização (necessário em vagas de última hora)"));
            }
        }

        return new SimulacaoDistribuicao(idConsulta, ctx.vaga().getDataHora(), ctx.minutosRestantes(),
                estrategia.getMetodo(), explicar(estrategia, ctx.clinica()), selecao.raioBuscaKm(),
                estrategia.prazoRespostaMinutos(ctx.clinica()), ranking, excluidos);
    }

    public EstrategiaDistribuicao escolherEstrategia(ContextoVaga ctx) {
        long minutos = ctx.minutosRestantes();
        return estrategias.stream()
                .filter(e -> e.seAplica(minutos, ctx.clinica()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhuma estratégia para " + minutos + " minutos"));
    }

    private ContextoVaga montarContexto(Long idConsulta, boolean exigirCancelada) {
        Consulta vaga = consultaDAO.buscarPorId(idConsulta)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta", idConsulta));
        if (exigirCancelada && vaga.getStatus() != StatusConsulta.CANCELADA) {
            throw new RegraNegocioException("A consulta " + idConsulta + " não é uma vaga aberta (status " + vaga.getStatus() + ")");
        }
        if (vaga.getStatus() == StatusConsulta.REALIZADA) {
            throw new RegraNegocioException("A consulta " + idConsulta + " já foi realizada");
        }
        Profissional profissional = profissionalDAO.buscarPorId(vaga.getProfissional().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", vaga.getProfissional().getId()));
        Clinica clinica = clinicaDAO.buscarPorId(profissional.getClinica().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clínica", profissional.getClinica().getId()));
        return new ContextoVaga(vaga, profissional, clinica, LocalDateTime.now(clock),
                ofertaDAO.maiorRaioUsado(idConsulta));
    }

    /** Prazo de resposta da estratégia, sem nunca passar do horário em que o paciente precisa sair de casa. */
    private LocalDateTime calcularExpiracao(ContextoVaga ctx, EstrategiaDistribuicao estrategia, Candidato c) {
        LocalDateTime pelaRegra = ctx.agora().plusMinutes(estrategia.prazoRespostaMinutos(ctx.clinica()));
        int deslocamento = c.tempoEstimadoMin() == null ? 0 : c.tempoEstimadoMin();
        LocalDateTime limite = ctx.vaga().getDataHora().minusMinutes(deslocamento);
        LocalDateTime expiracao = pelaRegra.isBefore(limite) ? pelaRegra : limite;
        return expiracao.isAfter(ctx.agora()) ? expiracao : ctx.agora().plusMinutes(1);
    }

    private String explicar(EstrategiaDistribuicao estrategia, Clinica c) {
        return switch (estrategia.getMetodo()) {
            case FILA_ESPERA -> "Faltam mais de " + c.getHorasLimiteFila()
                    + "h: a vaga segue a ordem de entrada na lista de interesse.";
            case DISPONIBILIDADE -> "Faltam entre " + c.getHorasLimiteUrgencia() + "h e " + c.getHorasLimiteFila()
                    + "h: considera a disponibilidade declarada e a ordem da lista, com prazo de resposta reduzido.";
            case PROXIMIDADE -> "Faltam menos de " + c.getHorasLimiteUrgencia()
                    + "h: prioridade para quem chega mais rápido à clínica, ampliando o raio se necessário.";
        };
    }
}
