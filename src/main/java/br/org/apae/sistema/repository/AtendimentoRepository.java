package br.org.apae.sistema.repository;

import br.org.apae.sistema.model.Atendimento;
import br.org.apae.sistema.model.StatusAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    List<Atendimento> findByProfissionalIdAndDataOrderByHoraInicioAsc(Long profissionalId, LocalDate data);

    List<Atendimento> findByDataOrderByHoraInicioAsc(LocalDate data);

    @Query("""
            select a from Atendimento a
            where a.data between :inicio and :fim
              and (:profissionalId is null or a.profissional.id = :profissionalId)
              and (:pacienteId is null or a.paciente.id = :pacienteId)
            order by a.data, a.horaInicio, a.profissional.nome
            """)
    List<Atendimento> buscar(@Param("inicio") LocalDate inicio,
                             @Param("fim") LocalDate fim,
                             @Param("profissionalId") Long profissionalId,
                             @Param("pacienteId") Long pacienteId);

    /** Atendimentos não cancelados do mesmo profissional ou paciente que se sobrepõem ao horário informado. */
    @Query("""
            select a from Atendimento a
            where a.data = :data
              and a.status <> :cancelado
              and a.horaInicio < :fim and a.horaFim > :inicio
              and (a.profissional.id = :profissionalId or a.paciente.id = :pacienteId)
              and (:ignorarId is null or a.id <> :ignorarId)
            """)
    List<Atendimento> conflitos(@Param("data") LocalDate data,
                                @Param("inicio") LocalTime inicio,
                                @Param("fim") LocalTime fim,
                                @Param("profissionalId") Long profissionalId,
                                @Param("pacienteId") Long pacienteId,
                                @Param("ignorarId") Long ignorarId,
                                @Param("cancelado") StatusAtendimento cancelado);

    boolean existsByPacienteId(Long pacienteId);

    boolean existsByProfissionalId(Long profissionalId);
}
