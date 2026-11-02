package br.org.apae.sistema.model;

public enum Perfil {
    /** Secretaria / coordenação: acessa e altera tudo. */
    ADMIN("Administração"),
    /** Professor(a) ou terapeuta: vê apenas a própria agenda. */
    PROFISSIONAL("Profissional");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
