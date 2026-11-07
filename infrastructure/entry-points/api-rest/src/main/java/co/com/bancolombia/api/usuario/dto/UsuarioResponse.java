package co.com.bancolombia.api.usuario.dto;

import co.com.bancolombia.model.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JSON de salida con el usuario registrado.
 */
public record UsuarioResponse(
        @JsonProperty("id_usuario") Long idUsuario,
        String nombres,
        String apellidos,
        @JsonProperty("fecha_nacimiento") LocalDate fechaNacimiento,
        String direccion,
        String telefono,
        @JsonProperty("correo_electronico") String correoElectronico,
        @JsonProperty("salario_base") BigDecimal salarioBase,
        @JsonProperty("id_rol") Long idRol
) {

    public static UsuarioResponse fromDomain(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getFechaNacimiento(),
                usuario.getDireccion(),
                usuario.getTelefono(),
                usuario.getCorreoElectronico(),
                usuario.getSalarioBase(),
                usuario.getIdRol()
        );
    }
}
