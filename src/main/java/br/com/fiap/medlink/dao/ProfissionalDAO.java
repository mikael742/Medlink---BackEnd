package br.com.fiap.medlink.dao;

import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.Especialidade;
import br.com.fiap.medlink.model.Profissional;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ProfissionalDAO extends BaseDAO {

    private static final String SELECT = """
            SELECT P.*, E.NOME AS NOME_ESPECIALIDADE, C.NOME AS NOME_CLINICA
              FROM MDL_PROFISSIONAL P
              JOIN MDL_ESPECIALIDADE E ON E.ID = P.ID_ESPECIALIDADE
              JOIN MDL_CLINICA C ON C.ID = P.ID_CLINICA
            """;

    public ProfissionalDAO(DataSource dataSource) {
        super(dataSource);
    }

    public Profissional inserir(Profissional p) {
        String sql = "INSERT INTO MDL_PROFISSIONAL (NOME, EMAIL, TELEFONE, CRM, ID_ESPECIALIDADE, ID_CLINICA) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        long id = inserir(sql, ps -> {
            ps.setString(1, p.getNome());
            setString(ps, 2, p.getEmail());
            setString(ps, 3, p.getTelefone());
            ps.setString(4, p.getCrm());
            ps.setLong(5, p.getEspecialidade().getId());
            ps.setLong(6, p.getClinica().getId());
        });
        p.setId(id);
        return p;
    }

    public boolean atualizar(Profissional p) {
        String sql = "UPDATE MDL_PROFISSIONAL SET NOME=?, EMAIL=?, TELEFONE=?, CRM=?, ID_ESPECIALIDADE=?, ID_CLINICA=? "
                + "WHERE ID=?";
        return executar(sql, ps -> {
            ps.setString(1, p.getNome());
            setString(ps, 2, p.getEmail());
            setString(ps, 3, p.getTelefone());
            ps.setString(4, p.getCrm());
            ps.setLong(5, p.getEspecialidade().getId());
            ps.setLong(6, p.getClinica().getId());
            ps.setLong(7, p.getId());
        }) > 0;
    }

    public boolean remover(Long id) {
        return executar("DELETE FROM MDL_PROFISSIONAL WHERE ID = ?", ps -> ps.setLong(1, id)) > 0;
    }

    public Optional<Profissional> buscarPorId(Long id) {
        return consultarUm(SELECT + " WHERE P.ID = ?", ps -> ps.setLong(1, id), this::mapear);
    }

    /** Lista com filtros opcionais por clínica e/ou especialidade. */
    public List<Profissional> listar(Long idClinica, Long idEspecialidade) {
        Filtro filtro = new Filtro()
                .se(idClinica, "P.ID_CLINICA = ?")
                .se(idEspecialidade, "P.ID_ESPECIALIDADE = ?");
        return consultarLista(SELECT + filtro.where() + " ORDER BY P.NOME", filtro::preencher, this::mapear);
    }

    private Profissional mapear(ResultSet rs) throws SQLException {
        Profissional p = new Profissional(rs.getLong("ID"));
        p.setNome(rs.getString("NOME"));
        p.setEmail(rs.getString("EMAIL"));
        p.setTelefone(rs.getString("TELEFONE"));
        p.setCrm(rs.getString("CRM"));
        p.setEspecialidade(new Especialidade(rs.getLong("ID_ESPECIALIDADE"), rs.getString("NOME_ESPECIALIDADE")));
        Clinica c = new Clinica(rs.getLong("ID_CLINICA"));
        c.setNome(rs.getString("NOME_CLINICA"));
        p.setClinica(c);
        return p;
    }
}
