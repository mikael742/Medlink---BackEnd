package br.com.fiap.medlink.bo;

import br.com.fiap.medlink.dao.ClinicaDAO;
import br.com.fiap.medlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.medlink.exception.RegraNegocioException;
import br.com.fiap.medlink.model.Clinica;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClinicaBO {

    // Valores padrão das regras de distribuição (seção 4 da proposta)
    static final int PADRAO_HORAS_FILA = 24;
    static final int PADRAO_HORAS_URGENCIA = 6;
    static final double PADRAO_RAIO_INICIAL_KM = 5;
    static final double PADRAO_RAIO_MAXIMO_KM = 30;
    static final int PADRAO_PRAZO_MIN = 120;
    static final int PADRAO_PRAZO_URGENTE_MIN = 15;

    private final ClinicaDAO dao;

    public ClinicaBO(ClinicaDAO dao) {
        this.dao = dao;
    }

    public List<Clinica> listar() {
        return dao.listar();
    }

    public Clinica buscar(Long id) {
        if (id == null) throw new RegraNegocioException("Informe o id de Clínica");
        return dao.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Clínica", id));
    }

    public Clinica cadastrar(Clinica c) {
        c.setId(null);
        prepararEValidar(c);
        return dao.inserir(c);
    }

    public Clinica atualizar(Long id, Clinica c) {
        buscar(id);
        c.setId(id);
        prepararEValidar(c);
        dao.atualizar(c);
        return c;
    }

    public void remover(Long id) {
        if (!dao.remover(id)) throw new RecursoNaoEncontradoException("Clínica", id);
    }

    private void prepararEValidar(Clinica c) {
        if (c.getHorasLimiteFila() == null) c.setHorasLimiteFila(PADRAO_HORAS_FILA);
        if (c.getHorasLimiteUrgencia() == null) c.setHorasLimiteUrgencia(PADRAO_HORAS_URGENCIA);
        if (c.getRaioInicialKm() == null) c.setRaioInicialKm(PADRAO_RAIO_INICIAL_KM);
        if (c.getRaioMaximoKm() == null) c.setRaioMaximoKm(PADRAO_RAIO_MAXIMO_KM);
        if (c.getPrazoRespostaMin() == null) c.setPrazoRespostaMin(PADRAO_PRAZO_MIN);
        if (c.getPrazoRespostaUrgenteMin() == null) c.setPrazoRespostaUrgenteMin(PADRAO_PRAZO_URGENTE_MIN);
        if (c.getConveniosAceitos() != null) {
            c.setConveniosAceitos(c.getConveniosAceitos().toUpperCase().replace(" ", ""));
        }

        if (c.getHorasLimiteUrgencia() >= c.getHorasLimiteFila()) {
            throw new RegraNegocioException("horasLimiteUrgencia deve ser menor que horasLimiteFila");
        }
        if (c.getRaioInicialKm() > c.getRaioMaximoKm()) {
            throw new RegraNegocioException("raioInicialKm não pode ser maior que raioMaximoKm");
        }
        if (c.getPrazoRespostaUrgenteMin() > c.getPrazoRespostaMin()) {
            throw new RegraNegocioException("prazoRespostaUrgenteMin não pode ser maior que prazoRespostaMin");
        }
        if (c.getLocalizacao() == null || !c.getLocalizacao().temCoordenadas()) {
            throw new RegraNegocioException("Informe latitude e longitude da clínica (usadas no método de proximidade)");
        }
    }
}
