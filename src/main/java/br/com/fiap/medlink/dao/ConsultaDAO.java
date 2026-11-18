package br.com.fiap.medlink.dao;

import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.Especialidade;
import br.com.fiap.medlink.model.Paciente;
import br.com.fiap.medlink.model.Profissional;
import br.com.fiap.medlink.model.enums.OrigemConsulta;
import br.com.fiap.medlink.model.enums.StatusConsulta;
import br.com.fiap.medlink.model.enums.TipoAtendimento;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ConsultaDAO extends BaseDAO {

    private static final String SELECT = """
            SELECT C.*, PA.NOME AS NOME_PACIENTE, PR.NOME AS NOME_PROFISSIONAL,
                   PR.ID_ESPECIALIDADE, E.NOME AS NOME_ESPECIALIDADE, PR.ID_CLINICA, CL.NOME AS NOME_CLINICA
              FROM MDL_CONSULTA C
              JOIN MDL_PACIENTE PA ON PA.ID = C.ID_PACIENTE
              JOIN MDL_PROFISSIONAL PR ON PR.ID = C.ID_PROFISSIONAL
              JOIN MDL_ESPECIALIDADE E ON E.ID = PR.ID_ESPECIALIDADE
              JOIN MDL_CLINICA CL ON CL.ID = PR.ID_CLINICA
            """;

    public ConsultaDAO(DataSource dataSource) {
        super(dataSource);
    }

    public Consulta inserir(Consulta c) {
        String sql = "INSERT INTO MDL_CONSULTA (ID_PACIENTE, ID_PROFISSIONAL, DATA_HORA, TIPO_ATENDIMENTO, STATUS, ORIGEM, "
                + "ID_CONSULTA_ORIGEM, MOTIVO_CANCELAMENTO, DATA_CANCELAMENTO, DATA_CRIACAO) VALUES (?,?,?,?,?,?,?,?,?,?)";
        long id = inserir(sql, ps -> {
            preencher(ps, c);
            setDataHora(ps, 10, c.getDataCriacao());
        });
        c.setId(id);
        return c;
    }

    public boolean atualizar(Consulta c) {
        String sql = "UPDATE MDL_CONSULTA SET ID_PACIENTE=?, ID_PROFISSIONAL=?, DATA_HORA=?, TIPO_ATENDIMENTO=?, STATUS=?, "
                + "ORIGEM=?, ID_CONSULTA_ORIGEM=?, MOTIVO_CANCELAMENTO=?, DATA_CANCELAMENTO=? WHERE ID=?";
        return executar(sql, ps -> {
            preencher(ps, c);
            ps.setLong(10, c.getId());
        }) > 0;
    }

    public boolean remover(Long id) {
        return executar("DELETE FROM MDL_CONSULTA WHERE ID = ?", ps -> ps.setLong(1, id)) > 0;
    }

    public Optional<Consulta> buscarPorId(Long id) {
        return consultarUm(SELECT + " WHERE C.ID = ?", ps -> ps.setLong(1, id), this::mapear);
    }

    public List<Consulta> listar(StatusConsulta status, Long idPaciente, Long idProfissional, Long idClinica) {
        Filtro filtro = new Filtro()
                .se(status, "C.STATUS = ?")
                .se(idPaciente, "C.ID_PACIENTE = ?")
                .se(idProfissional, "C.ID_PROFISSIONAL = ?")
                .se(idClinica, "PR.ID_CLINICA = ?");
        return consultarLista(SELECT + filtro.where() + " ORDER BY C.DATA_HORA", filtro::preencher, this::mapear);
    }

    /** O profissional já tem outra consulta AGENDADA neste horário? */
    public boolean profissionalOcupado(Long idProfissional, LocalDateTime dataHora, Long ignorarId) {
        String sql = "SELECT COUNT(*) FROM MDL_CONSULTA WHERE ID_PROFISSIONAL = ? AND DATA_HORA = ? "
                + "AND STATUS = 'AGENDADA' AND ID <> ?";
        return contar(sql, ps -> {
            ps.setLong(1, idProfissional);
            setDataHora(ps, 2, dataHora);
            ps.setLong(3, ignorarId == null ? -1L : ignorarId);
        }) > 0;
    }

    /** O paciente já tem uma consulta AGENDADA neste mesmo horário (com qualquer profissional)? */
    public boolean pacienteOcupado(Long idPaciente, LocalDateTime dataHora) {
        String sql = "SELECT COUNT(*) FROM MDL_CONSULTA WHERE ID_PACIENTE = ? AND DATA_HORA = ? AND STATUS = 'AGENDADA'";
        return contar(sql, ps -> {
            ps.setLong(1, idPaciente);
            setDataHora(ps, 2, dataHora);
        }) > 0;
    }

    private void preencher(PreparedStatement ps, Consulta c) throws SQLException {
        ps.setLong(1, c.getPaciente().getId());
        ps.setLong(2, c.getProfissional().getId());
        setDataHora(ps, 3, c.getDataHora());
        setEnum(ps, 4, c.getTipoAtendimento());
        setEnum(ps, 5, c.getStatus());
        setEnum(ps, 6, c.getOrigem());
        setLong(ps, 7, c.getIdConsultaOrigem());
        setString(ps, 8, c.getMotivoCancelamento());
        setDataHora(ps, 9, c.getDataCancelamento());
    }

    private Consulta mapear(ResultSet rs) throws SQLException {
        Consulta c = new Consulta(rs.getLong("ID"));

        Paciente pa = new Paciente(rs.getLong("ID_PACIENTE"));
        pa.setNome(rs.getString("NOME_PACIENTE"));
        c.setPaciente(pa);

        Profissional pr = new Profissional(rs.getLong("ID_PROFISSIONAL"));
        pr.setNome(rs.getString("NOME_PROFISSIONAL"));
        pr.setEspecialidade(new Especialidade(rs.getLong("ID_ESPECIALIDADE"), rs.getString("NOME_ESPECIALIDADE")));
        Clinica cl = new Clinica(rs.getLong("ID_CLINICA"));
        cl.setNome(rs.getString("NOME_CLINICA"));
        pr.setClinica(cl);
        c.setProfissional(pr);

        c.setDataHora(getDataHora(rs, "DATA_HORA"));
        c.setTipoAtendimento(getEnum(rs, "TIPO_ATENDIMENTO", TipoAtendimento.class));
        c.setStatus(getEnum(rs, "STATUS", StatusConsulta.class));
        c.setOrigem(getEnum(rs, "ORIGEM", OrigemConsulta.class));
        c.setIdConsultaOrigem(getLong(rs, "ID_CONSULTA_ORIGEM"));
        c.setMotivoCancelamento(rs.getString("MOTIVO_CANCELAMENTO"));
        c.setDataCancelamento(getDataHora(rs, "DATA_CANCELAMENTO"));
        c.setDataCriacao(getDataHora(rs, "DATA_CRIACAO"));
        return c;
    }
}
