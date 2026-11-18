package br.com.fiap.medlink.bo.distribuicao;

import br.com.fiap.medlink.model.InteresseConsulta;
import br.com.fiap.medlink.model.Paciente;

/**
 * Paciente compatível com uma vaga. Distância e tempo estimado só são
 * calculados pela estratégia de proximidade.
 */
public record Candidato(
        InteresseConsulta interesse,
        Paciente paciente,
        Double distanciaKm,
        Integer tempoEstimadoMin
) {
    public Candidato(InteresseConsulta interesse, Paciente paciente) {
        this(interesse, paciente, null, null);
    }

    public Candidato comDeslocamento(double distanciaKm, int tempoEstimadoMin) {
        return new Candidato(interesse, paciente, Math.round(distanciaKm * 100) / 100.0, tempoEstimadoMin);
    }
}
