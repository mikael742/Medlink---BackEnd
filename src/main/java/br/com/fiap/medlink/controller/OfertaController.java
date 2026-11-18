package br.com.fiap.medlink.controller;

import br.com.fiap.medlink.bo.OfertaBO;
import br.com.fiap.medlink.dto.ResultadoResposta;
import br.com.fiap.medlink.model.Oferta;
import br.com.fiap.medlink.model.enums.StatusOferta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ofertas")
@Tag(name = "Ofertas")
public class OfertaController {

    private final OfertaBO bo;

    public OfertaController(OfertaBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<Oferta> listar(@RequestParam(required = false) Long vaga,
                               @RequestParam(required = false) Long paciente,
                               @RequestParam(required = false) StatusOferta status,
                               @RequestParam(required = false) Long clinica) {
        return bo.listar(vaga, paciente, status, clinica);
    }

    @GetMapping("/{id}")
    public Oferta buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @Operation(summary = "Paciente aceita a vaga: cria a consulta e libera a antiga (se estava antecipando)")
    @PostMapping("/{id}/aceitar")
    public ResultadoResposta aceitar(@PathVariable Long id) {
        return bo.aceitar(id);
    }

    @Operation(summary = "Paciente recusa a vaga: ela é oferecida ao próximo candidato")
    @PostMapping("/{id}/recusar")
    public ResultadoResposta recusar(@PathVariable Long id) {
        return bo.recusar(id);
    }
}
