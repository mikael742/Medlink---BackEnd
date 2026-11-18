package br.com.fiap.medlink.controller;

import br.com.fiap.medlink.bo.EspecialidadeBO;
import br.com.fiap.medlink.model.Especialidade;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/especialidades")
@Tag(name = "Especialidades")
public class EspecialidadeController {

    private final EspecialidadeBO bo;

    public EspecialidadeController(EspecialidadeBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<Especialidade> listar() {
        return bo.listar();
    }

    @GetMapping("/{id}")
    public Especialidade buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Especialidade> cadastrar(@RequestBody @Valid Especialidade especialidade) {
        Especialidade criada = bo.cadastrar(especialidade);
        return ResponseEntity.created(URI.create("/especialidades/" + criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    public Especialidade atualizar(@PathVariable Long id, @RequestBody @Valid Especialidade especialidade) {
        return bo.atualizar(id, especialidade);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        bo.remover(id);
        return ResponseEntity.noContent().build();
    }
}
