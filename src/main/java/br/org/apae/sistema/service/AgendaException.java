package br.org.apae.sistema.service;

/** Erro de regra de negócio da agenda (conflito de horário, horário inválido...). */
public class AgendaException extends RuntimeException {

    public AgendaException(String mensagem) {
        super(mensagem);
    }
}
