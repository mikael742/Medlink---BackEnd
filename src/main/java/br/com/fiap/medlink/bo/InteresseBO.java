package br.com.fiap.medlink.bo;

import br.com.fiap.medlink.dao.InteresseDAO;
import br.com.fiap.medlink.exception.DatabaseException;
import br.com.fiap.medlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.medlink.exception.RegraNegocioException;
import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.InteresseConsulta;
import br.com.fiap.medlink.model.Profissional;
import br.com.fiap.medlink.model.enums.StatusConsulta;
import br.com.fiap.medlink.model.enums.TipoAtendimento;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Regras da lista de interesse (pacientes que querem antecipar ou conseguir uma consulta). */
@Service
public class InteresseBO {

    private final InteresseDAO dao;
    private final PacienteBO pacienteBO;
    private final EspecialidadeBO especialidadeBO;
    private final ProfissionalBO profissionalBO;
    private final ConsultaBO consultaBO;
    private final Clock clock;

    public InteresseBO(InteresseDAO dao, PacienteBO pacienteBO, EspecialidadeBO especialidadeBO,
                       ProfissionalBO profissionalBO, ConsultaBO consultaBO, Clock clock) {
        this.dao = dao;
        this.pacienteBO = pacienteBO;
        this.especialidadeBO = especialidadeBO;
        this.profissionalBO = profissionalBO;
        this.consultaBO = consultaBO;
        this.clock = clock;
    }

    public List<InteresseConsulta> listar(Long idPaciente, Long idEspecialidade, Boolean ativo) {
        return dao.listar(idPaciente, idEspecialidade, ativo);
    }

    public InteresseConsulta buscar(Long id) {
        if (id == null) throw new RegraNegocioException("Informe o id de Interesse");
        return dao.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Interesse", id));
    }

    public InteresseConsulta cadastrar(InteresseConsulta i) {
        i.setId(null);
        validar(i);
        i.setDataEntrada(LocalDateTime.now(clock)); // posição na fila
        i.setAtivo(true);
        dao.inserir(i);
        return buscar(i.getId());
    }

    /** Atualiza preferências mantendo a posição original na fila (data de entrada). */
    public InteresseConsulta atualizar(Long id, InteresseConsulta i) {
        InteresseConsulta atual = buscar(id);
        i.setId(id);
        validar(i);
        i.setDataEntrada(atual.getDataEntrada());
        if (i.getAtivo() == null) i.setAtivo(atual.getAtivo());
        dao.atualizar(i);
        return buscar(id);
    }

    /**
     * Remove o interesse. Se ele já participou de ofertas (histórico), apenas
     * o desativa, para não perder os dados usados nos indicadores.
     */
    public void remover(Long id) {
        buscar(id);
        try {
            dao.remover(id);
        } catch (DatabaseException e) {
            if (!e.isViolacaoFk()) throw e;
            dao.desativar(id);
        }
    }

    private void validar(InteresseConsulta i) {
        pacienteBO.buscar(i.getPaciente().getId());
        Long idEspecialidade = especialidadeBO.buscar(i.getEspecialidade().getId()).getId();
        if (i.getTipoAtendimento() == null) i.setTipoAtendimento(TipoAtendimento.QUALQUER);

        if (!i.getHoraInicio().isBefore(i.getHoraFim())) {
            throw new RegraNegocioException("horaInicio deve ser anterior a horaFim");
        }
        if (i.getProfissional() != null && i.getProfissional().getId() != null) {
            Profissional p = profissionalBO.buscar(i.getProfissional().getId());
            if (!p.getEspecialidade().getId().equals(idEspecialidade)) {
                throw new RegraNegocioException("O profissional escolhido não atende a especialidade " + i.getEspecialidade().getId());
            }
        } else {
            i.setProfissional(null);
        }
        if (i.getConsultaAtual() != null && i.getConsultaAtual().getId() != null) {
            Consulta c = consultaBO.buscar(i.getConsultaAtual().getId());
            if (!c.getPaciente().getId().equals(i.getPaciente().getId())) {
                throw new RegraNegocioException("A consulta a antecipar pertence a outro paciente");
            }
            if (c.getStatus() != StatusConsulta.AGENDADA) {
                throw new RegraNegocioException("A consulta a antecipar precisa estar AGENDADA");
            }
            if (!c.getProfissional().getEspecialidade().getId().equals(idEspecialidade)) {
                throw new RegraNegocioException("A consulta a antecipar é de outra especialidade");
            }
        } else {
            i.setConsultaAtual(null);
        }
    }
}
