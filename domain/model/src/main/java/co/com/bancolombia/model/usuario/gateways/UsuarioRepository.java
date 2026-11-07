package co.com.bancolombia.model.usuario.gateways;

import co.com.bancolombia.model.usuario.Usuario;

/**
 * Puerto de salida: lo que el dominio necesita para persistir usuarios.
 * La implementación vive en infraestructura (adaptador JPA).
 */
public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    boolean existePorCorreoElectronico(String correoElectronico);
}
