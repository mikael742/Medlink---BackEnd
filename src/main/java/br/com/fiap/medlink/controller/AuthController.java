package br.com.fiap.medlink.controller;

import br.com.fiap.medlink.bo.AuthBO;
import br.com.fiap.medlink.dto.AuthDTOs.CadastroClinicaRequest;
import br.com.fiap.medlink.dto.AuthDTOs.CadastroPacienteRequest;
import br.com.fiap.medlink.dto.AuthDTOs.LoginRequest;
import br.com.fiap.medlink.dto.AuthDTOs.Sessao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Login e cadastro de contas")
public class AuthController {

    private final AuthBO bo;

    public AuthController(AuthBO bo) {
        this.bo = bo;
    }

    @Operation(summary = "Entra com e-mail e senha; a resposta diz se a conta é de PACIENTE ou CLINICA")
    @PostMapping("/login")
    public Sessao login(@RequestBody @Valid LoginRequest request) {
        return bo.login(request);
    }

    @Operation(summary = "Cria a conta e o cadastro de um paciente")
    @PostMapping("/cadastro/paciente")
    public ResponseEntity<Sessao> cadastrarPaciente(@RequestBody @Valid CadastroPacienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bo.cadastrarPaciente(request));
    }

    @Operation(summary = "Cria a conta e o cadastro de uma clínica")
    @PostMapping("/cadastro/clinica")
    public ResponseEntity<Sessao> cadastrarClinica(@RequestBody @Valid CadastroClinicaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bo.cadastrarClinica(request));
    }
}
