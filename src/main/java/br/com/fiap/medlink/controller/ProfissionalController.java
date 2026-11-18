package br.com.fiap.medlink.controller;

import br.com.fiap.medlink.bo.ProfissionalBO;
import br.com.fiap.medlink.model.Profissional;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/profissionais")
@Tag(name = "Profissionais")
public class ProfissionalController {

    private final ProfissionalBO bo;

    public ProfissionalController(ProfissionalBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<Profissional> listar(@RequestParam(required = false) Long clinica,
                                     @RequestParam(required = false) Long especialidade) {
        return bo.listar(clinica, especialidade);
    }

    @GetMapping("/{id}")
    public Profissional buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Profissional> cadastrar(@RequestBody @Valid Profissional profissional) {
        Profissional criado = bo.cadastrar(profissional);
        return ResponseEntity.created(URI.create("/profissionais/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public Profissional atualizar(@PathVariable Long id, @RequestBody @Valid Profissional profissional) {
        return bo.atualizar(id, profissional);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        bo.remover(id);
        return ResponseEntity.noContent().build();
    }
}
