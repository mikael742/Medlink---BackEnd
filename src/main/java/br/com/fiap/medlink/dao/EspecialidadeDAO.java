package br.com.fiap.medlink.dao;

import br.com.fiap.medlink.model.Especialidade;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class EspecialidadeDAO extends BaseDAO {

    public EspecialidadeDAO(DataSource dataSource) {
        super(dataSource);
    }

    public Especialidade inserir(Especialidade e) {
        long id = inserir("INSERT INTO MDL_ESPECIALIDADE (NOME, DESCRICAO) VALUES (?, ?)", ps -> {
            ps.setString(1, e.getNome());
            setString(ps, 2, e.getDescricao());
        });
        e.setId(id);
        return e;
    }

    public boolean atualizar(Especialidade e) {
        return executar("UPDATE MDL_ESPECIALIDADE SET NOME = ?, DESCRICAO = ? WHERE ID = ?", ps -> {
            ps.setString(1, e.getNome());
            setString(ps, 2, e.getDescricao());
            ps.setLong(3, e.getId());
        }) > 0;
    }

    public boolean remover(Long id) {
        return executar("DELETE FROM MDL_ESPECIALIDADE WHERE ID = ?", ps -> ps.setLong(1, id)) > 0;
    }

    public Optional<Especialidade> buscarPorId(Long id) {
        return consultarUm("SELECT * FROM MDL_ESPECIALIDADE WHERE ID = ?", ps -> ps.setLong(1, id), this::mapear);
    }

    public List<Especialidade> listar() {
        return consultarLista("SELECT * FROM MDL_ESPECIALIDADE ORDER BY NOME", SEM_PARAMETROS, this::mapear);
    }

    private Especialidade mapear(ResultSet rs) throws SQLException {
        Especialidade e = new Especialidade(rs.getLong("ID"), rs.getString("NOME"));
        e.setDescricao(rs.getString("DESCRICAO"));
        return e;
    }
}
