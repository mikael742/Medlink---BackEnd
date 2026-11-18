package br.com.fiap.medlink.controller;

import br.com.fiap.medlink.bo.PacienteBO;
import br.com.fiap.medlink.model.Paciente;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pacientes")
@Tag(name = "Pacientes")
public class PacienteController {

    private final PacienteBO bo;

    public PacienteController(PacienteBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<Paciente> listar() {
        return bo.listar();
    }

    @GetMapping("/{id}")
    public Paciente buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Paciente> cadastrar(@RequestBody @Valid Paciente paciente) {
        Paciente criado = bo.cadastrar(paciente);
        return ResponseEntity.created(URI.create("/pacientes/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public Paciente atualizar(@PathVariable Long id, @RequestBody @Valid Paciente paciente) {
        return bo.atualizar(id, paciente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        bo.remover(id);
        return ResponseEntity.noContent().build();
    }
}
