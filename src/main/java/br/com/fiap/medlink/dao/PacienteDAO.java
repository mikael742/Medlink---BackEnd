package br.com.fiap.medlink.dao;

import br.com.fiap.medlink.model.Localizacao;
import br.com.fiap.medlink.model.Paciente;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class PacienteDAO extends BaseDAO {

    public PacienteDAO(DataSource dataSource) {
        super(dataSource);
    }

    public Paciente inserir(Paciente p) {
        String sql = "INSERT INTO MDL_PACIENTE (NOME, EMAIL, TELEFONE, CPF, DATA_NASCIMENTO, CONVENIO, CEP, LATITUDE, "
                + "LONGITUDE, CONSENTE_LOCALIZACAO) VALUES (?,?,?,?,?,?,?,?,?,?)";
        long id = inserir(sql, ps -> preencher(ps, p));
        p.setId(id);
        return p;
    }

    public boolean atualizar(Paciente p) {
        String sql = "UPDATE MDL_PACIENTE SET NOME=?, EMAIL=?, TELEFONE=?, CPF=?, DATA_NASCIMENTO=?, CONVENIO=?, CEP=?, "
                + "LATITUDE=?, LONGITUDE=?, CONSENTE_LOCALIZACAO=? WHERE ID=?";
        return executar(sql, ps -> {
            preencher(ps, p);
            ps.setLong(11, p.getId());
        }) > 0;
    }

    public boolean remover(Long id) {
        return executar("DELETE FROM MDL_PACIENTE WHERE ID = ?", ps -> ps.setLong(1, id)) > 0;
    }

    public Optional<Paciente> buscarPorId(Long id) {
        return consultarUm("SELECT * FROM MDL_PACIENTE WHERE ID = ?", ps -> ps.setLong(1, id), this::mapear);
    }

    public List<Paciente> listar() {
        return consultarLista("SELECT * FROM MDL_PACIENTE ORDER BY NOME", SEM_PARAMETROS, this::mapear);
    }

    private void preencher(PreparedStatement ps, Paciente p) throws SQLException {
        Localizacao loc = p.getLocalizacao();
        ps.setString(1, p.getNome());
        setString(ps, 2, p.getEmail());
        setString(ps, 3, p.getTelefone());
        ps.setString(4, p.getCpf());
        if (p.getDataNascimento() == null) ps.setNull(5, Types.DATE);
        else ps.setDate(5, Date.valueOf(p.getDataNascimento()));
        setString(ps, 6, p.getConvenio());
        setString(ps, 7, loc == null ? null : loc.getCep());
        setDouble(ps, 8, loc == null ? null : loc.getLatitude());
        setDouble(ps, 9, loc == null ? null : loc.getLongitude());
        setSimNao(ps, 10, p.getConsenteLocalizacao());
    }

    private Paciente mapear(ResultSet rs) throws SQLException {
        Paciente p = new Paciente(rs.getLong("ID"));
        p.setNome(rs.getString("NOME"));
        p.setEmail(rs.getString("EMAIL"));
        p.setTelefone(rs.getString("TELEFONE"));
        p.setCpf(rs.getString("CPF"));
        Date nasc = rs.getDate("DATA_NASCIMENTO");
        p.setDataNascimento(nasc == null ? null : nasc.toLocalDate());
        p.setConvenio(rs.getString("CONVENIO"));
        p.setLocalizacao(new Localizacao(rs.getString("CEP"), getDouble(rs, "LATITUDE"), getDouble(rs, "LONGITUDE")));
        p.setConsenteLocalizacao(getSimNao(rs, "CONSENTE_LOCALIZACAO"));
        return p;
    }
}
