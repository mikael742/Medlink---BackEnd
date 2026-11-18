package br.com.fiap.medlink.bo;

import br.com.fiap.medlink.dao.ConsultaDAO;
import br.com.fiap.medlink.dao.OfertaDAO;
import br.com.fiap.medlink.dto.Indicadores;
import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.Oferta;
import br.com.fiap.medlink.model.enums.MetodoDistribuicao;
import br.com.fiap.medlink.model.enums.StatusConsulta;
import br.com.fiap.medlink.model.enums.StatusOferta;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;

/** Calcula os indicadores que mostram se a solução funciona (seção 8 da proposta). */
@Service
public class IndicadoresBO {

    private final ClinicaBO clinicaBO;
    private final ConsultaDAO consultaDAO;
    private final OfertaDAO ofertaDAO;

    public IndicadoresBO(ClinicaBO clinicaBO, ConsultaDAO consultaDAO, OfertaDAO ofertaDAO) {
        this.clinicaBO = clinicaBO;
        this.consultaDAO = consultaDAO;
        this.ofertaDAO = ofertaDAO;
    }

    public Indicadores calcular(Long idClinica) {
        Clinica clinica = clinicaBO.buscar(idClinica);
        long canceladas = consultaDAO.listar(StatusConsulta.CANCELADA, null, null, idClinica).size();
        List<Oferta> ofertas = ofertaDAO.listar(null, null, null, idClinica);
        List<Oferta> aceitas = ofertas.stream().filter(o -> o.getStatus() == StatusOferta.ACEITA).toList();

        long recusadas = contar(ofertas, StatusOferta.RECUSADA);
        long expiradas = contar(ofertas, StatusOferta.EXPIRADA);
        long respondidas = aceitas.size() + recusadas + expiradas;

        OptionalDouble mediaDias = aceitas.stream()
                .map(Oferta::getDiasAntecipados).filter(Objects::nonNull)
                .mapToInt(Integer::intValue).average();
        OptionalDouble tempoMedio = aceitas.stream()
                .filter(o -> o.getVaga().getDataCancelamento() != null && o.getDataResposta() != null)
                .mapToLong(o -> Duration.between(o.getVaga().getDataCancelamento(), o.getDataResposta()).toMinutes())
                .average();

        Map<MetodoDistribuicao, Long> porMetodo = new EnumMap<>(MetodoDistribuicao.class);
        for (MetodoDistribuicao m : MetodoDistribuicao.values()) {
            porMetodo.put(m, aceitas.stream().filter(o -> o.getMetodo() == m).count());
        }

        return new Indicadores(
                clinica.getId(), clinica.getNome(),
                canceladas, aceitas.size(), percentual(aceitas.size(), canceladas),
                mediaDias.isPresent() ? arredondar(mediaDias.getAsDouble()) : null,
                tempoMedio.isPresent() ? arredondar(tempoMedio.getAsDouble()) : null,
                ofertas.size(), aceitas.size(), recusadas, expiradas,
                percentual(aceitas.size(), respondidas),
                porMetodo);
    }

    private static long contar(List<Oferta> ofertas, StatusOferta status) {
        return ofertas.stream().filter(o -> o.getStatus() == status).count();
    }

    private static double percentual(long parte, long total) {
        return total == 0 ? 0 : arredondar(parte * 100.0 / total);
    }

    private static double arredondar(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
