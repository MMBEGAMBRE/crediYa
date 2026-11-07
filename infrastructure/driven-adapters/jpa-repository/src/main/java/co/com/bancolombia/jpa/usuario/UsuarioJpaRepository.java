package co.com.bancolombia.jpa.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data: Spring genera la implementación en tiempo de ejecución.
 * existsByEmail se traduce a: SELECT ... FROM usuario WHERE email = ?
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    boolean existsByEmail(String email);
}
