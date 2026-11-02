package br.org.apae.sistema.web;

import br.org.apae.sistema.model.Usuario;
import br.org.apae.sistema.repository.UsuarioRepository;
import br.org.apae.sistema.security.UsuarioAtual;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Tela de login e troca da própria senha. */
@Controller
public class ContaController {

    private final UsuarioAtual usuarioAtual;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public ContaController(UsuarioAtual usuarioAtual, UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarioAtual = usuarioAtual;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/minha-senha")
    public String minhaSenha(Model model) {
        model.addAttribute("obrigatoria", usuarioAtual.obter().isTrocarSenha());
        return "minha-senha";
    }

    @PostMapping("/minha-senha")
    public String trocarSenha(@RequestParam String senhaAtual, @RequestParam String novaSenha,
                              @RequestParam String confirmacao, Model model, RedirectAttributes redirect) {
        Usuario u = usuarioAtual.obter();
        String erro = null;
        if (!encoder.matches(senhaAtual, u.getSenhaHash())) {
            erro = "A senha atual está incorreta.";
        } else if (novaSenha.length() < UsuarioController.SENHA_MINIMA) {
            erro = "A nova senha deve ter pelo menos " + UsuarioController.SENHA_MINIMA + " caracteres.";
        } else if (!novaSenha.equals(confirmacao)) {
            erro = "A confirmação não confere com a nova senha.";
        } else if (novaSenha.equals(senhaAtual)) {
            erro = "A nova senha deve ser diferente da atual.";
        }
        if (erro != null) {
            model.addAttribute("erro", erro);
            model.addAttribute("obrigatoria", u.isTrocarSenha());
            return "minha-senha";
        }
        u.setSenhaHash(encoder.encode(novaSenha));
        u.setTrocarSenha(false);
        usuarios.save(u);
        redirect.addFlashAttribute("sucesso", "Senha alterada.");
        return "redirect:/";
    }
}
