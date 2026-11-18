package br.com.fiap.medlink.bo;

import br.com.fiap.medlink.dao.ConsultaDAO;
import br.com.fiap.medlink.dto.ResultadoCancelamento;
import br.com.fiap.medlink.exception.ConflitoException;
import br.com.fiap.medlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.medlink.exception.RegraNegocioException;
import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.Oferta;
import br.com.fiap.medlink.model.enums.OrigemConsulta;
import br.com.fiap.medlink.model.enums.StatusConsulta;
import br.com.fiap.medlink.model.enums.TipoAtendimento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultaBO {

    private final ConsultaDAO dao;
    private final PacienteBO pacienteBO;
    private final ProfissionalBO profissionalBO;
    private final DistribuicaoBO distribuicaoBO;
    private final Clock clock;

    public ConsultaBO(ConsultaDAO dao, PacienteBO pacienteBO, ProfissionalBO profissionalBO,
                      DistribuicaoBO distribuicaoBO, Clock clock) {
        this.dao = dao;
        this.pacienteBO = pacienteBO;
        this.profissionalBO = profissionalBO;
        this.distribuicaoBO = distribuicaoBO;
        this.clock = clock;
    }

    public List<Consulta> listar(StatusConsulta status, Long idPaciente, Long idProfissional, Long idClinica) {
        return dao.listar(status, idPaciente, idProfissional, idClinica);
    }

    public Consulta buscar(Long id) {
        if (id == null) throw new RegraNegocioException("Informe o id de Consulta");
        return dao.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Consulta", id));
    }

    @Transactional
    public Consulta agendar(Consulta c) {
        c.setId(null);
        pacienteBO.buscar(c.getPaciente().getId());
        profissionalBO.buscar(c.getProfissional().getId());
        if (c.getTipoAtendimento() == null) c.setTipoAtendimento(TipoAtendimento.PRESENCIAL);
        if (c.getTipoAtendimento() == TipoAtendimento.QUALQUER) {
            throw new RegraNegocioException("Uma consulta deve ser PRESENCIAL ou TELECONSULTA");
        }
        validarConflitos(c, null);
        c.setStatus(StatusConsulta.AGENDADA);
        c.setOrigem(OrigemConsulta.NORMAL);
        c.setIdConsultaOrigem(null);
        c.setMotivoCancelamento(null);
        c.setDataCancelamento(null);
        c.setDataCriacao(LocalDateTime.now(clock));
        dao.inserir(c);
        return buscar(c.getId());
    }

    /** Reagendamento: permite trocar paciente, profissional, horário e tipo de uma consulta AGENDADA. */
    @Transactional
    public Consulta atualizar(Long id, Consulta c) {
        Consulta atual = buscar(id);
        if (atual.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("Só é possível alterar consultas AGENDADAS (status atual: " + atual.getStatus() + ")");
        }
        pacienteBO.buscar(c.getPaciente().getId());
        profissionalBO.buscar(c.getProfissional().getId());
        if (c.getTipoAtendimento() == null || c.getTipoAtendimento() == TipoAtendimento.QUALQUER) {
            c.setTipoAtendimento(atual.getTipoAtendimento());
        }
        c.setId(id);
        validarConflitos(c, id);
        c.setStatus(atual.getStatus());
        c.setOrigem(atual.getOrigem());
        c.setIdConsultaOrigem(atual.getIdConsultaOrigem());
        dao.atualizar(c);
        return buscar(id);
    }

    public void remover(Long id) {
        if (!dao.remover(id)) throw new RecursoNaoEncontradoException("Consulta", id);
    }

    /**
     * Registra o cancelamento e dispara automaticamente a busca por pacientes compatíveis.
     */
    @Transactional
    public ResultadoCancelamento cancelar(Long id, String motivo) {
        Consulta consulta = buscar(id);
        LocalDateTime agora = LocalDateTime.now(clock);
        if (!consulta.isFutura(agora)) {
            throw new RegraNegocioException("Não é possível cancelar uma consulta cujo horário já passou");
        }
        consulta.cancelar(motivo == null || motivo.isBlank() ? "Cancelada pelo paciente" : motivo, agora);
        dao.atualizar(consulta);

        Optional<Oferta> oferta = distribuicaoBO.distribuir(id);
        String mensagem = oferta
                .map(o -> "Consulta cancelada. Vaga oferecida a " + o.getPaciente().getNome()
                        + " pelo método " + o.getMetodo() + ".")
                .orElse("Consulta cancelada. Nenhum paciente compatível no momento; a vaga continua aberta.");
        return new ResultadoCancelamento(buscar(id), oferta.orElse(null), mensagem);
    }

    @Transactional
    public Consulta marcarRealizada(Long id) {
        Consulta c = buscar(id);
        if (c.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("Só consultas AGENDADAS podem ser marcadas como realizadas");
        }
        c.setStatus(StatusConsulta.REALIZADA);
        dao.atualizar(c);
        return c;
    }

    private void validarConflitos(Consulta c, Long ignorarId) {
        if (dao.profissionalOcupado(c.getProfissional().getId(), c.getDataHora(), ignorarId)) {
            throw new ConflitoException("O profissional já possui uma consulta agendada neste horário");
        }
        boolean mesmoPacienteMesmoHorario = ignorarId != null && dao.buscarPorId(ignorarId)
                .map(a -> a.getPaciente().getId().equals(c.getPaciente().getId()) && a.getDataHora().equals(c.getDataHora()))
                .orElse(false);
        if (!mesmoPacienteMesmoHorario && dao.pacienteOcupado(c.getPaciente().getId(), c.getDataHora())) {
            throw new ConflitoException("O paciente já possui uma consulta agendada neste horário");
        }
    }
}
