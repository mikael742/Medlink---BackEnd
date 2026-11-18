package br.com.fiap.medlink.bo;

import br.com.fiap.medlink.dao.UsuarioDAO;
import br.com.fiap.medlink.dto.AuthDTOs.CadastroClinicaRequest;
import br.com.fiap.medlink.dto.AuthDTOs.CadastroPacienteRequest;
import br.com.fiap.medlink.dto.AuthDTOs.LoginRequest;
import br.com.fiap.medlink.dto.AuthDTOs.Sessao;
import br.com.fiap.medlink.exception.ConflitoException;
import br.com.fiap.medlink.exception.CredenciaisInvalidasException;
import br.com.fiap.medlink.model.Clinica;
import br.com.fiap.medlink.model.Paciente;
import br.com.fiap.medlink.model.Usuario;
import br.com.fiap.medlink.model.enums.PerfilUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Cadastro de contas e login. No cadastro, o paciente (ou a clínica) e o
 * usuário são criados na mesma transação: se um falhar, nada é gravado.
 */
@Service
public class AuthBO {

    private final UsuarioDAO usuarioDAO;
    private final PacienteBO pacienteBO;
    private final ClinicaBO clinicaBO;
    private final Clock clock;

    public AuthBO(UsuarioDAO usuarioDAO, PacienteBO pacienteBO, ClinicaBO clinicaBO, Clock clock) {
        this.usuarioDAO = usuarioDAO;
        this.pacienteBO = pacienteBO;
        this.clinicaBO = clinicaBO;
        this.clock = clock;
    }

    @Transactional
    public Sessao cadastrarPaciente(CadastroPacienteRequest req) {
        String email = normalizar(req.email());
        garantirEmailLivre(email);
        Paciente p = req.paciente();
        if (p.getEmail() == null || p.getEmail().isBlank()) p.setEmail(email);
        Paciente criado = pacienteBO.cadastrar(p);
        Usuario u = criarUsuario(email, req.senha(), PerfilUsuario.PACIENTE);
        u.setIdPaciente(criado.getId());
        usuarioDAO.inserir(u);
        return sessao(u, criado.getNome());
    }

    @Transactional
    public Sessao cadastrarClinica(CadastroClinicaRequest req) {
        String email = normalizar(req.email());
        garantirEmailLivre(email);
        Clinica c = req.clinica();
        if (c.getEmail() == null || c.getEmail().isBlank()) c.setEmail(email);
        Clinica criada = clinicaBO.cadastrar(c);
        Usuario u = criarUsuario(email, req.senha(), PerfilUsuario.CLINICA);
        u.setIdClinica(criada.getId());
        usuarioDAO.inserir(u);
        return sessao(u, criada.getNome());
    }

    /** Valida e-mail e senha e devolve quem é a pessoa (paciente ou clínica). */
    public Sessao login(LoginRequest req) {
        Usuario u = usuarioDAO.buscarPorEmail(normalizar(req.email()))
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!Senha.confere(req.senha(), u.getSalt(), u.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        String nome = u.getPerfil() == PerfilUsuario.PACIENTE
                ? pacienteBO.buscar(u.getIdPaciente()).getNome()
                : clinicaBO.buscar(u.getIdClinica()).getNome();
        return sessao(u, nome);
    }

    private Usuario criarUsuario(String email, String senha, PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setSalt(Senha.gerarSalt());
        u.setSenhaHash(Senha.hash(senha, u.getSalt()));
        u.setPerfil(perfil);
        u.setDataCriacao(LocalDateTime.now(clock));
        return u;
    }

    private void garantirEmailLivre(String email) {
        if (usuarioDAO.existeEmail(email)) {
            throw new ConflitoException("Já existe uma conta com o e-mail " + email);
        }
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase();
    }

    private static Sessao sessao(Usuario u, String nome) {
        return new Sessao(u.getId(), u.getEmail(), u.getPerfil(), nome, u.getIdPaciente(), u.getIdClinica());
    }
}
