package br.org.apae.sistema;

import br.org.apae.sistema.model.*;
import br.org.apae.sistema.repository.AtendimentoRepository;
import br.org.apae.sistema.repository.PacienteRepository;
import br.org.apae.sistema.repository.ProfissionalRepository;
import br.org.apae.sistema.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:seguranca;DB_CLOSE_DELAY=-1")
class SegurancaTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired ProfissionalRepository profissionais;
    @Autowired PacienteRepository pacientes;
    @Autowired AtendimentoRepository atendimentos;
    @Autowired PasswordEncoder encoder;

    Profissional ana;
    Profissional carlos;
    Atendimento atendimentoDoCarlos;

    @BeforeEach
    void preparar() {
        atendimentos.deleteAll();
        usuarios.deleteAll();
        profissionais.deleteAll();
        pacientes.deleteAll();
        ana = profissional("Ana Fisio");
        carlos = profissional("Carlos Professor");
        Paciente joao = new Paciente();
        joao.setNome("João Paciente");
        pacientes.save(joao);
        atendimentoDoCarlos = new Atendimento();
        atendimentoDoCarlos.setProfissional(carlos);
        atendimentoDoCarlos.setPaciente(joao);
        atendimentoDoCarlos.setTipo(Especialidade.PEDAGOGIA);
        atendimentoDoCarlos.setData(LocalDate.now());
        atendimentoDoCarlos.setHoraInicio(LocalTime.of(9, 0));
        atendimentoDoCarlos.setHoraFim(LocalTime.of(9, 40));
        atendimentos.save(atendimentoDoCarlos);
        usuario("ana", Perfil.PROFISSIONAL, ana, false);
        usuario("chefe", Perfil.ADMIN, null, false);
    }

    @Test
    void semLoginVaiParaTelaDeLogin() throws Exception {
        mvc.perform(get("/pacientes")).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginComSenhaCorreta() throws Exception {
        mvc.perform(formLogin("/login").user("ana").password("senha123")).andExpect(authenticated().withUsername("ana"));
        mvc.perform(formLogin("/login").user("ana").password("errada")).andExpect(unauthenticated());
    }

    @Test
    void profissionalNaoAcessaCadastros() throws Exception {
        mvc.perform(get("/pacientes").with(user("ana").roles("PROFISSIONAL"))).andExpect(status().isForbidden());
        mvc.perform(get("/atendimentos/novo").with(user("ana").roles("PROFISSIONAL"))).andExpect(status().isForbidden());
        mvc.perform(get("/usuarios").with(user("ana").roles("PROFISSIONAL"))).andExpect(status().isForbidden());
    }

    @Test
    void profissionalVeSoAPropriaAgenda() throws Exception {
        mvc.perform(get("/agenda").param("profissionalId", carlos.getId().toString())
                        .with(user("ana").roles("PROFISSIONAL")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana Fisio")))
                .andExpect(content().string(not(containsString("João Paciente"))));
    }

    @Test
    void profissionalNaoAlteraAtendimentoDeOutro() throws Exception {
        mvc.perform(post("/atendimentos/{id}/status", atendimentoDoCarlos.getId()).param("status", "FALTOU")
                        .with(user("ana").roles("PROFISSIONAL")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminVeAgendaDeQualquerProfissional() throws Exception {
        mvc.perform(get("/agenda").param("profissionalId", carlos.getId().toString())
                        .with(user("chefe").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("João Paciente")));
    }

    @Test
    void senhaInicialObrigaTroca() throws Exception {
        usuario("novo", Perfil.PROFISSIONAL, carlos, true);
        mvc.perform(get("/agenda").with(user("novo").roles("PROFISSIONAL")))
                .andExpect(redirectedUrl("/minha-senha"));
    }

    private Profissional profissional(String nome) {
        Profissional p = new Profissional();
        p.setNome(nome);
        p.setEspecialidade(Especialidade.FISIOTERAPIA);
        return profissionais.save(p);
    }

    private void usuario(String login, Perfil perfil, Profissional profissional, boolean trocarSenha) {
        Usuario u = new Usuario();
        u.setLogin(login);
        u.setSenhaHash(encoder.encode("senha123"));
        u.setPerfil(perfil);
        u.setProfissional(profissional);
        u.setTrocarSenha(trocarSenha);
        usuarios.save(u);
    }
}
