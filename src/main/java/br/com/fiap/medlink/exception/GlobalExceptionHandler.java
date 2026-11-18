package br.com.fiap.medlink.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Camada de tratamento de exceções: converte qualquer erro em uma resposta
 * JSON padronizada (ErroResponse) com o status HTTP adequado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException e, HttpServletRequest req) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), req, null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResponse> estadoInvalido(IllegalStateException e, HttpServletRequest req) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), req, null);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> credenciais(CredenciaisInvalidasException e, HttpServletRequest req) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage(), req, null);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, e.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<ErroResponse.CampoInvalido> campos = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErroResponse.CampoInvalido(f.getField(), f.getDefaultMessage()))
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos na requisição", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST,
                "JSON inválido ou com formato incorreto (verifique datas, enums e números)", req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + e.getName() + "': " + e.getValue(), req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage(), req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Endpoint não encontrado", req, null);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErroResponse> banco(DatabaseException e, HttpServletRequest req) {
        if (e.isViolacaoUnica()) {
            return resposta(HttpStatus.CONFLICT, "Registro duplicado: já existe um cadastro com esses dados", req, null);
        }
        if (e.isViolacaoFk()) {
            return resposta(HttpStatus.CONFLICT,
                    "Operação viola um relacionamento: o registro está em uso ou referencia um registro inexistente", req, null);
        }
        log.error("Erro de banco de dados", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao acessar o banco de dados", req, null);
    }

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<ErroResponse> semConexao(CannotGetJdbcConnectionException e, HttpServletRequest req) {
        log.error("Falha ao conectar no banco: {}", e.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível conectar ao banco de dados: " + e.getMostSpecificCause().getMessage(), req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> generico(Exception e, HttpServletRequest req) {
        log.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", req, null);
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem, HttpServletRequest req,
                                                  List<ErroResponse.CampoInvalido> campos) {
        ErroResponse corpo = new ErroResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensagem, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
