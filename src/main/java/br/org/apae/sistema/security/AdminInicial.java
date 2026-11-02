package br.org.apae.sistema.security;

import br.org.apae.sistema.model.Perfil;
import br.org.apae.sistema.model.Usuario;
import br.org.apae.sistema.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Na primeira execução (sem nenhum usuário), cria o usuário "admin", que deve trocar a senha no primeiro acesso. */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String senhaInicial;

    public AdminInicial(UsuarioRepository usuarios, PasswordEncoder encoder,
                        @Value("${apae.admin.senha-inicial:admin}") String senhaInicial) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.senhaInicial = senhaInicial;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarios.count() > 0) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setLogin("admin");
        admin.setSenhaHash(encoder.encode(senhaInicial));
        admin.setPerfil(Perfil.ADMIN);
        admin.setTrocarSenha(true);
        usuarios.save(admin);
        log.warn("Usuário inicial criado: login 'admin'. Troque a senha no primeiro acesso.");
    }
}
