package br.com.fiap.medlink.model.enums;

public enum TipoAtendimento {
    PRESENCIAL,
    TELECONSULTA,
    /** Usado apenas na lista de interesse: o paciente aceita qualquer formato. */
    QUALQUER;

    /** Verifica se a preferência do paciente (this) aceita o tipo da vaga. */
    public boolean aceita(TipoAtendimento tipoDaVaga) {
        return this == QUALQUER || this == tipoDaVaga;
    }
}
