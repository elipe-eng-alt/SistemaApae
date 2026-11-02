package br.org.apae.sistema.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Enquanto o usuário estiver com a senha inicial, só deixa acessar a tela de troca de senha. */
@Configuration
public class TrocaSenhaObrigatoria implements WebMvcConfigurer, HandlerInterceptor {

    private final UsuarioAtual usuarioAtual;

    public TrocaSenhaObrigatoria(UsuarioAtual usuarioAtual) {
        this.usuarioAtual = usuarioAtual;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).excludePathPatterns("/minha-senha", "/login", "/logout", "/css/**", "/error");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (usuarioAtual.buscar().filter(u -> u.isTrocarSenha()).isPresent()) {
            response.sendRedirect(request.getContextPath() + "/minha-senha");
            return false;
        }
        return true;
    }
}
