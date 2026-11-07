package co.com.bancolombia.api.exception;

import java.time.OffsetDateTime;

/**
 * Formato único de error que recibe el cliente de la API.
 */
public record RespuestaError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta
) {
}
