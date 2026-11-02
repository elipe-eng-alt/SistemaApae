package br.org.apae.sistema.repository;

import br.org.apae.sistema.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    List<Profissional> findAllByOrderByNomeAsc();

    List<Profissional> findByAtivoTrueOrderByNomeAsc();
}
