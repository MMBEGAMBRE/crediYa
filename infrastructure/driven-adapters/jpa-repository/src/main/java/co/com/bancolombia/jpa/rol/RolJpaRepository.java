package co.com.bancolombia.jpa.rol;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data para la tabla rol.
 * findByNombre se traduce a: SELECT ... FROM rol WHERE nombre = ?
 */
public interface RolJpaRepository extends JpaRepository<RolEntity, Long> {

    Optional<RolEntity> findByNombre(String nombre);
}
