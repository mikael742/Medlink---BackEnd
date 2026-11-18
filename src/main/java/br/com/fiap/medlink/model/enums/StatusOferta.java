package br.com.fiap.medlink.model.enums;

/** Situação de uma oferta de vaga enviada a um paciente. */
public enum StatusOferta {
    PENDENTE,
    ACEITA,
    RECUSADA,
    EXPIRADA;

    public boolean isFinalizada() {
        return this != PENDENTE;
    }
}
