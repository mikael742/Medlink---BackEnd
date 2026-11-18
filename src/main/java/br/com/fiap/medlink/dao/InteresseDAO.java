package br.com.fiap.medlink.dao;

import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.Especialidade;
import br.com.fiap.medlink.model.InteresseConsulta;
import br.com.fiap.medlink.model.Paciente;
import br.com.fiap.medlink.model.Profissional;
import br.com.fiap.medlink.model.enums.TipoAtendimento;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class InteresseDAO extends BaseDAO {

    private static final String SELECT = """
            SELECT I.*, PA.NOME AS NOME_PACIENTE, E.NOME AS NOME_ESPECIALIDADE, PR.NOME AS NOME_PROFISSIONAL
              FROM MDL_INTERESSE I
              JOIN MDL_PACIENTE PA ON PA.ID = I.ID_PACIENTE
              JOIN MDL_ESPECIALIDADE E ON E.ID = I.ID_ESPECIALIDADE
              LEFT JOIN MDL_PROFISSIONAL PR ON PR.ID = I.ID_PROFISSIONAL
            """;

    public InteresseDAO(DataSource dataSource) {
        super(dataSource);
    }

    public InteresseConsulta inserir(InteresseConsulta i) {
        String sql = "INSERT INTO MDL_INTERESSE (ID_PACIENTE, ID_ESPECIALIDADE, ID_PROFISSIONAL, ID_CONSULTA_ATUAL, "
                + "DIAS_SEMANA, HORA_INICIO, HORA_FIM, TIPO_ATENDIMENTO, ATIVO, DATA_ENTRADA) VALUES (?,?,?,?,?,?,?,?,?,?)";
        long id = inserir(sql, ps -> {
            preencher(ps, i);
            setDataHora(ps, 10, i.getDataEntrada());
        });
        i.setId(id);
        return i;
    }

    public boolean atualizar(InteresseConsulta i) {
        String sql = "UPDATE MDL_INTERESSE SET ID_PACIENTE=?, ID_ESPECIALIDADE=?, ID_PROFISSIONAL=?, ID_CONSULTA_ATUAL=?, "
                + "DIAS_SEMANA=?, HORA_INICIO=?, HORA_FIM=?, TIPO_ATENDIMENTO=?, ATIVO=? WHERE ID=?";
        return executar(sql, ps -> {
            preencher(ps, i);
            ps.setLong(10, i.getId());
        }) > 0;
    }

    public void desativar(Long id) {
        executar("UPDATE MDL_INTERESSE SET ATIVO = 'N' WHERE ID = ?", ps -> ps.setLong(1, id));
    }

    public boolean remover(Long id) {
        return executar("DELETE FROM MDL_INTERESSE WHERE ID = ?", ps -> ps.setLong(1, id)) > 0;
    }

    public Optional<InteresseConsulta> buscarPorId(Long id) {
        return consultarUm(SELECT + " WHERE I.ID = ?", ps -> ps.setLong(1, id), this::mapear);
    }

    public List<InteresseConsulta> listar(Long idPaciente, Long idEspecialidade, Boolean ativo) {
        Filtro filtro = new Filtro()
                .se(idPaciente, "I.ID_PACIENTE = ?")
                .se(idEspecialidade, "I.ID_ESPECIALIDADE = ?")
                .se(ativo == null ? null : (ativo ? "S" : "N"), "I.ATIVO = ?");
        return consultarLista(SELECT + filtro.where() + " ORDER BY I.DATA_ENTRADA", filtro::preencher, this::mapear);
    }

    /** Candidatos iniciais do algoritmo: interesses ativos na especialidade, em ordem de entrada na fila. */
    public List<InteresseConsulta> listarAtivosPorEspecialidade(Long idEspecialidade) {
        return listar(null, idEspecialidade, true);
    }

    private void preencher(PreparedStatement ps, InteresseConsulta i) throws SQLException {
        ps.setLong(1, i.getPaciente().getId());
        ps.setLong(2, i.getEspecialidade().getId());
        setLong(ps, 3, i.getProfissional() == null ? null : i.getProfissional().getId());
        setLong(ps, 4, i.getConsultaAtual() == null ? null : i.getConsultaAtual().getId());
        ps.setString(5, i.getDiasSemana());
        ps.setString(6, i.getHoraInicio().toString());
        ps.setString(7, i.getHoraFim().toString());
        setEnum(ps, 8, i.getTipoAtendimento());
        setSimNao(ps, 9, i.getAtivo());
    }

    private InteresseConsulta mapear(ResultSet rs) throws SQLException {
        InteresseConsulta i = new InteresseConsulta(rs.getLong("ID"));

        Paciente pa = new Paciente(rs.getLong("ID_PACIENTE"));
        pa.setNome(rs.getString("NOME_PACIENTE"));
        i.setPaciente(pa);

        i.setEspecialidade(new Especialidade(rs.getLong("ID_ESPECIALIDADE"), rs.getString("NOME_ESPECIALIDADE")));

        Long idProf = getLong(rs, "ID_PROFISSIONAL");
        if (idProf != null) {
            Profissional pr = new Profissional(idProf);
            pr.setNome(rs.getString("NOME_PROFISSIONAL"));
            i.setProfissional(pr);
        }
        Long idConsulta = getLong(rs, "ID_CONSULTA_ATUAL");
        if (idConsulta != null) {
            i.setConsultaAtual(new Consulta(idConsulta));
        }
        i.setDiasSemana(rs.getString("DIAS_SEMANA"));
        i.setHoraInicio(LocalTime.parse(rs.getString("HORA_INICIO")));
        i.setHoraFim(LocalTime.parse(rs.getString("HORA_FIM")));
        i.setTipoAtendimento(getEnum(rs, "TIPO_ATENDIMENTO", TipoAtendimento.class));
        i.setDataEntrada(getDataHora(rs, "DATA_ENTRADA"));
        i.setAtivo(getSimNao(rs, "ATIVO"));
        return i;
    }
}
