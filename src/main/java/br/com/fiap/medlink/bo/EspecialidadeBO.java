package br.com.fiap.medlink.bo;

import br.com.fiap.medlink.dao.EspecialidadeDAO;
import br.com.fiap.medlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.medlink.exception.RegraNegocioException;
import br.com.fiap.medlink.model.Especialidade;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EspecialidadeBO {

    private final EspecialidadeDAO dao;

    public EspecialidadeBO(EspecialidadeDAO dao) {
        this.dao = dao;
    }

    public List<Especialidade> listar() {
        return dao.listar();
    }

    public Especialidade buscar(Long id) {
        if (id == null) throw new RegraNegocioException("Informe o id de Especialidade");
        return dao.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Especialidade", id));
    }

    public Especialidade cadastrar(Especialidade e) {
        e.setId(null);
        e.setNome(e.getNome().trim());
        return dao.inserir(e);
    }

    public Especialidade atualizar(Long id, Especialidade e) {
        buscar(id);
        e.setId(id);
        e.setNome(e.getNome().trim());
        dao.atualizar(e);
        return e;
    }

    public void remover(Long id) {
        if (!dao.remover(id)) throw new RecursoNaoEncontradoException("Especialidade", id);
    }
}
