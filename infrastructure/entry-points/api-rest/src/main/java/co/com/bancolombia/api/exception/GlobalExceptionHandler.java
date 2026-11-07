package co.com.bancolombia.api.exception;

import co.com.bancolombia.model.exceptions.CorreoYaRegistradoException;
import co.com.bancolombia.model.exceptions.DatosInvalidosException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Convierte cualquier excepción en un JSON claro (RespuestaError).
 * El cliente nunca recibe stack traces ni mensajes internos.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MENSAJE_GENERICO = "Ocurrió un error inesperado. Intenta de nuevo más tarde.";

    /** 400: falló Bean Validation en el JSON de entrada (@NotBlank, @Email, @Size...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacion(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .sorted()
                .collect(Collectors.joining("; "));
        log.warn("Validación fallida en {}: {}", request.getRequestURI(), mensaje);
        return construir(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    /** 400: el JSON está mal formado o un campo tiene el tipo incorrecto (ej. salario_base = "abc"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> manejarJsonInvalido(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        log.warn("JSON inválido en {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es válido. Revisa el formato: salario_base debe ser numérico "
                        + "y fecha_nacimiento debe tener el formato yyyy-MM-dd",
                request);
    }

    /** 400: falló una regla de negocio validada en el caso de uso. */
    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<RespuestaError> manejarDatosInvalidos(DatosInvalidosException ex,
                                                                HttpServletRequest request) {
        log.warn("Datos inválidos en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** 409: el correo ya pertenece a otro usuario. */
    @ExceptionHandler(CorreoYaRegistradoException.class)
    public ResponseEntity<RespuestaError> manejarCorreoDuplicado(CorreoYaRegistradoException ex,
                                                                 HttpServletRequest request) {
        log.warn("Conflicto en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Cualquier otra excepción.
     * Si es un error HTTP estándar de Spring (404 ruta no existe, 405 método no permitido,
     * 415 tipo de contenido no soportado) se respeta su código; lo demás es 500 con mensaje genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarInesperado(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse errorSpring) {
            HttpStatusCode status = errorSpring.getStatusCode();
            log.warn("Error HTTP {} en {}: {}", status.value(), request.getRequestURI(), ex.getMessage());
            String mensaje = errorSpring.getBody().getDetail() != null
                    ? errorSpring.getBody().getDetail()
                    : "La petición no pudo ser procesada";
            return construir(status, mensaje, request);
        }

        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, MENSAJE_GENERICO, request);
    }

    private ResponseEntity<RespuestaError> construir(HttpStatusCode status, String mensaje,
                                                     HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.resolve(status.value());
        String error = httpStatus != null ? httpStatus.getReasonPhrase() : "Error";
        RespuestaError cuerpo = new RespuestaError(
                OffsetDateTime.now(), status.value(), error, mensaje, request.getRequestURI());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
