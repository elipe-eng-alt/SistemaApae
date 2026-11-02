package br.org.apae.sistema.web;

import br.org.apae.sistema.model.Perfil;
import br.org.apae.sistema.model.Profissional;
import br.org.apae.sistema.model.Usuario;
import br.org.apae.sistema.repository.ProfissionalRepository;
import br.org.apae.sistema.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/** Cadastro de acessos (login e senha) da administração e dos profissionais. */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    static final int SENHA_MINIMA = 6;

    private final UsuarioRepository usuarios;
    private final ProfissionalRepository profissionais;
    private final PasswordEncoder encoder;

    public UsuarioController(UsuarioRepository usuarios, ProfissionalRepository profissionais, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.profissionais = profissionais;
        this.encoder = encoder;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarios.findAllByOrderByLoginAsc());
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long profissionalId, Model model) {
        UsuarioForm form = new UsuarioForm();
        if (profissionalId != null) {
            form.setProfissionalId(profissionalId);
            usuarios.findByProfissionalId(profissionalId).ifPresent(u -> form.setId(u.getId()));
        } else {
            form.setPerfil(Perfil.ADMIN);
        }
        if (form.getId() != null) {
            return "redirect:/usuarios/" + form.getId() + "/editar";
        }
        return formulario(form, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(UsuarioForm.de(buscar(id)), model);
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("form") UsuarioForm form, BindingResult erros,
                         Model model, RedirectAttributes redirect) {
        Usuario u = form.getId() == null ? new Usuario() : buscar(form.getId());
        validar(form, u, erros);
        if (erros.hasErrors()) {
            return formulario(form, model);
        }
        u.setLogin(form.getLogin().trim());
        u.setPerfil(form.getPerfil());
        u.setAtivo(form.isAtivo());
        u.setProfissional(form.getPerfil() == Perfil.PROFISSIONAL
                ? profissionais.findById(form.getProfissionalId()).orElseThrow()
                : null);
        if (form.getSenha() != null && !form.getSenha().isBlank()) {
            u.setSenhaHash(encoder.encode(form.getSenha()));
            u.setTrocarSenha(true);
        }
        usuarios.save(u);
        redirect.addFlashAttribute("sucesso", "Acesso '" + u.getLogin() + "' salvo."
                + (u.isTrocarSenha() ? " A pessoa deverá trocar a senha no primeiro acesso." : ""));
        return "redirect:/usuarios";
    }

    private void validar(UsuarioForm form, Usuario atual, BindingResult erros) {
        String senha = form.getSenha() == null ? "" : form.getSenha();
        if (form.getId() == null && senha.isBlank()) {
            erros.rejectValue("senha", "obrigatoria", "Informe a senha inicial");
        } else if (!senha.isBlank() && senha.length() < SENHA_MINIMA) {
            erros.rejectValue("senha", "curta", "A senha deve ter pelo menos " + SENHA_MINIMA + " caracteres");
        }
        if (form.getLogin() != null) {
            usuarios.findByLoginIgnoreCase(form.getLogin().trim())
                    .filter(outro -> !outro.getId().equals(form.getId()))
                    .ifPresent(outro -> erros.rejectValue("login", "duplicado", "Este login já está em uso"));
        }
        if (form.getPerfil() == Perfil.PROFISSIONAL) {
            if (form.getProfissionalId() == null) {
                erros.rejectValue("profissionalId", "obrigatorio", "Escolha o profissional deste acesso");
            } else {
                usuarios.findByProfissionalId(form.getProfissionalId())
                        .filter(outro -> !outro.getId().equals(form.getId()))
                        .ifPresent(outro -> erros.rejectValue("profissionalId", "duplicado",
                                "Este profissional já tem o acesso '" + outro.getLogin() + "'"));
            }
        }
        boolean deixaDeSerAdminAtivo = atual.getId() != null && atual.isAdmin() && atual.isAtivo()
                && (form.getPerfil() != Perfil.ADMIN || !form.isAtivo());
        if (deixaDeSerAdminAtivo && usuarios.countByPerfilAndAtivoTrue(Perfil.ADMIN) <= 1) {
            erros.reject("ultimoAdmin", "Este é o único acesso de administração ativo; crie outro antes de alterá-lo.");
        }
    }

    private String formulario(UsuarioForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("perfis", Perfil.values());
        model.addAttribute("profissionais", profissionais.findAllByOrderByNomeAsc());
        return "usuarios/form";
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }
}
