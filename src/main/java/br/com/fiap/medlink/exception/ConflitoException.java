package br.com.fiap.medlink.exception;

/** Lançada em conflitos de dados: CPF/CNPJ/CRM duplicado, horário ocupado etc. (HTTP 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
