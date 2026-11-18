package br.com.fiap.medlink.bo;

import br.com.fiap.medlink.dao.ProfissionalDAO;
import br.com.fiap.medlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.medlink.exception.RegraNegocioException;
import br.com.fiap.medlink.model.Profissional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfissionalBO {

    private final ProfissionalDAO dao;
    private final EspecialidadeBO especialidadeBO;
    private final ClinicaBO clinicaBO;

    public ProfissionalBO(ProfissionalDAO dao, EspecialidadeBO especialidadeBO, ClinicaBO clinicaBO) {
        this.dao = dao;
        this.especialidadeBO = especialidadeBO;
        this.clinicaBO = clinicaBO;
    }

    public List<Profissional> listar(Long idClinica, Long idEspecialidade) {
        return dao.listar(idClinica, idEspecialidade);
    }

    public Profissional buscar(Long id) {
        if (id == null) throw new RegraNegocioException("Informe o id de Profissional");
        return dao.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", id));
    }

    public Profissional cadastrar(Profissional p) {
        p.setId(null);
        validarReferencias(p);
        dao.inserir(p);
        return buscar(p.getId());
    }

    public Profissional atualizar(Long id, Profissional p) {
        buscar(id);
        p.setId(id);
        validarReferencias(p);
        dao.atualizar(p);
        return buscar(id);
    }

    public void remover(Long id) {
        if (!dao.remover(id)) throw new RecursoNaoEncontradoException("Profissional", id);
    }

    private void validarReferencias(Profissional p) {
        especialidadeBO.buscar(p.getEspecialidade().getId());
        clinicaBO.buscar(p.getClinica().getId());
    }
}
