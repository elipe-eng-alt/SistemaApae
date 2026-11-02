package br.org.apae.sistema.service;

import br.org.apae.sistema.model.Atendimento;
import br.org.apae.sistema.model.Paciente;
import br.org.apae.sistema.model.Profissional;
import br.org.apae.sistema.model.StatusAtendimento;
import br.org.apae.sistema.repository.AtendimentoRepository;
import br.org.apae.sistema.repository.PacienteRepository;
import br.org.apae.sistema.repository.ProfissionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AtendimentoService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int MAX_SEMANAS_REPETICAO = 53;

    private final AtendimentoRepository atendimentos;
    private final ProfissionalRepository profissionais;
    private final PacienteRepository pacientes;

    public AtendimentoService(AtendimentoRepository atendimentos,
                              ProfissionalRepository profissionais,
                              PacienteRepository pacientes) {
        this.atendimentos = atendimentos;
        this.profissionais = profissionais;
        this.pacientes = pacientes;
    }

    /**
     * Cria ou altera um atendimento. Em novos agendamentos com {@code repetirAte}
     * cria um atendimento por semana. Nada é salvo se houver qualquer conflito.
     */
    public List<Atendimento> salvar(AtendimentoForm form) {
        if (!form.getHoraFim().isAfter(form.getHoraInicio())) {
            throw new AgendaException("O horário de término deve ser depois do horário de início.");
        }
        Profissional profissional = profissionais.findById(form.getProfissionalId())
                .orElseThrow(() -> new AgendaException("Profissional não encontrado."));
        Paciente paciente = pacientes.findById(form.getPacienteId())
                .orElseThrow(() -> new AgendaException("Paciente não encontrado."));

        List<LocalDate> datas = datasDoAgendamento(form);
        List<Atendimento> resultado = new ArrayList<>();
        List<String> conflitos = new ArrayList<>();

        for (LocalDate data : datas) {
            Atendimento a = form.getId() == null
                    ? new Atendimento()
                    : atendimentos.findById(form.getId())
                        .orElseThrow(() -> new AgendaException("Atendimento não encontrado."));
            a.setProfissional(profissional);
            a.setPaciente(paciente);
            a.setTipo(form.getTipo() != null ? form.getTipo() : profissional.getEspecialidade());
            a.setData(data);
            a.setHoraInicio(form.getHoraInicio());
            a.setHoraFim(form.getHoraFim());
            a.setSala(form.getSala());
            a.setStatus(form.getStatus() != null ? form.getStatus() : StatusAtendimento.AGENDADO);
            a.setObservacoes(form.getObservacoes());

            if (a.getStatus() != StatusAtendimento.CANCELADO) {
                atendimentos.conflitos(data, a.getHoraInicio(), a.getHoraFim(),
                                profissional.getId(), paciente.getId(), form.getId(), StatusAtendimento.CANCELADO)
                        .forEach(c -> conflitos.add(descreverConflito(c, profissional, paciente)));
            }
            resultado.add(a);
        }

        if (!conflitos.isEmpty()) {
            String lista = String.join("; ", conflitos.stream().limit(5).toList());
            String resto = conflitos.size() > 5 ? " (e mais " + (conflitos.size() - 5) + ")" : "";
            throw new AgendaException("Conflito de horário: " + lista + resto + ".");
        }
        return atendimentos.saveAll(resultado);
    }

    public void alterarStatus(Long id, StatusAtendimento status) {
        Atendimento a = atendimentos.findById(id)
                .orElseThrow(() -> new AgendaException("Atendimento não encontrado."));
        a.setStatus(status);
    }

    private List<LocalDate> datasDoAgendamento(AtendimentoForm form) {
        LocalDate inicio = form.getData();
        LocalDate fim = form.getRepetirAte();
        if (form.getId() != null || fim == null) {
            return List.of(inicio);
        }
        if (fim.isBefore(inicio)) {
            throw new AgendaException("A data final da repetição deve ser igual ou posterior à data do atendimento.");
        }
        List<LocalDate> datas = new ArrayList<>();
        for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusWeeks(1)) {
            datas.add(d);
        }
        if (datas.size() > MAX_SEMANAS_REPETICAO) {
            throw new AgendaException("A repetição pode ter no máximo " + MAX_SEMANAS_REPETICAO + " semanas.");
        }
        return datas;
    }

    private String descreverConflito(Atendimento c, Profissional profissional, Paciente paciente) {
        String quem = c.getProfissional().getId().equals(profissional.getId())
                ? profissional.getNome() + " já atende " + c.getPaciente().getNome()
                : paciente.getNome() + " já tem atendimento com " + c.getProfissional().getNome();
        return quem + " em " + c.getData().format(DATA) + " das " + c.getHoraInicio() + " às " + c.getHoraFim();
    }
}
