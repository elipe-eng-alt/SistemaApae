package br.org.apae.sistema.web;

import br.org.apae.sistema.repository.AtendimentoRepository;
import br.org.apae.sistema.repository.ProfissionalRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/** Agenda diária do profissional: os horários do dia com cada paciente. */
@Controller
public class AgendaController {

    private final AtendimentoRepository atendimentos;
    private final ProfissionalRepository profissionais;

    public AgendaController(AtendimentoRepository atendimentos, ProfissionalRepository profissionais) {
        this.atendimentos = atendimentos;
        this.profissionais = profissionais;
    }

    @GetMapping("/agenda")
    public String agenda(@RequestParam(required = false) Long profissionalId,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                         Model model) {
        LocalDate dia = data != null ? data : LocalDate.now();
        model.addAttribute("data", dia);
        model.addAttribute("profissionais", profissionais.findByAtivoTrueOrderByNomeAsc());
        if (profissionalId != null) {
            profissionais.findById(profissionalId).ifPresent(p -> {
                model.addAttribute("profissional", p);
                model.addAttribute("atendimentos",
                        atendimentos.findByProfissionalIdAndDataOrderByHoraInicioAsc(p.getId(), dia));
            });
        }
        return "agenda/dia";
    }
}
