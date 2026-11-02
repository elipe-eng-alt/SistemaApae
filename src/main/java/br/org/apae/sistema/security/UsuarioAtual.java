package br.org.apae.sistema.security;

import br.org.apae.sistema.model.Usuario;
import br.org.apae.sistema.repository.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Dá acesso ao usuário que está logado na requisição atual. */
@Component
public class UsuarioAtual {

    private final UsuarioRepository usuarios;

    public UsuarioAtual(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Optional<Usuario> buscar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return usuarios.findByLoginIgnoreCase(auth.getName());
    }

    public Usuario obter() {
        return buscar().orElseThrow(() -> new IllegalStateException("Nenhum usuário logado"));
    }
}
