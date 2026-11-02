package br.org.apae.sistema.model;

public enum Especialidade {
    FISIOTERAPIA("Fisioterapia"),
    HIDROTERAPIA("Hidroterapia"),
    FONOAUDIOLOGIA("Fonoaudiologia"),
    PSICOLOGIA("Psicologia"),
    TERAPIA_OCUPACIONAL("Terapia Ocupacional"),
    PSICOPEDAGOGIA("Psicopedagogia"),
    PEDAGOGIA("Professor(a) / Pedagogia"),
    EDUCACAO_FISICA("Educação Física"),
    SERVICO_SOCIAL("Serviço Social"),
    OUTRO("Outro");

    private final String descricao;

    Especialidade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
