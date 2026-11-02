package br.org.apae.sistema.web;

import br.org.apae.sistema.model.Paciente;
import br.org.apae.sistema.repository.AtendimentoRepository;
import br.org.apae.sistema.repository.PacienteRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteRepository pacientes;
    private final AtendimentoRepository atendimentos;

    public PacienteController(PacienteRepository pacientes, AtendimentoRepository atendimentos) {
        this.pacientes = pacientes;
        this.atendimentos = atendimentos;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String busca, Model model) {
        model.addAttribute("busca", busca);
        model.addAttribute("pacientes", busca == null || busca.isBlank()
                ? pacientes.findAllByOrderByNomeAsc()
                : pacientes.findByNomeContainingIgnoreCaseOrderByNomeAsc(busca.trim()));
        return "pacientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("paciente", new Paciente());
        return "pacientes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("paciente", pacientes.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND)));
        return "pacientes/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("paciente") Paciente paciente, BindingResult erros,
                         RedirectAttributes redirect) {
        if (erros.hasErrors()) {
            return "pacientes/form";
        }
        pacientes.save(paciente);
        redirect.addFlashAttribute("sucesso", "Paciente " + paciente.getNome() + " salvo.");
        return "redirect:/pacientes";
    }

    /** Exclui se nunca teve atendimento; caso contrário apenas inativa, para manter o histórico. */
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        Paciente p = pacientes.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        if (atendimentos.existsByPacienteId(id)) {
            p.setAtivo(false);
            pacientes.save(p);
            redirect.addFlashAttribute("sucesso", p.getNome() + " possui atendimentos e foi marcado(a) como inativo(a).");
        } else {
            pacientes.delete(p);
            redirect.addFlashAttribute("sucesso", "Paciente " + p.getNome() + " excluído.");
        }
        return "redirect:/pacientes";
    }
}
