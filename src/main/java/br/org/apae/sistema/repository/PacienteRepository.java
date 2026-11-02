package br.org.apae.sistema.repository;

import br.org.apae.sistema.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findAllByOrderByNomeAsc();

    List<Paciente> findByAtivoTrueOrderByNomeAsc();

    List<Paciente> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
}
