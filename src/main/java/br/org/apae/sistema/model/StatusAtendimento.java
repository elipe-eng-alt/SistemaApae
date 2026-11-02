package br.org.apae.sistema.model;

public enum StatusAtendimento {
    AGENDADO("Agendado", "primary"),
    REALIZADO("Realizado", "success"),
    FALTOU("Faltou", "warning"),
    CANCELADO("Cancelado", "secondary");

    private final String descricao;
    private final String cor;

    StatusAtendimento(String descricao, String cor) {
        this.descricao = descricao;
        this.cor = cor;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Cor do badge Bootstrap usada na tela. */
    public String getCor() {
        return cor;
    }
}
