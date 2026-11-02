package br.org.apae.sistema;

import br.org.apae.sistema.model.Especialidade;
import br.org.apae.sistema.model.Paciente;
import br.org.apae.sistema.model.Profissional;
import br.org.apae.sistema.repository.AtendimentoRepository;
import br.org.apae.sistema.repository.PacienteRepository;
import br.org.apae.sistema.repository.ProfissionalRepository;
import br.org.apae.sistema.service.AgendaException;
import br.org.apae.sistema.service.AtendimentoForm;
import br.org.apae.sistema.service.AtendimentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1")
class AtendimentoServiceTest {

    @Autowired AtendimentoService service;
    @Autowired AtendimentoRepository atendimentos;
    @Autowired ProfissionalRepository profissionais;
    @Autowired PacienteRepository pacientes;

    Profissional fisio;
    Paciente joao;
    Paciente maria;
    final LocalDate segunda = LocalDate.of(2030, 1, 7);

    @BeforeEach
    void preparar() {
        atendimentos.deleteAll();
        profissionais.deleteAll();
        pacientes.deleteAll();
        fisio = new Profissional();
        fisio.setNome("Ana Fisio");
        fisio.setEspecialidade(Especialidade.FISIOTERAPIA);
        profissionais.save(fisio);
        joao = paciente("João");
        maria = paciente("Maria");
    }

    @Test
    void agendaRepetindoTodaSemanaEApareceNaAgendaDoDia() {
        var criados = service.salvar(form(joao, segunda, "08:00", "08:40", segunda.plusWeeks(3)));

        assertThat(criados).hasSize(4);
        assertThat(criados.getFirst().getTipo()).isEqualTo(Especialidade.FISIOTERAPIA);
        assertThat(atendimentos.findByProfissionalIdAndDataOrderByHoraInicioAsc(fisio.getId(), segunda.plusWeeks(2)))
                .extracting(a -> a.getPaciente().getNome()).containsExactly("João");
    }

    @Test
    void recusaHorarioSobrepostoDoMesmoProfissional() {
        service.salvar(form(joao, segunda, "08:00", "08:40", null));

        assertThatThrownBy(() -> service.salvar(form(maria, segunda, "08:30", "09:10", null)))
                .isInstanceOf(AgendaException.class)
                .hasMessageContaining("Conflito");
        // Horário encostado (termina 08:40, começa 08:40) é permitido.
        assertThat(service.salvar(form(maria, segunda, "08:40", "09:20", null))).hasSize(1);
    }

    @Test
    void recusaTerminoAntesDoInicio() {
        assertThatThrownBy(() -> service.salvar(form(joao, segunda, "09:00", "08:00", null)))
                .isInstanceOf(AgendaException.class);
    }

    private Paciente paciente(String nome) {
        Paciente p = new Paciente();
        p.setNome(nome);
        return pacientes.save(p);
    }

    private AtendimentoForm form(Paciente p, LocalDate data, String inicio, String fim, LocalDate repetirAte) {
        AtendimentoForm f = new AtendimentoForm();
        f.setProfissionalId(fisio.getId());
        f.setPacienteId(p.getId());
        f.setData(data);
        f.setHoraInicio(LocalTime.parse(inicio));
        f.setHoraFim(LocalTime.parse(fim));
        f.setRepetirAte(repetirAte);
        return f;
    }
}
