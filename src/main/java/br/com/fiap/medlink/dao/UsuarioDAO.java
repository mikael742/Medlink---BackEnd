package br.com.fiap.medlink.dao;

import br.com.fiap.medlink.model.Usuario;
import br.com.fiap.medlink.model.enums.PerfilUsuario;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class UsuarioDAO extends BaseDAO {

    public UsuarioDAO(DataSource dataSource) {
        super(dataSource);
    }

    public Usuario inserir(Usuario u) {
        String sql = "INSERT INTO MDL_USUARIO (EMAIL, SENHA_HASH, SALT, PERFIL, ID_PACIENTE, ID_CLINICA, DATA_CRIACAO) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        long id = inserir(sql, ps -> {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getSenhaHash());
            ps.setString(3, u.getSalt());
            setEnum(ps, 4, u.getPerfil());
            setLong(ps, 5, u.getIdPaciente());
            setLong(ps, 6, u.getIdClinica());
            setDataHora(ps, 7, u.getDataCriacao());
        });
        u.setId(id);
        return u;
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return consultarUm("SELECT * FROM MDL_USUARIO WHERE EMAIL = ?", ps -> ps.setString(1, email), this::mapear);
    }

    public boolean existeEmail(String email) {
        return contar("SELECT COUNT(*) FROM MDL_USUARIO WHERE EMAIL = ?", ps -> ps.setString(1, email)) > 0;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("ID"));
        u.setEmail(rs.getString("EMAIL"));
        u.setSenhaHash(rs.getString("SENHA_HASH"));
        u.setSalt(rs.getString("SALT"));
        u.setPerfil(getEnum(rs, "PERFIL", PerfilUsuario.class));
        u.setIdPaciente(getLong(rs, "ID_PACIENTE"));
        u.setIdClinica(getLong(rs, "ID_CLINICA"));
        u.setDataCriacao(getDataHora(rs, "DATA_CRIACAO"));
        return u;
    }
}
