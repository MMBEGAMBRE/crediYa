package co.com.bancolombia.jpa.rol;

import co.com.bancolombia.model.rol.gateways.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador: implementa el puerto RolRepository del dominio usando Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class RolRepositoryAdapter implements RolRepository {

    private final RolJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> buscarIdPorNombre(String nombre) {
        return repository.findByNombre(nombre).map(RolEntity::getIdRol);
    }
}
