package br.org.apae.sistema.web;

import br.org.apae.sistema.model.StatusAtendimento;
import br.org.apae.sistema.repository.AtendimentoRepository;
import br.org.apae.sistema.repository.PacienteRepository;
import br.org.apae.sistema.repository.ProfissionalRepository;
import br.org.apae.sistema.service.AgendaException;
import br.org.apae.sistema.service.AtendimentoForm;
import br.org.apae.sistema.service.AtendimentoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/atendimentos")
public class AtendimentoController {

    private final AtendimentoRepository atendimentos;
    private final ProfissionalRepository profissionais;
    private final PacienteRepository pacientes;
    private final AtendimentoService service;

    public AtendimentoController(AtendimentoRepository atendimentos, ProfissionalRepository profissionais,
                                 PacienteRepository pacientes, AtendimentoService service) {
        this.atendimentos = atendimentos;
        this.profissionais = profissionais;
        this.pacientes = pacientes;
        this.service = service;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                         @RequestParam(required = false) Long profissionalId,
                         @RequestParam(required = false) Long pacienteId,
                         Model model) {
        LocalDate inicio = de != null ? de : LocalDate.now();
        LocalDate fim = ate != null && !ate.isBefore(inicio) ? ate : inicio.plusDays(6);
        model.addAttribute("de", inicio);
        model.addAttribute("ate", fim);
        model.addAttribute("profissionalId", profissionalId);
        model.addAttribute("pacienteId", pacienteId);
        model.addAttribute("atendimentos", atendimentos.buscar(inicio, fim, profissionalId, pacienteId));
        model.addAttribute("voltar", UriComponentsBuilder.fromPath("/atendimentos")
                .queryParam("de", inicio).queryParam("ate", fim)
                .queryParamIfPresent("profissionalId", Optional.ofNullable(profissionalId))
                .queryParamIfPresent("pacienteId", Optional.ofNullable(pacienteId))
                .toUriString());
        model.addAttribute("profissionais", profissionais.findAllByOrderByNomeAsc());
        model.addAttribute("pacientes", pacientes.findAllByOrderByNomeAsc());
        return "atendimentos/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long profissionalId,
                       @RequestParam(required = false) Long pacienteId,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                       Model model) {
        AtendimentoForm form = new AtendimentoForm();
        form.setProfissionalId(profissionalId);
        form.setPacienteId(pacienteId);
        form.setData(data != null ? data : LocalDate.now());
        return formulario(form, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(AtendimentoForm.de(atendimentos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND))), model);
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("form") AtendimentoForm form, BindingResult erros,
                         Model model, RedirectAttributes redirect) {
        if (erros.hasErrors()) {
            return formulario(form, model);
        }
        try {
            int criados = service.salvar(form).size();
            redirect.addFlashAttribute("sucesso", criados > 1
                    ? criados + " atendimentos agendados (um por semana)."
                    : "Atendimento salvo.");
        } catch (AgendaException e) {
            erros.reject("agenda", e.getMessage());
            return formulario(form, model);
        }
        return "redirect:/agenda?profissionalId=" + form.getProfissionalId() + "&data=" + form.getData();
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusAtendimento status,
                                @RequestParam(required = false) String voltar) {
        service.alterarStatus(id, status);
        return "redirect:" + destinoSeguro(voltar);
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, @RequestParam(required = false) String voltar,
                          RedirectAttributes redirect) {
        atendimentos.deleteById(id);
        redirect.addFlashAttribute("sucesso", "Atendimento excluído.");
        return "redirect:" + destinoSeguro(voltar);
    }

    private String formulario(AtendimentoForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("profissionais", profissionais.findByAtivoTrueOrderByNomeAsc());
        model.addAttribute("pacientes", pacientes.findByAtivoTrueOrderByNomeAsc());
        return "atendimentos/form";
    }

    /** Só aceita caminhos internos da aplicação, para não redirecionar para outro site. */
    private static String destinoSeguro(String voltar) {
        return voltar != null && voltar.startsWith("/") && !voltar.startsWith("//") ? voltar : "/atendimentos";
    }
}
