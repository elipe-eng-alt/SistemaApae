package br.org.apae.sistema.web;

import br.org.apae.sistema.model.Profissional;
import br.org.apae.sistema.repository.AtendimentoRepository;
import br.org.apae.sistema.repository.ProfissionalRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalRepository profissionais;
    private final AtendimentoRepository atendimentos;

    public ProfissionalController(ProfissionalRepository profissionais, AtendimentoRepository atendimentos) {
        this.profissionais = profissionais;
        this.atendimentos = atendimentos;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("profissionais", profissionais.findAllByOrderByNomeAsc());
        return "profissionais/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("profissional", new Profissional());
        return "profissionais/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("profissional", profissionais.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND)));
        return "profissionais/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("profissional") Profissional profissional, BindingResult erros,
                         RedirectAttributes redirect) {
        if (erros.hasErrors()) {
            return "profissionais/form";
        }
        profissionais.save(profissional);
        redirect.addFlashAttribute("sucesso", "Profissional " + profissional.getNome() + " salvo(a).");
        return "redirect:/profissionais";
    }

    /** Exclui se nunca teve atendimento; caso contrário apenas inativa, para manter o histórico. */
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        Profissional p = profissionais.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        if (atendimentos.existsByProfissionalId(id)) {
            p.setAtivo(false);
            profissionais.save(p);
            redirect.addFlashAttribute("sucesso", p.getNome() + " possui atendimentos e foi marcado(a) como inativo(a).");
        } else {
            profissionais.delete(p);
            redirect.addFlashAttribute("sucesso", "Profissional " + p.getNome() + " excluído(a).");
        }
        return "redirect:/profissionais";
    }
}
