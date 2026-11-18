package br.com.fiap.medlink.dao;

import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.Localizacao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ClinicaDAO extends BaseDAO {

    private static final String COLUNAS = "NOME, CNPJ, TELEFONE, EMAIL, ENDERECO, CEP, LATITUDE, LONGITUDE, "
            + "CONVENIOS_ACEITOS, HORAS_LIMITE_FILA, HORAS_LIMITE_URGENCIA, RAIO_INICIAL_KM, RAIO_MAXIMO_KM, "
            + "PRAZO_RESPOSTA_MIN, PRAZO_RESPOSTA_URGENTE_MIN";

    public ClinicaDAO(DataSource dataSource) {
        super(dataSource);
    }

    public Clinica inserir(Clinica c) {
        long id = inserir("INSERT INTO MDL_CLINICA (" + COLUNAS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                ps -> preencher(ps, c));
        c.setId(id);
        return c;
    }

    public boolean atualizar(Clinica c) {
        String sql = "UPDATE MDL_CLINICA SET NOME=?, CNPJ=?, TELEFONE=?, EMAIL=?, ENDERECO=?, CEP=?, LATITUDE=?, "
                + "LONGITUDE=?, CONVENIOS_ACEITOS=?, HORAS_LIMITE_FILA=?, HORAS_LIMITE_URGENCIA=?, RAIO_INICIAL_KM=?, "
                + "RAIO_MAXIMO_KM=?, PRAZO_RESPOSTA_MIN=?, PRAZO_RESPOSTA_URGENTE_MIN=? WHERE ID=?";
        return executar(sql, ps -> {
            preencher(ps, c);
            ps.setLong(16, c.getId());
        }) > 0;
    }

    public boolean remover(Long id) {
        return executar("DELETE FROM MDL_CLINICA WHERE ID = ?", ps -> ps.setLong(1, id)) > 0;
    }

    public Optional<Clinica> buscarPorId(Long id) {
        return consultarUm("SELECT * FROM MDL_CLINICA WHERE ID = ?", ps -> ps.setLong(1, id), this::mapear);
    }

    public List<Clinica> listar() {
        return consultarLista("SELECT * FROM MDL_CLINICA ORDER BY NOME", SEM_PARAMETROS, this::mapear);
    }

    private void preencher(PreparedStatement ps, Clinica c) throws SQLException {
        Localizacao loc = c.getLocalizacao();
        ps.setString(1, c.getNome());
        ps.setString(2, c.getCnpj());
        setString(ps, 3, c.getTelefone());
        setString(ps, 4, c.getEmail());
        setString(ps, 5, c.getEndereco());
        setString(ps, 6, loc == null ? null : loc.getCep());
        setDouble(ps, 7, loc == null ? null : loc.getLatitude());
        setDouble(ps, 8, loc == null ? null : loc.getLongitude());
        setString(ps, 9, c.getConveniosAceitos());
        setInt(ps, 10, c.getHorasLimiteFila());
        setInt(ps, 11, c.getHorasLimiteUrgencia());
        setDouble(ps, 12, c.getRaioInicialKm());
        setDouble(ps, 13, c.getRaioMaximoKm());
        setInt(ps, 14, c.getPrazoRespostaMin());
        setInt(ps, 15, c.getPrazoRespostaUrgenteMin());
    }

    private Clinica mapear(ResultSet rs) throws SQLException {
        Clinica c = new Clinica(rs.getLong("ID"));
        c.setNome(rs.getString("NOME"));
        c.setCnpj(rs.getString("CNPJ"));
        c.setTelefone(rs.getString("TELEFONE"));
        c.setEmail(rs.getString("EMAIL"));
        c.setEndereco(rs.getString("ENDERECO"));
        c.setLocalizacao(new Localizacao(rs.getString("CEP"), getDouble(rs, "LATITUDE"), getDouble(rs, "LONGITUDE")));
        c.setConveniosAceitos(rs.getString("CONVENIOS_ACEITOS"));
        c.setHorasLimiteFila(getInt(rs, "HORAS_LIMITE_FILA"));
        c.setHorasLimiteUrgencia(getInt(rs, "HORAS_LIMITE_URGENCIA"));
        c.setRaioInicialKm(getDouble(rs, "RAIO_INICIAL_KM"));
        c.setRaioMaximoKm(getDouble(rs, "RAIO_MAXIMO_KM"));
        c.setPrazoRespostaMin(getInt(rs, "PRAZO_RESPOSTA_MIN"));
        c.setPrazoRespostaUrgenteMin(getInt(rs, "PRAZO_RESPOSTA_URGENTE_MIN"));
        return c;
    }
}
