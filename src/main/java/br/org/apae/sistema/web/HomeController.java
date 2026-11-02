package br.org.apae.sistema.web;

import br.org.apae.sistema.repository.AtendimentoRepository;
import br.org.apae.sistema.repository.PacienteRepository;
import br.org.apae.sistema.repository.ProfissionalRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class HomeController {

    private final AtendimentoRepository atendimentos;
    private final PacienteRepository pacientes;
    private final ProfissionalRepository profissionais;

    public HomeController(AtendimentoRepository atendimentos, PacienteRepository pacientes,
                          ProfissionalRepository profissionais) {
        this.atendimentos = atendimentos;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("atendimentosHoje", atendimentos.findByDataOrderByHoraInicioAsc(LocalDate.now()));
        model.addAttribute("totalPacientes", pacientes.findByAtivoTrueOrderByNomeAsc().size());
        model.addAttribute("profissionais", profissionais.findByAtivoTrueOrderByNomeAsc());
        return "index";
    }
}
