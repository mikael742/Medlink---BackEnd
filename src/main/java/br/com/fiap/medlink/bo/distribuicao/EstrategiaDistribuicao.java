package br.com.fiap.medlink.bo.distribuicao;

import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.enums.MetodoDistribuicao;

import java.util.List;

/**
 * Padrão Strategy: cada método de distribuição de vagas é uma implementação.
 * O DistribuicaoBO não conhece as regras de cada método — ele só pergunta
 * qual estratégia se aplica e pede o ranking dos candidatos (polimorfismo).
 */
public interface EstrategiaDistribuicao {

    MetodoDistribuicao getMetodo();

    /** Esta estratégia deve ser usada para uma vaga que acontece daqui a {@code minutosRestantes}? */
    boolean seAplica(long minutosRestantes, Clinica clinica);

    /** Ordena (e, se necessário, filtra) os candidatos compatíveis por prioridade. */
    Selecao selecionar(List<Candidato> compativeis, ContextoVaga contexto);

    /** Quanto tempo o paciente tem para responder a oferta. */
    int prazoRespostaMinutos(Clinica clinica);
}
