package br.com.fiap.medlink;

import br.com.fiap.medlink.bo.distribuicao.Candidato;
import br.com.fiap.medlink.bo.distribuicao.ContextoVaga;
import br.com.fiap.medlink.bo.distribuicao.EstrategiaDisponibilidade;
import br.com.fiap.medlink.bo.distribuicao.EstrategiaFilaEspera;
import br.com.fiap.medlink.bo.distribuicao.EstrategiaProximidade;
import br.com.fiap.medlink.bo.distribuicao.Selecao;
import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.Especialidade;
import br.com.fiap.medlink.model.InteresseConsulta;
import br.com.fiap.medlink.model.Localizacao;
import br.com.fiap.medlink.model.Paciente;
import br.com.fiap.medlink.model.Profissional;
import br.com.fiap.medlink.model.enums.TipoAtendimento;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Testes unitários das regras de distribuição (sem banco de dados). */
class EstrategiasDistribuicaoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 16, 10, 0);

    private final EstrategiaFilaEspera fila = new EstrategiaFilaEspera();
    private final EstrategiaDisponibilidade disponibilidade = new EstrategiaDisponibilidade();
    private final EstrategiaProximidade proximidade = new EstrategiaProximidade();

    private Clinica clinica() {
        Clinica c = new Clinica(1L);
        c.setLocalizacao(new Localizacao("01310-100", -23.5643, -46.6525));
        c.setHorasLimiteFila(24);
        c.setHorasLimiteUrgencia(6);
        c.setRaioInicialKm(5.0);
        c.setRaioMaximoKm(30.0);
        c.setPrazoRespostaMin(120);
        c.setPrazoRespostaUrgenteMin(15);
        return c;
    }

    private Candidato candidato(long id, String nome, String entrada, double lat, double lon) {
        Paciente p = new Paciente(id);
        p.setNome(nome);
        p.setLocalizacao(new Localizacao("0", lat, lon));
        p.setConsenteLocalizacao(true);
        InteresseConsulta i = new InteresseConsulta(id);
        i.setPaciente(p);
        i.setDataEntrada(LocalDateTime.parse(entrada));
        return new Candidato(i, p);
    }

    private ContextoVaga contexto(LocalDateTime dataHora) {
        Consulta vaga = new Consulta(1L);
        vaga.setDataHora(dataHora);
        vaga.setTipoAtendimento(TipoAtendimento.PRESENCIAL);
        Profissional prof = new Profissional(1L);
        prof.setEspecialidade(new Especialidade(1L));
        return new ContextoVaga(vaga, prof, clinica(), AGORA, null);
    }

    private final List<Candidato> candidatos = List.of(
            candidato(1, "Ana (perto, entrou por último)", "2026-10-10T18:00", -23.5587, -46.6458),
            candidato(2, "Diego (7 km)", "2026-10-03T14:30", -23.5017, -46.6250),
            candidato(3, "Elaine (12 km, entrou primeiro)", "2026-10-01T08:00", -23.6539, -46.7097));

    @Test
    void escolheMetodoPeloTempoRestante() {
        Clinica c = clinica();
        assertTrue(fila.seAplica(10 * 24 * 60, c));
        assertTrue(disponibilidade.seAplica(12 * 60, c));
        assertTrue(proximidade.seAplica(60, c));
        assertFalse(fila.seAplica(12 * 60, c));
        assertFalse(proximidade.seAplica(12 * 60, c));
    }

    @Test
    void filaRespeitaOrdemDeEntrada() {
        Selecao s = fila.selecionar(candidatos, contexto(AGORA.plusDays(10)));
        assertEquals(3L, s.ranking().get(0).paciente().getId().longValue());
        assertEquals(1L, s.ranking().get(2).paciente().getId().longValue());
    }

    @Test
    void proximidadePriorizaQuemChegaMaisRapido() {
        Selecao s = proximidade.selecionar(candidatos, contexto(AGORA.plusHours(3)));
        assertEquals(1L, s.ranking().get(0).paciente().getId().longValue());
        assertEquals(1, s.ranking().size());       // só Ana está no raio inicial de 5 km
        assertEquals(5.0, s.raioBuscaKm());
    }

    @Test
    void proximidadeAmpliaRaioQuandoNinguemEstaPerto() {
        List<Candidato> longe = candidatos.subList(1, 3);
        Selecao s = proximidade.selecionar(longe, contexto(AGORA.plusHours(3)));
        assertEquals(10.0, s.raioBuscaKm());
        assertEquals(2L, s.ranking().get(0).paciente().getId().longValue());
    }

    @Test
    void proximidadeDescartaQuemNaoChegaATempo() {
        Selecao s = proximidade.selecionar(candidatos.subList(2, 3), contexto(AGORA.plusMinutes(20)));
        assertTrue(s.isVazia());
    }
}
