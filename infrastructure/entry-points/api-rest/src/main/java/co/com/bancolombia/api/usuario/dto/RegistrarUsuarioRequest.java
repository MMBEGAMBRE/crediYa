package co.com.bancolombia.api.usuario.dto;

import co.com.bancolombia.model.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JSON de entrada de POST /api/v1/usuarios.
 * Los nombres del JSON siguen la HU (snake_case); las validaciones son un primer filtro
 * antes de llegar al caso de uso, que vuelve a validar las reglas de negocio.
 */
public record RegistrarUsuarioRequest(

        @NotBlank(message = "nombres es obligatorio")
        @Size(max = 100, message = "nombres no puede superar 100 caracteres")
        String nombres,

        @NotBlank(message = "apellidos es obligatorio")
        @Size(max = 100, message = "apellidos no puede superar 100 caracteres")
        String apellidos,

        @JsonProperty("fecha_nacimiento")
        @Past(message = "fecha_nacimiento debe ser una fecha pasada")
        LocalDate fechaNacimiento,

        @Size(max = 255, message = "direccion no puede superar 255 caracteres")
        String direccion,

        @Size(max = 20, message = "telefono no puede superar 20 caracteres")
        String telefono,

        @JsonProperty("correo_electronico")
        @NotBlank(message = "correo_electronico es obligatorio")
        @Email(message = "correo_electronico no tiene un formato válido")
        @Size(max = 150, message = "correo_electronico no puede superar 150 caracteres")
        String correoElectronico,

        @JsonProperty("salario_base")
        @NotNull(message = "salario_base es obligatorio")
        @DecimalMin(value = "0", message = "salario_base debe estar entre 0 y 15000000")
        @DecimalMax(value = "15000000", message = "salario_base debe estar entre 0 y 15000000")
        @Digits(integer = 13, fraction = 2, message = "salario_base admite máximo 2 decimales")
        BigDecimal salarioBase
) {

    public Usuario toDomain() {
        return Usuario.builder()
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .direccion(direccion)
                .telefono(telefono)
                .correoElectronico(correoElectronico)
                .salarioBase(salarioBase)
                .build();
    }
}
