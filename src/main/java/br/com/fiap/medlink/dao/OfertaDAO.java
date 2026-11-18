package br.com.fiap.medlink.dao;

import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.InteresseConsulta;
import br.com.fiap.medlink.model.Oferta;
import br.com.fiap.medlink.model.Paciente;
import br.com.fiap.medlink.model.enums.MetodoDistribuicao;
import br.com.fiap.medlink.model.enums.StatusOferta;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class OfertaDAO extends BaseDAO {

    private static final String SELECT = """
            SELECT O.*, PA.NOME AS NOME_PACIENTE, C.DATA_HORA AS DATA_HORA_VAGA,
                   C.DATA_CANCELAMENTO AS DATA_CANCELAMENTO_VAGA, PR.ID_CLINICA
              FROM MDL_OFERTA O
              JOIN MDL_PACIENTE PA ON PA.ID = O.ID_PACIENTE
              JOIN MDL_CONSULTA C ON C.ID = O.ID_CONSULTA_VAGA
              JOIN MDL_PROFISSIONAL PR ON PR.ID = C.ID_PROFISSIONAL
            """;

    public OfertaDAO(DataSource dataSource) {
        super(dataSource);
    }

    public Oferta inserir(Oferta o) {
        String sql = "INSERT INTO MDL_OFERTA (ID_CONSULTA_VAGA, ID_INTERESSE, ID_PACIENTE, METODO, STATUS, TENTATIVA, "
                + "DISTANCIA_KM, TEMPO_ESTIMADO_MIN, RAIO_BUSCA_KM, DATA_ENVIO, DATA_EXPIRACAO) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        long id = inserir(sql, ps -> {
            ps.setLong(1, o.getVaga().getId());
            ps.setLong(2, o.getInteresse().getId());
            ps.setLong(3, o.getPaciente().getId());
            setEnum(ps, 4, o.getMetodo());
            setEnum(ps, 5, o.getStatus());
            ps.setInt(6, o.getTentativa());
            setDouble(ps, 7, o.getDistanciaKm());
            setInt(ps, 8, o.getTempoEstimadoMin());
            setDouble(ps, 9, o.getRaioBuscaKm());
            setDataHora(ps, 10, o.getDataEnvio());
            setDataHora(ps, 11, o.getDataExpiracao());
        });
        o.setId(id);
        return o;
    }

    /** Atualiza a resposta da oferta (aceite, recusa ou expiração). */
    public boolean atualizarResposta(Oferta o) {
        String sql = "UPDATE MDL_OFERTA SET STATUS=?, DATA_RESPOSTA=?, ID_CONSULTA_GERADA=?, DIAS_ANTECIPADOS=? WHERE ID=?";
        return executar(sql, ps -> {
            setEnum(ps, 1, o.getStatus());
            setDataHora(ps, 2, o.getDataResposta());
            setLong(ps, 3, o.getConsultaGerada() == null ? null : o.getConsultaGerada().getId());
            setInt(ps, 4, o.getDiasAntecipados());
            ps.setLong(5, o.getId());
        }) > 0;
    }

    public Optional<Oferta> buscarPorId(Long id) {
        return consultarUm(SELECT + " WHERE O.ID = ?", ps -> ps.setLong(1, id), this::mapear);
    }

    public List<Oferta> listar(Long idVaga, Long idPaciente, StatusOferta status, Long idClinica) {
        Filtro filtro = new Filtro()
                .se(idVaga, "O.ID_CONSULTA_VAGA = ?")
                .se(idPaciente, "O.ID_PACIENTE = ?")
                .se(status, "O.STATUS = ?")
                .se(idClinica, "PR.ID_CLINICA = ?");
        return consultarLista(SELECT + filtro.where() + " ORDER BY O.DATA_ENVIO DESC, O.ID DESC",
                filtro::preencher, this::mapear);
    }

    public Optional<Oferta> buscarPendentePorVaga(Long idVaga) {
        return consultarUm(SELECT + " WHERE O.ID_CONSULTA_VAGA = ? AND O.STATUS = 'PENDENTE'",
                ps -> ps.setLong(1, idVaga), this::mapear);
    }

    public boolean vagaJaPreenchida(Long idVaga) {
        return contar("SELECT COUNT(*) FROM MDL_OFERTA WHERE ID_CONSULTA_VAGA = ? AND STATUS = 'ACEITA'",
                ps -> ps.setLong(1, idVaga)) > 0;
    }

    public int contarPorVaga(Long idVaga) {
        return (int) contar("SELECT COUNT(*) FROM MDL_OFERTA WHERE ID_CONSULTA_VAGA = ?", ps -> ps.setLong(1, idVaga));
    }

    /** Pacientes que já receberam oferta desta vaga (não recebem de novo). */
    public Set<Long> pacientesJaOfertados(Long idVaga) {
        return new HashSet<>(consultarLista("SELECT ID_PACIENTE FROM MDL_OFERTA WHERE ID_CONSULTA_VAGA = ?",
                ps -> ps.setLong(1, idVaga), rs -> rs.getLong(1)));
    }

    /** Maior raio já usado nesta vaga (para a ampliação progressiva da busca por proximidade). */
    public Double maiorRaioUsado(Long idVaga) {
        return consultarUm("SELECT MAX(RAIO_BUSCA_KM) AS RAIO FROM MDL_OFERTA WHERE ID_CONSULTA_VAGA = ?",
                ps -> ps.setLong(1, idVaga), rs -> getDouble(rs, "RAIO"))
                .orElse(null);
    }

    public List<Oferta> listarPendentesVencidas(LocalDateTime agora) {
        return consultarLista(SELECT + " WHERE O.STATUS = 'PENDENTE' AND O.DATA_EXPIRACAO < ?",
                ps -> setDataHora(ps, 1, agora), this::mapear);
    }

    private Oferta mapear(ResultSet rs) throws SQLException {
        Oferta o = new Oferta();
        o.setId(rs.getLong("ID"));

        Consulta vaga = new Consulta(rs.getLong("ID_CONSULTA_VAGA"));
        vaga.setDataHora(getDataHora(rs, "DATA_HORA_VAGA"));
        vaga.setDataCancelamento(getDataHora(rs, "DATA_CANCELAMENTO_VAGA"));
        o.setVaga(vaga);

        o.setInteresse(new InteresseConsulta(rs.getLong("ID_INTERESSE")));

        Paciente pa = new Paciente(rs.getLong("ID_PACIENTE"));
        pa.setNome(rs.getString("NOME_PACIENTE"));
        o.setPaciente(pa);

        o.setMetodo(getEnum(rs, "METODO", MetodoDistribuicao.class));
        o.setStatus(getEnum(rs, "STATUS", StatusOferta.class));
        o.setTentativa(rs.getInt("TENTATIVA"));
        o.setDistanciaKm(getDouble(rs, "DISTANCIA_KM"));
        o.setTempoEstimadoMin(getInt(rs, "TEMPO_ESTIMADO_MIN"));
        o.setRaioBuscaKm(getDouble(rs, "RAIO_BUSCA_KM"));
        o.setDataEnvio(getDataHora(rs, "DATA_ENVIO"));
        o.setDataExpiracao(getDataHora(rs, "DATA_EXPIRACAO"));
        o.setDataResposta(getDataHora(rs, "DATA_RESPOSTA"));
        Long idGerada = getLong(rs, "ID_CONSULTA_GERADA");
        if (idGerada != null) o.setConsultaGerada(new Consulta(idGerada));
        o.setDiasAntecipados(getInt(rs, "DIAS_ANTECIPADOS"));
        return o;
    }
}
