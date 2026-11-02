package br.org.apae.sistema.web;

import br.org.apae.sistema.model.Usuario;
import br.org.apae.sistema.repository.AtendimentoRepository;
import br.org.apae.sistema.repository.ProfissionalRepository;
import br.org.apae.sistema.security.UsuarioAtual;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Agenda diária do profissional: os horários do dia com cada paciente.
 * A administração escolhe o profissional; o profissional logado vê sempre a própria agenda.
 */
@Controller
public class AgendaController {

    private final AtendimentoRepository atendimentos;
    private final ProfissionalRepository profissionais;
    private final UsuarioAtual usuarioAtual;

    public AgendaController(AtendimentoRepository atendimentos, ProfissionalRepository profissionais,
                            UsuarioAtual usuarioAtual) {
        this.atendimentos = atendimentos;
        this.profissionais = profissionais;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping("/agenda")
    public String agenda(@RequestParam(required = false) Long profissionalId,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                         Model model) {
        Usuario usuario = usuarioAtual.obter();
        if (!usuario.isAdmin()) {
            profissionalId = usuario.getProfissional().getId();
        }
        LocalDate dia = data != null ? data : LocalDate.now();
        model.addAttribute("data", dia);
        if (usuario.isAdmin()) {
            model.addAttribute("profissionais", profissionais.findByAtivoTrueOrderByNomeAsc());
        }
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
