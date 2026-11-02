package br.org.apae.sistema.model;

import jakarta.persistence.*;

/** Acesso ao sistema. Usuários com perfil PROFISSIONAL ficam ligados ao cadastro do profissional. */
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String login;

    @Column(nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @OneToOne
    @JoinColumn(unique = true)
    private Profissional profissional;

    private boolean ativo = true;

    /** Obriga a trocar a senha no próximo acesso (senha inicial definida pela administração). */
    private boolean trocarSenha = true;

    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional profissional) { this.profissional = profissional; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public boolean isTrocarSenha() { return trocarSenha; }
    public void setTrocarSenha(boolean trocarSenha) { this.trocarSenha = trocarSenha; }
}
