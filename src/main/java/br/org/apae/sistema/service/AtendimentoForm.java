package br.org.apae.sistema.service;

import br.org.apae.sistema.model.Atendimento;
import br.org.apae.sistema.model.Especialidade;
import br.org.apae.sistema.model.StatusAtendimento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/** Dados do formulário de agendamento (permite repetir o horário toda semana). */
public class AtendimentoForm {

    private Long id;

    @NotNull(message = "Escolha o profissional")
    private Long profissionalId;

    @NotNull(message = "Escolha o paciente")
    private Long pacienteId;

    /** Se vazio, usa a especialidade do profissional. */
    private Especialidade tipo;

    @NotNull(message = "Informe a data")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;

    @NotNull(message = "Informe o horário de início")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotNull(message = "Informe o horário de término")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaFim;

    @Size(max = 60)
    private String sala;

    private StatusAtendimento status = StatusAtendimento.AGENDADO;

    @Size(max = 2000)
    private String observacoes;

    /** Quando preenchido (somente em novos agendamentos), cria o mesmo horário toda semana até esta data. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate repetirAte;

    public static AtendimentoForm de(Atendimento a) {
        AtendimentoForm f = new AtendimentoForm();
        f.id = a.getId();
        f.profissionalId = a.getProfissional().getId();
        f.pacienteId = a.getPaciente().getId();
        f.tipo = a.getTipo();
        f.data = a.getData();
        f.horaInicio = a.getHoraInicio();
        f.horaFim = a.getHoraFim();
        f.sala = a.getSala();
        f.status = a.getStatus();
        f.observacoes = a.getObservacoes();
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProfissionalId() { return profissionalId; }
    public void setProfissionalId(Long profissionalId) { this.profissionalId = profissionalId; }
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    public Especialidade getTipo() { return tipo; }
    public void setTipo(Especialidade tipo) { this.tipo = tipo; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }
    public StatusAtendimento getStatus() { return status; }
    public void setStatus(StatusAtendimento status) { this.status = status; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public LocalDate getRepetirAte() { return repetirAte; }
    public void setRepetirAte(LocalDate repetirAte) { this.repetirAte = repetirAte; }
}
