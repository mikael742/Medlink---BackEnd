package br.com.fiap.medlink.exception;

/** Lançada quando uma operação viola uma regra do domínio (HTTP 422). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
