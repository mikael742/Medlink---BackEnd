package br.com.fiap.medlink.model.enums;

import java.time.DayOfWeek;

/** Dias da semana em português, usados na disponibilidade do paciente. */
public enum DiaSemana {
    SEG(DayOfWeek.MONDAY),
    TER(DayOfWeek.TUESDAY),
    QUA(DayOfWeek.WEDNESDAY),
    QUI(DayOfWeek.THURSDAY),
    SEX(DayOfWeek.FRIDAY),
    SAB(DayOfWeek.SATURDAY),
    DOM(DayOfWeek.SUNDAY);

    private final DayOfWeek dayOfWeek;

    DiaSemana(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public static DiaSemana de(DayOfWeek dayOfWeek) {
        for (DiaSemana d : values()) {
            if (d.dayOfWeek == dayOfWeek) return d;
        }
        throw new IllegalArgumentException("Dia inválido: " + dayOfWeek);
    }
}
