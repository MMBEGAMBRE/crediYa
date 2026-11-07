package co.com.bancolombia.api.usuario;

import co.com.bancolombia.api.TestConfig;
import co.com.bancolombia.model.exceptions.CorreoYaRegistradoException;
import co.com.bancolombia.model.exceptions.DatosInvalidosException;
import co.com.bancolombia.model.usuario.Usuario;
import co.com.bancolombia.usecase.registrarusuario.RegistrarUsuarioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Prueba la capa HTTP: códigos de respuesta, formato del JSON y manejo de errores.
 * El caso de uso se reemplaza por un mock (@MockitoBean): aquí no hay base de datos.
 */
@SpringBootTest(classes = TestConfig.class)
class UsuarioControllerTest {

    private static final String URL = "/api/v1/usuarios";

    private static final String JSON_VALIDO = """
            {
              "nombres": "Ana Maria",
              "apellidos": "Perez Gomez",
              "fecha_nacimiento": "1995-04-12",
              "direccion": "Calle 10 # 20-30",
              "telefono": "3001234567",
              "correo_electronico": "ana.perez@crediya.com",
              "salario_base": 3500000
            }
            """;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToApplicationContext(context).build();
    }

    private RestTestClient.ResponseSpec post(String json) {
        return client.post()
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .exchange();
    }

    @Test
    void responde201ConElUsuarioRegistrado() {
        when(registrarUsuarioUseCase.registrar(any(Usuario.class))).thenReturn(Usuario.builder()
                .idUsuario(1L)
                .nombres("Ana Maria")
                .apellidos("Perez Gomez")
                .fechaNacimiento(LocalDate.of(1995, 4, 12))
                .correoElectronico("ana.perez@crediya.com")
                .salarioBase(new BigDecimal("3500000"))
                .idRol(3L)
                .build());

        post(JSON_VALIDO)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id_usuario").isEqualTo(1)
                .jsonPath("$.correo_electronico").isEqualTo("ana.perez@crediya.com")
                .jsonPath("$.fecha_nacimiento").isEqualTo("1995-04-12")
                .jsonPath("$.id_rol").isEqualTo(3);
    }

    @Test
    void responde400SiFaltanCamposObligatoriosSinLlamarAlCasoDeUso() {
        post("""
                { "nombres": "", "correo_electronico": "no-es-correo" }
                """)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.mensaje").value(String.class, mensaje -> assertTrue(mensaje.contains("nombres es obligatorio")))
                .jsonPath("$.mensaje").value(String.class, mensaje -> assertTrue(mensaje.contains("apellidos es obligatorio")))
                .jsonPath("$.mensaje").value(String.class, mensaje -> assertTrue(mensaje.contains("salario_base es obligatorio")))
                .jsonPath("$.mensaje").value(String.class, mensaje -> assertTrue(mensaje.contains("correo_electronico no tiene un formato válido")));

        verifyNoInteractions(registrarUsuarioUseCase);
    }

    @Test
    void responde400SiElSalarioEstaFueraDeRango() {
        post(JSON_VALIDO.replace("3500000", "15000001"))
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("salario_base debe estar entre 0 y 15000000");

        verifyNoInteractions(registrarUsuarioUseCase);
    }

    @Test
    void responde400SiElSalarioNoEsNumerico() {
        post(JSON_VALIDO.replace("3500000", "\"abc\""))
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.mensaje").value(String.class, mensaje -> assertTrue(mensaje.contains("salario_base debe ser numérico")));
    }

    @Test
    void responde400SiElCasoDeUsoRechazaLosDatos() {
        when(registrarUsuarioUseCase.registrar(any(Usuario.class)))
                .thenThrow(new DatosInvalidosException("correo_electronico no tiene un formato válido"));

        post(JSON_VALIDO)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("correo_electronico no tiene un formato válido");
    }

    @Test
    void responde409SiElCorreoYaEstaRegistrado() {
        when(registrarUsuarioUseCase.registrar(any(Usuario.class)))
                .thenThrow(new CorreoYaRegistradoException("ana.perez@crediya.com"));

        post(JSON_VALIDO)
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Conflict")
                .jsonPath("$.mensaje").isEqualTo("El correo electrónico ana.perez@crediya.com ya está registrado");
    }

    @Test
    void responde500ConMensajeGenericoSinExponerDetallesInternos() {
        when(registrarUsuarioUseCase.registrar(any(Usuario.class)))
                .thenThrow(new IllegalStateException("detalle interno secreto"));

        post(JSON_VALIDO)
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.mensaje").isEqualTo("Ocurrió un error inesperado. Intenta de nuevo más tarde.")
                .jsonPath("$.mensaje").value(String.class, mensaje -> assertFalse(mensaje.contains("secreto")));
    }

    @Test
    void responde405SiSeUsaUnMetodoNoPermitido() {
        client.get().uri(URL).exchange()
                .expectStatus().isEqualTo(405)
                .expectBody()
                .jsonPath("$.status").isEqualTo(405);
    }
}
