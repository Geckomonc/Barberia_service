package com.barberia.modules.modulo_usuarios.services;

import com.barberia.modules.modulo_usuarios.models.dtos.RegistroDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UpdatePerfilDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link UsuarioService} siguiendo el patrón AAA
 * (Arrange - Act - Assert).
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioCliente;
    private Usuario usuarioBarbero;

    @BeforeEach
    void setUp() {
        usuarioCliente = Usuario.builder()
                .numeroDocumento("100")
                .numeroCelular("3001234567")
                .email("cliente@mail.com")
                .nombrePersona("Cliente Prueba")
                .contrasenaHasheada("hashed")
                .idRol(3)
                .idEstado(1)
                .fechaRegistro(LocalDateTime.now())
                .build();

        usuarioBarbero = Usuario.builder()
                .numeroDocumento("200")
                .numeroCelular("3007654321")
                .email("barbero@mail.com")
                .nombrePersona("Barbero Prueba")
                .contrasenaHasheada("hashed")
                .idRol(2)
                .idEstado(1)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    // ============================================================
    // registrarPersona
    // ============================================================
    @Nested
    @DisplayName("registrarPersona")
    class RegistrarPersona {

        @Test
        @DisplayName("retorna UsuarioDTO cuando los datos son válidos")
        void registrarPersona_conDatosValidos_retornaUsuarioDTO() {
            // Arrange
            RegistroDTO dto = RegistroDTO.builder()
                    .numeroDocumento("100")
                    .numeroCelular("3001234567")
                    .email("nuevo@mail.com")
                    .nombrePersona("Nuevo Usuario")
                    .contraseña("clave123")
                    .confirmarContraseña("clave123")
                    .build();
            when(usuarioRepository.existsByNumeroDocumento("100")).thenReturn(false);
            when(usuarioRepository.existsByEmail("nuevo@mail.com")).thenReturn(false);
            when(passwordEncoder.encode("clave123")).thenReturn("hashedClave");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UsuarioDTO resultado = usuarioService.registrarPersona(dto);

            // Assert
            assertNotNull(resultado);
            assertEquals("100", resultado.getNumeroDocumento());
            assertEquals("nuevo@mail.com", resultado.getEmail());
            assertEquals(3, resultado.getIdRol());
            assertEquals(1, resultado.getIdEstado());
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el documento ya existe")
        void registrarPersona_conDocumentoDuplicado_lanzaIllegalArgumentException() {
            // Arrange
            RegistroDTO dto = RegistroDTO.builder()
                    .numeroDocumento("100")
                    .email("a@a.com")
                    .contraseña("clave123")
                    .confirmarContraseña("clave123")
                    .build();
            when(usuarioRepository.existsByNumeroDocumento("100")).thenReturn(true);

            // Act + Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.registrarPersona(dto));
            assertTrue(ex.getMessage().contains("documento"));
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el email ya existe")
        void registrarPersona_conEmailDuplicado_lanzaIllegalArgumentException() {
            // Arrange
            RegistroDTO dto = RegistroDTO.builder()
                    .numeroDocumento("100")
                    .email("dup@mail.com")
                    .contraseña("clave123")
                    .confirmarContraseña("clave123")
                    .build();
            when(usuarioRepository.existsByNumeroDocumento("100")).thenReturn(false);
            when(usuarioRepository.existsByEmail("dup@mail.com")).thenReturn(true);

            // Act + Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.registrarPersona(dto));
            assertTrue(ex.getMessage().contains("email"));
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando las contraseñas no coinciden")
        void registrarPersona_conContraseñasDistintas_lanzaIllegalArgumentException() {
            // Arrange
            RegistroDTO dto = RegistroDTO.builder()
                    .numeroDocumento("100")
                    .email("a@a.com")
                    .contraseña("clave123")
                    .confirmarContraseña("clave999")
                    .build();
            when(usuarioRepository.existsByNumeroDocumento("100")).thenReturn(false);
            when(usuarioRepository.existsByEmail("a@a.com")).thenReturn(false);

            // Act + Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.registrarPersona(dto));
            assertTrue(ex.getMessage().contains("contraseñas"));
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando la contraseña es muy corta")
        void registrarPersona_conContraseñaCorta_lanzaIllegalArgumentException() {
            // Arrange
            RegistroDTO dto = RegistroDTO.builder()
                    .numeroDocumento("100")
                    .email("a@a.com")
                    .contraseña("123")
                    .confirmarContraseña("123")
                    .build();
            when(usuarioRepository.existsByNumeroDocumento("100")).thenReturn(false);
            when(usuarioRepository.existsByEmail("a@a.com")).thenReturn(false);

            // Act + Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.registrarPersona(dto));
            assertTrue(ex.getMessage().contains("6 caracteres"));
            verify(usuarioRepository, never()).save(any());
        }
    }

    // ============================================================
    // obtenerPorNumeroDocumento / obtenerPorEmail
    // ============================================================
    @Nested
    @DisplayName("obtener por documento o email")
    class ObtenerPorIdentificador {

        @Test
        @DisplayName("obtenerPorNumeroDocumento retorna usuario cuando existe")
        void obtenerPorNumeroDocumento_conDocumentoValido_retornaUsuarioDTO() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));

            // Act
            UsuarioDTO resultado = usuarioService.obtenerPorNumeroDocumento("100");

            // Assert
            assertEquals("100", resultado.getNumeroDocumento());
            assertEquals("cliente@mail.com", resultado.getEmail());
        }

        @Test
        @DisplayName("obtenerPorNumeroDocumento lanza ResourceNotFoundException cuando no existe")
        void obtenerPorNumeroDocumento_conDocumentoInexistente_lanzaResourceNotFound() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> usuarioService.obtenerPorNumeroDocumento("999"));
        }

        @Test
        @DisplayName("obtenerPorEmail retorna usuario cuando existe")
        void obtenerPorEmail_conEmailValido_retornaUsuarioDTO() {
            // Arrange
            when(usuarioRepository.findByEmail("cliente@mail.com")).thenReturn(Optional.of(usuarioCliente));

            // Act
            UsuarioDTO resultado = usuarioService.obtenerPorEmail("cliente@mail.com");

            // Assert
            assertEquals("cliente@mail.com", resultado.getEmail());
        }

        @Test
        @DisplayName("obtenerPorEmail lanza ResourceNotFoundException cuando no existe")
        void obtenerPorEmail_conEmailInexistente_lanzaResourceNotFound() {
            // Arrange
            when(usuarioRepository.findByEmail("no@mail.com")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> usuarioService.obtenerPorEmail("no@mail.com"));
        }
    }

    // ============================================================
    // Listados
    // ============================================================
    @Nested
    @DisplayName("listados")
    class Listados {

        @Test
        @DisplayName("obtenerTodas retorna la lista completa")
        void obtenerTodas_conUsuariosExistentes_retornaListaCompleta() {
            // Arrange
            when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuarioCliente, usuarioBarbero));

            // Act
            List<UsuarioDTO> resultado = usuarioService.obtenerTodas();

            // Assert
            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("obtenerTodas retorna lista vacía cuando no hay usuarios")
        void obtenerTodas_sinUsuarios_retornaListaVacia() {
            // Arrange
            when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<UsuarioDTO> resultado = usuarioService.obtenerTodas();

            // Assert
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("obtenerBarberos retorna solo los usuarios con rol 2")
        void obtenerBarberos_conBarberosExistentes_retornaListaDeBarberos() {
            // Arrange
            when(usuarioRepository.findByIdRol(2)).thenReturn(Collections.singletonList(usuarioBarbero));

            // Act
            List<UsuarioDTO> resultado = usuarioService.obtenerBarberos();

            // Assert
            assertEquals(1, resultado.size());
            assertEquals(2, resultado.get(0).getIdRol());
        }

        @Test
        @DisplayName("obtenerUsuariosBloqueados filtra solo clientes (rol 3) en estado 4")
        void obtenerUsuariosBloqueados_filtraSoloClientes() {
            // Arrange
            Usuario clienteBloqueado = Usuario.builder()
                    .numeroDocumento("300").idRol(3).idEstado(4).build();
            Usuario barberoEstado4 = Usuario.builder()
                    .numeroDocumento("400").idRol(2).idEstado(4).build();
            when(usuarioRepository.findByIdEstado(4))
                    .thenReturn(Arrays.asList(clienteBloqueado, barberoEstado4));

            // Act
            List<UsuarioDTO> resultado = usuarioService.obtenerUsuariosBloqueados();

            // Assert
            assertEquals(1, resultado.size());
            assertEquals("300", resultado.get(0).getNumeroDocumento());
        }
    }

    // ============================================================
    // cambiarRol
    // ============================================================
    @Nested
    @DisplayName("cambiarRol")
    class CambiarRol {

        @Test
        @DisplayName("actualiza el rol cuando el usuario existe")
        void cambiarRol_conUsuarioExistente_actualizaRol() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UsuarioDTO resultado = usuarioService.cambiarRol("100", 2);

            // Assert
            assertEquals(2, resultado.getIdRol());
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando el usuario no existe")
        void cambiarRol_conUsuarioInexistente_lanzaResourceNotFound() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> usuarioService.cambiarRol("999", 2));
        }
    }

    // ============================================================
    // bloquearUsuario / desbloquearUsuario
    // ============================================================
    @Nested
    @DisplayName("bloquear y desbloquear cliente")
    class BloquearDesbloquear {

        @Test
        @DisplayName("bloquearUsuario cambia idEstado a 4 cuando es cliente")
        void bloquearUsuario_conCliente_cambiaEstadoA4() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UsuarioDTO resultado = usuarioService.bloquearUsuario("100");

            // Assert
            assertEquals(4, resultado.getIdEstado());
        }

        @Test
        @DisplayName("bloquearUsuario lanza IllegalArgumentException cuando no es cliente")
        void bloquearUsuario_conNoCliente_lanzaIllegalArgumentException() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("200")).thenReturn(Optional.of(usuarioBarbero));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.bloquearUsuario("200"));
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("bloquearUsuario lanza ResourceNotFoundException cuando el usuario no existe")
        void bloquearUsuario_conUsuarioInexistente_lanzaResourceNotFound() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> usuarioService.bloquearUsuario("999"));
        }

        @Test
        @DisplayName("desbloquearUsuario cambia idEstado a 1 cuando está bloqueado")
        void desbloquearUsuario_conClienteBloqueado_cambiaEstadoA1() {
            // Arrange
            usuarioCliente.setIdEstado(4);
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UsuarioDTO resultado = usuarioService.desbloquearUsuario("100");

            // Assert
            assertEquals(1, resultado.getIdEstado());
        }

        @Test
        @DisplayName("desbloquearUsuario lanza IllegalArgumentException cuando no está bloqueado")
        void desbloquearUsuario_conClienteActivo_lanzaIllegalArgumentException() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.desbloquearUsuario("100"));
        }

        @Test
        @DisplayName("desbloquearUsuario lanza ResourceNotFoundException cuando el usuario no existe")
        void desbloquearUsuario_conUsuarioInexistente_lanzaResourceNotFound() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> usuarioService.desbloquearUsuario("999"));
        }
    }

    // ============================================================
    // deshabilitarBarbero / habilitarBarbero
    // ============================================================
    @Nested
    @DisplayName("deshabilitar y habilitar barbero")
    class DeshabilitarHabilitarBarbero {

        @Test
        @DisplayName("deshabilitarBarbero cambia idEstado a 5 cuando es barbero")
        void deshabilitarBarbero_conBarbero_cambiaEstadoA5() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("200")).thenReturn(Optional.of(usuarioBarbero));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UsuarioDTO resultado = usuarioService.deshabilitarBarbero("200");

            // Assert
            assertEquals(5, resultado.getIdEstado());
        }

        @Test
        @DisplayName("deshabilitarBarbero lanza IllegalArgumentException cuando no es barbero")
        void deshabilitarBarbero_conNoBarbero_lanzaIllegalArgumentException() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.deshabilitarBarbero("100"));
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("deshabilitarBarbero lanza ResourceNotFoundException cuando no existe")
        void deshabilitarBarbero_conUsuarioInexistente_lanzaResourceNotFound() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> usuarioService.deshabilitarBarbero("999"));
        }

        @Test
        @DisplayName("habilitarBarbero cambia idEstado a 1 cuando barbero está deshabilitado")
        void habilitarBarbero_conBarberoDeshabilitado_cambiaEstadoA1() {
            // Arrange
            usuarioBarbero.setIdEstado(5);
            when(usuarioRepository.findByNumeroDocumento("200")).thenReturn(Optional.of(usuarioBarbero));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UsuarioDTO resultado = usuarioService.habilitarBarbero("200");

            // Assert
            assertEquals(1, resultado.getIdEstado());
        }

        @Test
        @DisplayName("habilitarBarbero lanza IllegalArgumentException cuando no es barbero")
        void habilitarBarbero_conNoBarbero_lanzaIllegalArgumentException() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.habilitarBarbero("100"));
        }

        @Test
        @DisplayName("habilitarBarbero lanza IllegalArgumentException cuando barbero no está deshabilitado")
        void habilitarBarbero_conBarberoActivo_lanzaIllegalArgumentException() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("200")).thenReturn(Optional.of(usuarioBarbero));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.habilitarBarbero("200"));
        }

        @Test
        @DisplayName("habilitarBarbero lanza ResourceNotFoundException cuando no existe")
        void habilitarBarbero_conUsuarioInexistente_lanzaResourceNotFound() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> usuarioService.habilitarBarbero("999"));
        }
    }

    // ============================================================
    // actualizarPerfil
    // ============================================================
    @Nested
    @DisplayName("actualizarPerfil")
    class ActualizarPerfil {

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el DTO es null")
        void actualizarPerfil_conDtoNull_lanzaIllegalArgumentException() {
            // Arrange + Act + Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.actualizarPerfil("100", null));
            assertTrue(ex.getMessage().contains("requerido"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el documento es blanco")
        void actualizarPerfil_conDocumentoBlanco_lanzaIllegalArgumentException() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder().nombrePersona("X").build();

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.actualizarPerfil("  ", dto));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando el usuario no existe")
        void actualizarPerfil_conUsuarioInexistente_lanzaResourceNotFound() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder().nombrePersona("X").build();
            when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> usuarioService.actualizarPerfil("999", dto));
        }

        @Test
        @DisplayName("actualiza nombre, celular y email cuando los datos son válidos")
        void actualizarPerfil_conDatosValidos_actualizaCamposBasicos() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder()
                    .nombrePersona("Nuevo Nombre")
                    .numeroCelular("3019998888")
                    .email("nuevo@mail.com")
                    .build();
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));
            when(usuarioRepository.existsByEmail("nuevo@mail.com")).thenReturn(false);
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UsuarioDTO resultado = usuarioService.actualizarPerfil("100", dto);

            // Assert
            assertEquals("Nuevo Nombre", resultado.getNombrePersona());
            assertEquals("3019998888", resultado.getNumeroCelular());
            assertEquals("nuevo@mail.com", resultado.getEmail());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el celular tiene formato inválido")
        void actualizarPerfil_conCelularInvalido_lanzaIllegalArgumentException() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder().numeroCelular("ABC").build();
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.actualizarPerfil("100", dto));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el email ya está en uso")
        void actualizarPerfil_conEmailEnUso_lanzaIllegalArgumentException() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder().email("otro@mail.com").build();
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));
            when(usuarioRepository.existsByEmail("otro@mail.com")).thenReturn(true);

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.actualizarPerfil("100", dto));
        }

        @Test
        @DisplayName("permite mismo email del usuario (no valida duplicado)")
        void actualizarPerfil_conMismoEmail_noValidaDuplicado() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder().email("cliente@mail.com").build();
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UsuarioDTO resultado = usuarioService.actualizarPerfil("100", dto);

            // Assert
            assertEquals("cliente@mail.com", resultado.getEmail());
            verify(usuarioRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando no se provee ningún campo")
        void actualizarPerfil_sinCamposParaActualizar_lanzaIllegalArgumentException() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder().build();
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.actualizarPerfil("100", dto));
        }

        @Test
        @DisplayName("cambia contraseña cuando todos los campos coinciden")
        void actualizarPerfil_conCambioPasswordValido_actualizaPassword() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder()
                    .currentPassword("actual")
                    .newPassword("nuevaPass")
                    .confirmarNuevaPassword("nuevaPass")
                    .build();
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));
            when(passwordEncoder.matches("actual", "hashed")).thenReturn(true);
            when(passwordEncoder.encode("nuevaPass")).thenReturn("nuevaHashed");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UsuarioDTO resultado = usuarioService.actualizarPerfil("100", dto);

            // Assert
            assertNotNull(resultado);
            verify(passwordEncoder).encode("nuevaPass");
        }

        @Test
        @DisplayName("lanza IllegalArgumentException si falta la contraseña actual")
        void actualizarPerfil_sinPasswordActual_lanzaIllegalArgumentException() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder()
                    .newPassword("nuevaPass")
                    .confirmarNuevaPassword("nuevaPass")
                    .build();
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.actualizarPerfil("100", dto));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException si la contraseña actual no coincide")
        void actualizarPerfil_conPasswordActualIncorrecta_lanzaIllegalArgumentException() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder()
                    .currentPassword("mala")
                    .newPassword("nuevaPass")
                    .confirmarNuevaPassword("nuevaPass")
                    .build();
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));
            when(passwordEncoder.matches("mala", "hashed")).thenReturn(false);

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.actualizarPerfil("100", dto));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException si la nueva contraseña es muy corta")
        void actualizarPerfil_conNuevaPasswordCorta_lanzaIllegalArgumentException() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder()
                    .currentPassword("actual")
                    .newPassword("123")
                    .confirmarNuevaPassword("123")
                    .build();
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));
            when(passwordEncoder.matches("actual", "hashed")).thenReturn(true);

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.actualizarPerfil("100", dto));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException si confirmar password no coincide")
        void actualizarPerfil_conConfirmacionDistinta_lanzaIllegalArgumentException() {
            // Arrange
            UpdatePerfilDTO dto = UpdatePerfilDTO.builder()
                    .currentPassword("actual")
                    .newPassword("nuevaPass")
                    .confirmarNuevaPassword("otraPass")
                    .build();
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(usuarioCliente));
            when(passwordEncoder.matches("actual", "hashed")).thenReturn(true);

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.actualizarPerfil("100", dto));
        }
    }
}
