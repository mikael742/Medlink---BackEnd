package br.com.fiap.medlink.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Monta cláusulas WHERE dinâmicas apenas com os filtros informados,
 * mantendo os valores como parâmetros do PreparedStatement (sem SQL injection).
 */
class Filtro {

    private final List<String> condicoes = new ArrayList<>();
    private final List<Object> valores = new ArrayList<>();

    /** Adiciona a condição somente se o valor não for nulo. */
    Filtro se(Object valor, String condicao) {
        if (valor != null) {
            condicoes.add(condicao);
            valores.add(valor instanceof Enum<?> e ? e.name() : valor);
        }
        return this;
    }

    String where() {
        return condicoes.isEmpty() ? "" : " WHERE " + String.join(" AND ", condicoes);
    }

    void preencher(PreparedStatement ps) throws SQLException {
        for (int i = 0; i < valores.size(); i++) {
            ps.setObject(i + 1, valores.get(i));
        }
    }
}
