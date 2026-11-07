package co.com.bancolombia.usecase.registrarusuario;

import co.com.bancolombia.model.exceptions.CorreoYaRegistradoException;
import co.com.bancolombia.model.exceptions.DatosInvalidosException;
import co.com.bancolombia.model.exceptions.RolNoEncontradoException;
import co.com.bancolombia.model.rol.gateways.RolRepository;
import co.com.bancolombia.model.usuario.Usuario;
import co.com.bancolombia.model.usuario.gateways.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioUseCaseTest {

    private static final Long ID_ROL_CLIENTE = 3L;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RegistrarUsuarioUseCase useCase;

    private Usuario usuarioValido;

    @BeforeEach
    void setUp() {
        usuarioValido = Usuario.builder()
                .nombres("Ana Maria")
                .apellidos("Perez Gomez")
                .fechaNacimiento(LocalDate.of(1995, 4, 12))
                .direccion("Calle 10 # 20-30")
                .telefono("3001234567")
                .correoElectronico("ana.perez@crediya.com")
                .salarioBase(new BigDecimal("3500000"))
                .build();
    }

    // ---------- Caso exitoso ----------

    @Test
    void registraUsuarioConRolClienteYDevuelveElGuardado() {
        when(usuarioRepository.existePorCorreoElectronico("ana.perez@crediya.com")).thenReturn(false);
        when(rolRepository.buscarIdPorNombre("CLIENTE")).thenReturn(Optional.of(ID_ROL_CLIENTE));
        when(usuarioRepository.guardar(any(Usuario.class)))
                .thenAnswer(inv -> ((Usuario) inv.getArgument(0)).toBuilder().idUsuario(1L).build());

        Usuario resultado = useCase.registrar(usuarioValido);

        assertEquals(1L, resultado.getIdUsuario());
        assertEquals(ID_ROL_CLIENTE, resultado.getIdRol());
        assertEquals("Ana Maria", resultado.getNombres());
    }

    @Test
    void normalizaCorreoYEspaciosAntesDeGuardar() {
        Usuario conEspacios = usuarioValido.toBuilder()
                .nombres("  Ana Maria  ")
                .correoElectronico("  Ana.Perez@CrediYa.COM ")
                .idUsuario(99L)
                .build();
        when(usuarioRepository.existePorCorreoElectronico("ana.perez@crediya.com")).thenReturn(false);
        when(rolRepository.buscarIdPorNombre("CLIENTE")).thenReturn(Optional.of(ID_ROL_CLIENTE));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.registrar(conEspacios);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("ana.perez@crediya.com", guardado.getCorreoElectronico());
        assertEquals("Ana Maria", guardado.getNombres());
        assertNull(guardado.getIdUsuario(), "El id siempre lo genera la base de datos");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "15000000", "0.01", "14999999.99"})
    void aceptaSalariosDentroDelRangoIncluyendoLimites(String salario) {
        when(usuarioRepository.existePorCorreoElectronico(any())).thenReturn(false);
        when(rolRepository.buscarIdPorNombre("CLIENTE")).thenReturn(Optional.of(ID_ROL_CLIENTE));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = useCase.registrar(usuarioValido.toBuilder().salarioBase(new BigDecimal(salario)).build());

        assertEquals(new BigDecimal(salario), resultado.getSalarioBase());
    }

    // ---------- Correo duplicado ----------

    @Test
    void lanzaCorreoYaRegistradoSiElCorreoExiste() {
        when(usuarioRepository.existePorCorreoElectronico("ana.perez@crediya.com")).thenReturn(true);

        assertThrows(CorreoYaRegistradoException.class, () -> useCase.registrar(usuarioValido));

        verify(usuarioRepository, never()).guardar(any());
        verifyNoInteractions(rolRepository);
    }

    // ---------- Salario fuera de rango ----------

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-1", "15000000.01", "20000000"})
    void rechazaSalarioFueraDeRango(String salario) {
        Usuario usuario = usuarioValido.toBuilder().salarioBase(new BigDecimal(salario)).build();

        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class, () -> useCase.registrar(usuario));

        assertTrue(ex.getMessage().contains("salario_base debe estar entre 0 y 15000000"));
        verifyNoInteractions(usuarioRepository, rolRepository);
    }

    // ---------- Campos obligatorios y formato ----------

    @Test
    void rechazaCamposObligatoriosVaciosYReportaTodosLosErrores() {
        Usuario vacio = Usuario.builder()
                .nombres("")
                .apellidos("   ")
                .correoElectronico(null)
                .salarioBase(null)
                .build();

        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class, () -> useCase.registrar(vacio));

        assertTrue(ex.getMessage().contains("nombres es obligatorio"));
        assertTrue(ex.getMessage().contains("apellidos es obligatorio"));
        assertTrue(ex.getMessage().contains("correo_electronico es obligatorio"));
        assertTrue(ex.getMessage().contains("salario_base es obligatorio"));
        verifyNoInteractions(usuarioRepository, rolRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"no-es-correo", "ana@", "@crediya.com", "ana@crediya", "ana perez@crediya.com"})
    void rechazaCorreoConFormatoInvalido(String correo) {
        Usuario usuario = usuarioValido.toBuilder().correoElectronico(correo).build();

        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class, () -> useCase.registrar(usuario));

        assertTrue(ex.getMessage().contains("correo_electronico no tiene un formato válido"));
        verifyNoInteractions(usuarioRepository, rolRepository);
    }

    @Test
    void rechazaUsuarioNulo() {
        assertThrows(DatosInvalidosException.class, () -> useCase.registrar(null));
        verifyNoInteractions(usuarioRepository, rolRepository);
    }

    // ---------- Rol inexistente ----------

    @Test
    void lanzaRolNoEncontradoSiNoExisteElRolCliente() {
        when(usuarioRepository.existePorCorreoElectronico(any())).thenReturn(false);
        when(rolRepository.buscarIdPorNombre("CLIENTE")).thenReturn(Optional.empty());

        assertThrows(RolNoEncontradoException.class, () -> useCase.registrar(usuarioValido));

        verify(usuarioRepository, never()).guardar(any());
    }
}
