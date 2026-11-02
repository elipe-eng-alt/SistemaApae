package br.org.apae.sistema.web;

import br.org.apae.sistema.model.Especialidade;
import br.org.apae.sistema.model.StatusAtendimento;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

/** Valores usados em várias telas (listas de seleção, data de hoje). */
@ControllerAdvice
public class DadosComunsAdvice {

    @ModelAttribute("especialidades")
    public Especialidade[] especialidades() {
        return Especialidade.values();
    }

    @ModelAttribute("statusAtendimento")
    public StatusAtendimento[] statusAtendimento() {
        return StatusAtendimento.values();
    }

    @ModelAttribute("hoje")
    public LocalDate hoje() {
        return LocalDate.now();
    }
}
