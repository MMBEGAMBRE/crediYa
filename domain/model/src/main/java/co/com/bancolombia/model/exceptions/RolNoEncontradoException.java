package co.com.bancolombia.model.exceptions;

/**
 * Se lanza cuando un rol requerido por el negocio (por ejemplo CLIENTE) no existe en el sistema.
 * Indica un problema de configuración de datos, no un error del usuario que llama a la API.
 */
public class RolNoEncontradoException extends RuntimeException {

    public RolNoEncontradoException(String nombreRol) {
        super("No existe el rol " + nombreRol);
    }
}
