package co.com.bancolombia.jpa.usuario;

import co.com.bancolombia.model.exceptions.CorreoYaRegistradoException;
import co.com.bancolombia.model.usuario.Usuario;
import co.com.bancolombia.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador: implementa el puerto UsuarioRepository del dominio usando Spring Data JPA.
 * Convierte entre el modelo de dominio (Usuario) y la entidad de base de datos (UsuarioEntity).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private static final String RESTRICCION_EMAIL_UNICO = "usuario_email_key";

    private final UsuarioJpaRepository repository;

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        log.debug("Guardando usuario en base de datos");
        try {
            UsuarioEntity guardado = repository.saveAndFlush(toEntity(usuario));
            log.info("Usuario guardado con id {}", guardado.getIdUsuario());
            return toDomain(guardado);
        } catch (DataIntegrityViolationException ex) {
            if (esCorreoDuplicado(ex)) {
                log.warn("La base de datos rechazó un correo duplicado");
                throw new CorreoYaRegistradoException(usuario.getCorreoElectronico());
            }
            throw ex;
        }
    }

    /**
     * Si dos peticiones con el mismo correo llegan casi al mismo tiempo, ambas pueden pasar
     * la validación del caso de uso; el UNIQUE de la base detiene a la segunda.
     */
    private static boolean esCorreoDuplicado(DataIntegrityViolationException ex) {
        String detalle = ex.getMostSpecificCause().getMessage();
        return detalle != null && detalle.contains(RESTRICCION_EMAIL_UNICO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCorreoElectronico(String correoElectronico) {
        return repository.existsByEmail(correoElectronico);
    }

    private static UsuarioEntity toEntity(Usuario usuario) {
        return UsuarioEntity.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombres())
                .apellido(usuario.getApellidos())
                .email(usuario.getCorreoElectronico())
                .telefono(usuario.getTelefono())
                .idRol(usuario.getIdRol())
                .salarioBase(usuario.getSalarioBase())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .direccion(usuario.getDireccion())
                .build();
    }

    private static Usuario toDomain(UsuarioEntity entity) {
        return Usuario.builder()
                .idUsuario(entity.getIdUsuario())
                .nombres(entity.getNombre())
                .apellidos(entity.getApellido())
                .correoElectronico(entity.getEmail())
                .telefono(entity.getTelefono())
                .idRol(entity.getIdRol())
                .salarioBase(entity.getSalarioBase())
                .fechaNacimiento(entity.getFechaNacimiento())
                .direccion(entity.getDireccion())
                .build();
    }
}
