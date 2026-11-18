package br.com.fiap.medlink.bo;

import br.com.fiap.medlink.dao.PacienteDAO;
import br.com.fiap.medlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.medlink.exception.RegraNegocioException;
import br.com.fiap.medlink.model.Paciente;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteBO {

    private final PacienteDAO dao;

    public PacienteBO(PacienteDAO dao) {
        this.dao = dao;
    }

    public List<Paciente> listar() {
        return dao.listar();
    }

    public Paciente buscar(Long id) {
        if (id == null) throw new RegraNegocioException("Informe o id de Paciente");
        return dao.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }

    public Paciente cadastrar(Paciente p) {
        p.setId(null);
        preparar(p);
        return dao.inserir(p);
    }

    public Paciente atualizar(Long id, Paciente p) {
        buscar(id);
        p.setId(id);
        preparar(p);
        dao.atualizar(p);
        return p;
    }

    public void remover(Long id) {
        if (!dao.remover(id)) throw new RecursoNaoEncontradoException("Paciente", id);
    }

    private void preparar(Paciente p) {
        if (p.getConsenteLocalizacao() == null) p.setConsenteLocalizacao(false);
        if (p.getConvenio() == null || p.getConvenio().isBlank()) p.setConvenio("PARTICULAR");
        p.setConvenio(p.getConvenio().trim().toUpperCase());
    }
}
