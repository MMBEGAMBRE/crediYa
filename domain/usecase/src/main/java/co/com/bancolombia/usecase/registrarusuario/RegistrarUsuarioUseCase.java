package co.com.bancolombia.usecase.registrarusuario;

import co.com.bancolombia.model.exceptions.CorreoYaRegistradoException;
import co.com.bancolombia.model.exceptions.DatosInvalidosException;
import co.com.bancolombia.model.exceptions.RolNoEncontradoException;
import co.com.bancolombia.model.rol.gateways.RolRepository;
import co.com.bancolombia.model.usuario.Usuario;
import co.com.bancolombia.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Caso de uso HU-01: registrar un nuevo usuario (solicitante).
 * Aplica las reglas de negocio y usa los gateways para consultar y guardar.
 */
@RequiredArgsConstructor
public class RegistrarUsuarioUseCase {

    static final String ROL_POR_DEFECTO = "CLIENTE";
    static final BigDecimal SALARIO_MINIMO = BigDecimal.ZERO;
    static final BigDecimal SALARIO_MAXIMO = new BigDecimal("15000000");
    private static final Pattern FORMATO_CORREO =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public Usuario registrar(Usuario usuario) {
        if (usuario == null) {
            throw new DatosInvalidosException("Los datos del usuario son obligatorios");
        }

        Usuario normalizado = normalizar(usuario);
        validarDatos(normalizado);

        if (usuarioRepository.existePorCorreoElectronico(normalizado.getCorreoElectronico())) {
            throw new CorreoYaRegistradoException(normalizado.getCorreoElectronico());
        }

        Long idRolCliente = rolRepository.buscarIdPorNombre(ROL_POR_DEFECTO)
                .orElseThrow(() -> new RolNoEncontradoException(ROL_POR_DEFECTO));

        Usuario aGuardar = normalizado.toBuilder()
                .idUsuario(null)
                .idRol(idRolCliente)
                .build();

        return usuarioRepository.guardar(aGuardar);
    }

    /**
     * Quita espacios sobrantes y pasa el correo a minúsculas,
     * para que "Ana@Mail.com " y "ana@mail.com" se consideren el mismo correo.
     */
    private Usuario normalizar(Usuario usuario) {
        String correo = recortar(usuario.getCorreoElectronico());
        return usuario.toBuilder()
                .nombres(recortar(usuario.getNombres()))
                .apellidos(recortar(usuario.getApellidos()))
                .direccion(recortar(usuario.getDireccion()))
                .telefono(recortar(usuario.getTelefono()))
                .correoElectronico(correo == null ? null : correo.toLowerCase(Locale.ROOT))
                .build();
    }

    private void validarDatos(Usuario usuario) {
        List<String> errores = new ArrayList<>();

        if (estaVacio(usuario.getNombres())) {
            errores.add("nombres es obligatorio");
        }
        if (estaVacio(usuario.getApellidos())) {
            errores.add("apellidos es obligatorio");
        }

        if (estaVacio(usuario.getCorreoElectronico())) {
            errores.add("correo_electronico es obligatorio");
        } else if (!FORMATO_CORREO.matcher(usuario.getCorreoElectronico()).matches()) {
            errores.add("correo_electronico no tiene un formato válido");
        }

        BigDecimal salario = usuario.getSalarioBase();
        if (salario == null) {
            errores.add("salario_base es obligatorio");
        } else if (salario.compareTo(SALARIO_MINIMO) < 0 || salario.compareTo(SALARIO_MAXIMO) > 0) {
            errores.add("salario_base debe estar entre 0 y 15000000");
        }

        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(String.join("; ", errores));
        }
    }

    private static String recortar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
