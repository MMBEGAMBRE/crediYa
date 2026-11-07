package co.com.bancolombia.model.exceptions;

/**
 * Se lanza cuando los datos de entrada no cumplen las reglas de negocio
 * (campos obligatorios, formato de correo, rango de salario).
 */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
