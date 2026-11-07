package co.com.bancolombia.api.usuario;

import co.com.bancolombia.api.usuario.dto.RegistrarUsuarioRequest;
import co.com.bancolombia.api.usuario.dto.UsuarioResponse;
import co.com.bancolombia.model.usuario.Usuario;
import co.com.bancolombia.usecase.registrarusuario.RegistrarUsuarioUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entry point HTTP de la HU-01: traduce el JSON al modelo de dominio y llama al caso de uso.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistrarUsuarioRequest request) {
        log.info("Petición recibida: registrar usuario");

        Usuario registrado = registrarUsuarioUseCase.registrar(request.toDomain());

        log.info("Usuario registrado exitosamente con id {}", registrado.getIdUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.fromDomain(registrado));
    }
}
