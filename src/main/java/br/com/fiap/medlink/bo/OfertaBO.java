package br.com.fiap.medlink.bo;

import br.com.fiap.medlink.dao.ConsultaDAO;
import br.com.fiap.medlink.dao.InteresseDAO;
import br.com.fiap.medlink.dao.OfertaDAO;
import br.com.fiap.medlink.dto.ResultadoResposta;
import br.com.fiap.medlink.exception.ConflitoException;
import br.com.fiap.medlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.medlink.exception.RegraNegocioException;
import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.InteresseConsulta;
import br.com.fiap.medlink.model.Oferta;
import br.com.fiap.medlink.model.enums.OrigemConsulta;
import br.com.fiap.medlink.model.enums.StatusConsulta;
import br.com.fiap.medlink.model.enums.StatusOferta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/** Respostas dos pacientes às ofertas: aceite, recusa e expiração automática. */
@Service
public class OfertaBO {

    private static final Logger log = LoggerFactory.getLogger(OfertaBO.class);

    private final OfertaDAO ofertaDAO;
    private final ConsultaDAO consultaDAO;
    private final InteresseDAO interesseDAO;
    private final DistribuicaoBO distribuicaoBO;
    private final TransactionTemplate transacao;
    private final Clock clock;

    public OfertaBO(OfertaDAO ofertaDAO, ConsultaDAO consultaDAO, InteresseDAO interesseDAO,
                    DistribuicaoBO distribuicaoBO, TransactionTemplate transacao, Clock clock) {
        this.ofertaDAO = ofertaDAO;
        this.consultaDAO = consultaDAO;
        this.interesseDAO = interesseDAO;
        this.distribuicaoBO = distribuicaoBO;
        this.transacao = transacao;
        this.clock = clock;
    }

    public List<Oferta> listar(Long idVaga, Long idPaciente, StatusOferta status, Long idClinica) {
        return ofertaDAO.listar(idVaga, idPaciente, status, idClinica);
    }

    public Oferta buscar(Long id) {
        if (id == null) throw new RegraNegocioException("Informe o id de Oferta");
        return ofertaDAO.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Oferta", id));
    }

