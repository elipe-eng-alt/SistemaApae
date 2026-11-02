package br.org.apae.sistema.security;

import br.org.apae.sistema.repository.UsuarioRepository;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a
                        .requestMatchers(caminho("/css/**"), caminho("/login"), caminho("/error")).permitAll()
                        // O que o profissional pode usar; o controller garante que ele só veja/altere a própria agenda.
                        .requestMatchers(caminho("/"), caminho("/agenda"), caminho("/minha-senha"),
                                caminho("/atendimentos/{id}/status")).authenticated()
                        .requestMatchers(PathRequest.toH2Console()).hasRole("ADMIN")
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
                .logout(l -> l.logoutSuccessUrl("/login?saiu").permitAll())
                .csrf(c -> c.ignoringRequestMatchers(PathRequest.toH2Console()))
                .headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }

    @Bean
    UserDetailsService usuarios(UsuarioRepository repo) {
        return login -> repo.findByLoginIgnoreCase(login)
                .map(u -> User.withUsername(u.getLogin())
                        .password(u.getSenhaHash())
                        .roles(u.getPerfil().name())
                        .disabled(!u.isAtivo() || (u.getProfissional() != null && !u.getProfissional().isAtivo()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(login));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private static RequestMatcher caminho(String padrao) {
        return PathPatternRequestMatcher.withDefaults().matcher(padrao);
    }
}
