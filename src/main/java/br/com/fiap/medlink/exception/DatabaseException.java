package br.com.fiap.medlink.exception;

import java.sql.SQLException;

/** Envolve erros de JDBC para não vazar SQLException para as camadas de cima (HTTP 500). */
public class DatabaseException extends RuntimeException {

    private final String sqlState;
    private final int errorCode;

    public DatabaseException(String mensagem, SQLException causa) {
        super(mensagem, causa);
        this.sqlState = causa.getSQLState();
        this.errorCode = causa.getErrorCode();
    }

    /**
     * Violação de chave única: ORA-00001 no Oracle, SQLState 23505 no H2.
     */
    public boolean isViolacaoUnica() {
        return errorCode == 1 || "23505".equals(sqlState);
    }

    /**
     * Violação de chave estrangeira: ORA-02291/02292 no Oracle, SQLState 23503/23506 no H2.
     */
    public boolean isViolacaoFk() {
        return errorCode == 2291 || errorCode == 2292
                || "23503".equals(sqlState) || "23506".equals(sqlState);
    }
}