    /**
     * O paciente aceita a vaga: cria a nova consulta (origem ENCAIXE), encerra o interesse e,
     * se ele estava antecipando uma consulta, cancela a antiga — que vira uma nova vaga
     * e é distribuída automaticamente (efeito cascata).
     */
    @Transactional(noRollbackFor = RegraNegocioException.class)
    public ResultadoResposta aceitar(Long idOferta) {
        LocalDateTime agora = LocalDateTime.now(clock);
        Oferta oferta = buscarPendente(idOferta, agora);
        Consulta vaga = consultaDAO.buscarPorId(oferta.getVaga().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta", oferta.getVaga().getId()));

        if (!vaga.isFutura(agora)) {
            throw new RegraNegocioException("O horário desta vaga já passou");
        }
        if (consultaDAO.profissionalOcupado(vaga.getProfissional().getId(), vaga.getDataHora(), null)) {
            throw new ConflitoException("Este horário já foi ocupado por outra consulta");
        }
        if (consultaDAO.pacienteOcupado(oferta.getPaciente().getId(), vaga.getDataHora())) {
            throw new ConflitoException("O paciente já possui outra consulta neste horário");
        }

        // Consulta que o paciente desejava antecipar (se houver e ainda estiver agendada)
        InteresseConsulta interesse = interesseDAO.buscarPorId(oferta.getInteresse().getId()).orElseThrow();
        Consulta antiga = null;
        Integer diasAntecipados = null;
        if (interesse.isAntecipacao()) {
            antiga = consultaDAO.buscarPorId(interesse.getConsultaAtual().getId())
                    .filter(c -> c.getStatus() == StatusConsulta.AGENDADA)
                    .orElse(null);
            if (antiga != null) {
                diasAntecipados = (int) ChronoUnit.DAYS.between(vaga.getDataHora().toLocalDate(),
                        antiga.getDataHora().toLocalDate());
            }
        }

        // 1) Nova consulta
        Consulta nova = new Consulta();
        nova.setPaciente(oferta.getPaciente());
        nova.setProfissional(vaga.getProfissional());
        nova.setDataHora(vaga.getDataHora());
        nova.setTipoAtendimento(vaga.getTipoAtendimento());
        nova.setStatus(StatusConsulta.AGENDADA);
        nova.setOrigem(OrigemConsulta.ENCAIXE);
        nova.setIdConsultaOrigem(vaga.getId());
        nova.setDataCriacao(agora);
        consultaDAO.inserir(nova);

        // 2) Oferta aceita e interesse encerrado
        oferta.aceitar(agora, nova, diasAntecipados);
        ofertaDAO.atualizarResposta(oferta);
        interesseDAO.desativar(interesse.getId());

        // 3) Efeito cascata: a consulta antiga vira uma nova vaga
        Oferta ofertaCascata = null;
        if (antiga != null) {
            antiga.cancelar("Antecipada pelo Medlink (oferta #" + oferta.getId() + ")", agora);
            consultaDAO.atualizar(antiga);
            ofertaCascata = distribuicaoBO.distribuir(antiga.getId()).orElse(null);
        }

        String mensagem = "Consulta confirmada para " + oferta.getPaciente().getNome() + " em " + vaga.getDataHora() + "."
                + (antiga != null ? " A consulta antiga (" + antiga.getId() + ") foi liberada como nova vaga." : "");
        log.info("[CONFIRMAÇÃO] {}", mensagem);
        return new ResultadoResposta(buscar(idOferta), consultaDAO.buscarPorId(nova.getId()).orElse(nova), null,
                antiga == null ? null : consultaDAO.buscarPorId(antiga.getId()).orElse(antiga), ofertaCascata, mensagem);
    }

    /** O paciente recusa: a vaga passa automaticamente para o próximo candidato. */
    @Transactional(noRollbackFor = RegraNegocioException.class)
    public ResultadoResposta recusar(Long idOferta) {
        LocalDateTime agora = LocalDateTime.now(clock);
        Oferta oferta = buscarPendente(idOferta, agora);
        oferta.recusar(agora);
        ofertaDAO.atualizarResposta(oferta);

        Optional<Oferta> proxima = distribuicaoBO.distribuir(oferta.getVaga().getId());
        String mensagem = proxima
                .map(p -> "Oferta recusada. A vaga foi oferecida a " + p.getPaciente().getNome() + ".")
                .orElse("Oferta recusada. Não há outros candidatos compatíveis no momento.");
        return new ResultadoResposta(buscar(idOferta), null, proxima.orElse(null), null, null, mensagem);
    }

    /**
     * Roda a cada minuto: ofertas sem resposta dentro do prazo expiram
     * e a vaga segue para o próximo candidato.
     */
    @Scheduled(fixedDelayString = "${medlink.expiracao.intervalo-ms:60000}", initialDelay = 30000)
    public void processarExpiradas() {
        LocalDateTime agora = LocalDateTime.now(clock);
        for (Oferta oferta : ofertaDAO.listarPendentesVencidas(agora)) {
            try {
                transacao.executeWithoutResult(status -> expirarEDistribuir(oferta, agora));
            } catch (Exception e) {
                log.error("Falha ao expirar a oferta {}", oferta.getId(), e);
            }
        }
    }

    private void expirarEDistribuir(Oferta oferta, LocalDateTime agora) {
        oferta.expirar(agora);
        ofertaDAO.atualizarResposta(oferta);
        log.info("Oferta {} expirou sem resposta de {}", oferta.getId(), oferta.getPaciente().getNome());
        distribuicaoBO.distribuir(oferta.getVaga().getId());
    }

    /** Garante que a oferta ainda pode ser respondida; se o prazo venceu, expira e repassa a vaga. */
    private Oferta buscarPendente(Long idOferta, LocalDateTime agora) {
        Oferta oferta = buscar(idOferta);
        if (oferta.isExpirada(agora)) {
            expirarEDistribuir(oferta, agora);
            throw new RegraNegocioException("O prazo para responder esta oferta expirou e a vaga foi repassada");
        }
        if (!oferta.isPendente()) {
            throw new RegraNegocioException("Esta oferta já foi respondida (status: " + oferta.getStatus() + ")");
        }
        return oferta;
    }
}
