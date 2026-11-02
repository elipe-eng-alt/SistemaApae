package br.org.apae.sistema.web;

import br.org.apae.sistema.model.Perfil;
import br.org.apae.sistema.model.Usuario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Formulário de acesso ao sistema; a senha só é alterada quando preenchida. */
public class UsuarioForm {

    private Long id;

    @Pattern(regexp = "[A-Za-z0-9._-]{3,60}", message = "Use de 3 a 60 letras, números, ponto, hífen ou sublinhado")
    private String login;

    private String senha;

    @NotNull(message = "Escolha o perfil")
    private Perfil perfil = Perfil.PROFISSIONAL;

    private Long profissionalId;

    private boolean ativo = true;

    public static UsuarioForm de(Usuario u) {
        UsuarioForm f = new UsuarioForm();
        f.id = u.getId();
        f.login = u.getLogin();
        f.perfil = u.getPerfil();
        f.profissionalId = u.getProfissional() != null ? u.getProfissional().getId() : null;
        f.ativo = u.isAtivo();
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public Long getProfissionalId() { return profissionalId; }
    public void setProfissionalId(Long profissionalId) { this.profissionalId = profissionalId; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
