package co.com.bancolombia.model.exceptions;

/**
 * Se lanza cuando se intenta registrar un correo electrónico que ya pertenece a otro usuario.
 */
public class CorreoYaRegistradoException extends RuntimeException {

    public CorreoYaRegistradoException(String correoElectronico) {
        super("El correo electrónico " + correoElectronico + " ya está registrado");
    }
}
