package br.com.fiap.medlink.controller;

import br.com.fiap.medlink.bo.ClinicaBO;
import br.com.fiap.medlink.bo.IndicadoresBO;
import br.com.fiap.medlink.dto.Indicadores;
import br.com.fiap.medlink.model.Clinica;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clinicas")
@Tag(name = "Clínicas")
public class ClinicaController {

    private final ClinicaBO bo;
    private final IndicadoresBO indicadoresBO;

    public ClinicaController(ClinicaBO bo, IndicadoresBO indicadoresBO) {
        this.bo = bo;
        this.indicadoresBO = indicadoresBO;
    }

    @GetMapping
    public List<Clinica> listar() {
        return bo.listar();
    }

    @GetMapping("/{id}")
    public Clinica buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @Operation(summary = "Painel de resultados da clínica: vagas recuperadas, antecipação média, taxa de aceite...")
    @GetMapping("/{id}/indicadores")
    public Indicadores indicadores(@PathVariable Long id) {
        return indicadoresBO.calcular(id);
    }

    @PostMapping
    public ResponseEntity<Clinica> cadastrar(@RequestBody @Valid Clinica clinica) {
        Clinica criada = bo.cadastrar(clinica);
        return ResponseEntity.created(URI.create("/clinicas/" + criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    public Clinica atualizar(@PathVariable Long id, @RequestBody @Valid Clinica clinica) {
        return bo.atualizar(id, clinica);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        bo.remover(id);
        return ResponseEntity.noContent().build();
    }
}
