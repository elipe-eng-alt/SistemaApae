package br.org.apae.sistema.repository;

import br.org.apae.sistema.model.Perfil;
import br.org.apae.sistema.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLoginIgnoreCase(String login);

    Optional<Usuario> findByProfissionalId(Long profissionalId);

    List<Usuario> findAllByOrderByLoginAsc();

    List<Usuario> findByProfissionalIsNotNull();

    long countByPerfilAndAtivoTrue(Perfil perfil);
}
