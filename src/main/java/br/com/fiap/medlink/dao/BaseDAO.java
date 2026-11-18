package br.com.fiap.medlink.dao;

import br.com.fiap.medlink.exception.DatabaseException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe base dos DAOs. Centraliza o acesso JDBC: obtenção/liberação da
 * conexão, execução de PreparedStatements e conversão de tipos.
 *
 * A conexão é obtida com DataSourceUtils para participar das transações
 * abertas pelos BOs (@Transactional): se um BO faz várias operações,
 * todas usam a mesma conexão e são confirmadas (ou desfeitas) juntas.
 */
public abstract class BaseDAO {

    protected final DataSource dataSource;

    protected BaseDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @FunctionalInterface
    protected interface Parametros {
        void preencher(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    protected interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    protected static final Parametros SEM_PARAMETROS = ps -> { };

    // ------------------------------------------------------------------
    // Execução
    // ------------------------------------------------------------------

    protected <T> List<T> consultarLista(String sql, Parametros parametros, Mapeador<T> mapeador) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            parametros.preencher(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapeador.mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao consultar: " + e.getMessage(), e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    protected <T> Optional<T> consultarUm(String sql, Parametros parametros, Mapeador<T> mapeador) {
        List<T> lista = consultarLista(sql, parametros, mapeador);
        return lista.isEmpty() ? Optional.empty() : Optional.ofNullable(lista.get(0));
    }

    protected long contar(String sql, Parametros parametros) {
        return consultarUm(sql, parametros, rs -> rs.getLong(1)).orElse(0L);
    }

    /** Executa INSERT e devolve o ID gerado pela coluna IDENTITY. */
    protected long inserir(String sql, Parametros parametros) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID"})) {
            parametros.preencher(ps);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("O banco não retornou o ID gerado");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao inserir: " + e.getMessage(), e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    /** Executa UPDATE/DELETE e devolve a quantidade de linhas afetadas. */
    protected int executar(String sql, Parametros parametros) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            parametros.preencher(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao executar comando: " + e.getMessage(), e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    // ------------------------------------------------------------------
    // Conversões de tipos (tratando valores nulos)
    // ------------------------------------------------------------------

    protected static void setLong(PreparedStatement ps, int i, Long valor) throws SQLException {
        if (valor == null) ps.setNull(i, Types.NUMERIC); else ps.setLong(i, valor);
    }

    protected static void setInt(PreparedStatement ps, int i, Integer valor) throws SQLException {
        if (valor == null) ps.setNull(i, Types.NUMERIC); else ps.setInt(i, valor);
    }

    protected static void setDouble(PreparedStatement ps, int i, Double valor) throws SQLException {
        if (valor == null) ps.setNull(i, Types.NUMERIC); else ps.setDouble(i, valor);
    }

    protected static void setString(PreparedStatement ps, int i, String valor) throws SQLException {
        if (valor == null) ps.setNull(i, Types.VARCHAR); else ps.setString(i, valor);
    }

    protected static void setDataHora(PreparedStatement ps, int i, LocalDateTime valor) throws SQLException {
        if (valor == null) ps.setNull(i, Types.TIMESTAMP); else ps.setTimestamp(i, Timestamp.valueOf(valor));
    }

    protected static void setEnum(PreparedStatement ps, int i, Enum<?> valor) throws SQLException {
        setString(ps, i, valor == null ? null : valor.name());
    }

    protected static void setSimNao(PreparedStatement ps, int i, Boolean valor) throws SQLException {
        ps.setString(i, Boolean.TRUE.equals(valor) ? "S" : "N");
    }

    protected static Long getLong(ResultSet rs, String coluna) throws SQLException {
        long v = rs.getLong(coluna);
        return rs.wasNull() ? null : v;
    }

    protected static Integer getInt(ResultSet rs, String coluna) throws SQLException {
        int v = rs.getInt(coluna);
        return rs.wasNull() ? null : v;
    }

    protected static Double getDouble(ResultSet rs, String coluna) throws SQLException {
        double v = rs.getDouble(coluna);
        return rs.wasNull() ? null : v;
    }

    protected static LocalDateTime getDataHora(ResultSet rs, String coluna) throws SQLException {
        Timestamp ts = rs.getTimestamp(coluna);
        return ts == null ? null : ts.toLocalDateTime();
    }

    protected static boolean getSimNao(ResultSet rs, String coluna) throws SQLException {
        return "S".equals(rs.getString(coluna));
    }

    protected static <E extends Enum<E>> E getEnum(ResultSet rs, String coluna, Class<E> tipo) throws SQLException {
        String v = rs.getString(coluna);
        return v == null ? null : Enum.valueOf(tipo, v);
    }
}
