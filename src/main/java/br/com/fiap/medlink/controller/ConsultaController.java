package br.com.fiap.medlink.controller;

import br.com.fiap.medlink.bo.ConsultaBO;
import br.com.fiap.medlink.bo.DistribuicaoBO;
import br.com.fiap.medlink.dto.CancelamentoRequest;
import br.com.fiap.medlink.dto.ResultadoCancelamento;
import br.com.fiap.medlink.dto.SimulacaoDistribuicao;
import br.com.fiap.medlink.model.Consulta;
import br.com.fiap.medlink.model.Oferta;
import br.com.fiap.medlink.model.enums.StatusConsulta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/consultas")
@Tag(name = "Consultas e vagas")
public class ConsultaController {

    private final ConsultaBO bo;
    private final DistribuicaoBO distribuicaoBO;

    public ConsultaController(ConsultaBO bo, DistribuicaoBO distribuicaoBO) {
        this.bo = bo;
        this.distribuicaoBO = distribuicaoBO;
    }

    @GetMapping
    public List<Consulta> listar(@RequestParam(required = false) StatusConsulta status,
                                 @RequestParam(required = false) Long paciente,
                                 @RequestParam(required = false) Long profissional,
                                 @RequestParam(required = false) Long clinica) {
        return bo.listar(status, paciente, profissional, clinica);
    }

    @GetMapping("/{id}")
    public Consulta buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Consulta> agendar(@RequestBody @Valid Consulta consulta) {
        Consulta criada = bo.agendar(consulta);
        return ResponseEntity.created(URI.create("/consultas/" + criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    public Consulta atualizar(@PathVariable Long id, @RequestBody @Valid Consulta consulta) {
        return bo.atualizar(id, consulta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        bo.remover(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancela a consulta e dispara automaticamente a distribuição da vaga")
    @PostMapping("/{id}/cancelar")
    public ResultadoCancelamento cancelar(@PathVariable Long id,
                                          @RequestBody(required = false) @Valid CancelamentoRequest request) {
        return bo.cancelar(id, request == null ? null : request.motivo());
    }

    @Operation(summary = "Mostra o método, o ranking de candidatos e os excluídos, sem enviar oferta")
    @GetMapping("/{id}/simulacao")
    public SimulacaoDistribuicao simular(@PathVariable Long id) {
        return distribuicaoBO.simular(id);
    }

    @Operation(summary = "Tenta novamente distribuir uma vaga aberta (ex.: novos pacientes entraram na lista)")
    @PostMapping("/{id}/redistribuir")
    public ResponseEntity<Oferta> redistribuir(@PathVariable Long id) {
        return distribuicaoBO.distribuir(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PatchMapping("/{id}/realizada")
    public Consulta marcarRealizada(@PathVariable Long id) {
        return bo.marcarRealizada(id);
    }
}
