package br.com.fiap.medlink.controller;

import br.com.fiap.medlink.bo.InteresseBO;
import br.com.fiap.medlink.model.InteresseConsulta;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/interesses")
@Tag(name = "Lista de interesse")
public class InteresseController {

    private final InteresseBO bo;

    public InteresseController(InteresseBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<InteresseConsulta> listar(@RequestParam(required = false) Long paciente,
                                          @RequestParam(required = false) Long especialidade,
                                          @RequestParam(required = false) Boolean ativo) {
        return bo.listar(paciente, especialidade, ativo);
    }

    @GetMapping("/{id}")
    public InteresseConsulta buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<InteresseConsulta> cadastrar(@RequestBody @Valid InteresseConsulta interesse) {
        InteresseConsulta criado = bo.cadastrar(interesse);
        return ResponseEntity.created(URI.create("/interesses/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public InteresseConsulta atualizar(@PathVariable Long id, @RequestBody @Valid InteresseConsulta interesse) {
        return bo.atualizar(id, interesse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        bo.remover(id);
        return ResponseEntity.noContent().build();
    }
}
