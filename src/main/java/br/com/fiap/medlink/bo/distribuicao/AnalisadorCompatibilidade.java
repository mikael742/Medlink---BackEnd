package br.com.fiap.medlink.bo.distribuicao;

import br.com.fiap.medlink.dao.ConsultaDAO;
import br.com.fiap.medlink.dao.InteresseDAO;
import br.com.fiap.medlink.dao.OfertaDAO;
import br.com.fiap.medlink.dao.PacienteDAO;
import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.InteresseConsulta;
import br.com.fiap.medlink.model.Paciente;
import br.com.fiap.medlink.model.enums.StatusConsulta;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Verifica, para uma vaga, quais pacientes da lista de interesse realmente
 * atendem aos requisitos: especialidade, profissional, convênio, tipo de
 * atendimento, disponibilidade, ganho real de antecipação e ausência de conflitos.
 * Os excluídos voltam com o motivo, o que facilita explicar a decisão.
 */
@Component
public class AnalisadorCompatibilidade {

    private final InteresseDAO interesseDAO;
    private final PacienteDAO pacienteDAO;
    private final ConsultaDAO consultaDAO;
    private final OfertaDAO ofertaDAO;

    public AnalisadorCompatibilidade(InteresseDAO interesseDAO, PacienteDAO pacienteDAO,
                                     ConsultaDAO consultaDAO, OfertaDAO ofertaDAO) {
        this.interesseDAO = interesseDAO;
        this.pacienteDAO = pacienteDAO;
        this.consultaDAO = consultaDAO;
        this.ofertaDAO = ofertaDAO;
    }

    public record Exclusao(Long idInteresse, Long idPaciente, String nomePaciente, String motivo) {
    }

    public record Resultado(List<Candidato> compativeis, List<Exclusao> excluidos) {
    }

    public Resultado analisar(ContextoVaga ctx) {
        Consulta vaga = ctx.vaga();
        Long idEspecialidade = ctx.profissional().getEspecialidade().getId();
        Set<Long> jaOfertados = ofertaDAO.pacientesJaOfertados(vaga.getId());
        Set<Long> pacientesVistos = new HashSet<>();

        List<Candidato> compativeis = new ArrayList<>();
        List<Exclusao> excluidos = new ArrayList<>();

        for (InteresseConsulta interesse : interesseDAO.listarAtivosPorEspecialidade(idEspecialidade)) {
            Optional<Paciente> opt = pacienteDAO.buscarPorId(interesse.getPaciente().getId());
            if (opt.isEmpty()) continue;
            Paciente paciente = opt.get();

            String motivo = motivoDeExclusao(ctx, interesse, paciente, jaOfertados, pacientesVistos);
            if (motivo == null) {
                compativeis.add(new Candidato(interesse, paciente));
                pacientesVistos.add(paciente.getId());
            } else {
                excluidos.add(new Exclusao(interesse.getId(), paciente.getId(), paciente.getNome(), motivo));
            }
        }
        return new Resultado(compativeis, excluidos);
    }

    /** Retorna null se o interesse é compatível; caso contrário, o motivo da exclusão. */
    private String motivoDeExclusao(ContextoVaga ctx, InteresseConsulta interesse, Paciente paciente,
                                    Set<Long> jaOfertados, Set<Long> pacientesVistos) {
        Consulta vaga = ctx.vaga();

        if (paciente.getId().equals(vaga.getPaciente().getId())) {
            return "É o próprio paciente que cancelou a consulta";
        }
        if (jaOfertados.contains(paciente.getId())) {
            return "Já recebeu uma oferta para esta vaga";
        }
        if (pacientesVistos.contains(paciente.getId())) {
            return "Paciente já está no ranking por outro interesse";
        }
        if (interesse.getProfissional() != null
                && !interesse.getProfissional().getId().equals(ctx.profissional().getId())) {
            return "Prefere outro profissional";
        }
        if (!ctx.clinica().aceitaConvenio(paciente.getConvenio())) {
            return "Convênio " + paciente.getConvenio() + " não é aceito pela clínica";
        }
        if (!interesse.getTipoAtendimento().aceita(vaga.getTipoAtendimento())) {
            return "Quer atendimento " + interesse.getTipoAtendimento() + " e a vaga é " + vaga.getTipoAtendimento();
        }
        if (!interesse.isDisponivelEm(vaga.getDataHora())) {
            return "Sem disponibilidade neste dia/horário";
        }
        if (interesse.isAntecipacao()) {
            Optional<Consulta> atual = consultaDAO.buscarPorId(interesse.getConsultaAtual().getId());
            if (atual.isPresent() && atual.get().getStatus() == StatusConsulta.AGENDADA
                    && !vaga.getDataHora().isBefore(atual.get().getDataHora())) {
                return "A vaga não antecipa a consulta que ele já possui";
            }
        }
        if (consultaDAO.pacienteOcupado(paciente.getId(), vaga.getDataHora())) {
            return "Já tem outra consulta neste mesmo horário";
        }
        return null;
    }
}
