package com.barberia.modules.modulo_usuarios.services;

import com.barberia.modules.modulo_usuarios.models.dtos.RegistroDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void registrarPersona_DeberiaRegistrarUsuarioCorrectamente() {
        RegistroDTO registroDTO = crearRegistroDTOValido();

        when(usuarioRepository.existsByNumeroDocumento("123456789")).thenReturn(false);
        when(usuarioRepository.existsByEmail("usuario@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("password-hasheada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioDTO resultado = usuarioService.registrarPersona(registroDTO);

        assertNotNull(resultado);
        assertEquals("123456789", resultado.getNumeroDocumento());
        assertEquals("3001234567", resultado.getNumeroCelular());
        assertEquals("usuario@test.com", resultado.getEmail());
        assertEquals("Usuario Prueba", resultado.getNombrePersona());
        assertEquals(1, resultado.getIdEstado());
        assertEquals(3, resultado.getIdRol());

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioGuardado = usuarioCaptor.getValue();

        assertEquals("123456789", usuarioGuardado.getNumeroDocumento());
        assertEquals("3001234567", usuarioGuardado.getNumeroCelular());
        assertEquals("usuario@test.com", usuarioGuardado.getEmail());
        assertEquals("Usuario Prueba", usuarioGuardado.getNombrePersona());
        assertEquals("password-hasheada", usuarioGuardado.getContrasenaHasheada());
        assertEquals(1, usuarioGuardado.getIdEstado());
        assertEquals(3, usuarioGuardado.getIdRol());

        verify(passwordEncoder).encode("123456");
    }

    @Test
    void registrarPersona_DeberiaLanzarErrorCuandoDocumentoYaExiste() {
        RegistroDTO registroDTO = mock(RegistroDTO.class);

        when(registroDTO.getNumeroDocumento()).thenReturn("123456789");
        when(usuarioRepository.existsByNumeroDocumento("123456789")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarPersona(registroDTO)
        );

        assertEquals("Error: El número de documento ya está registrado", exception.getMessage());

        verify(usuarioRepository).existsByNumeroDocumento("123456789");
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registrarPersona_DeberiaLanzarErrorCuandoEmailYaExiste() {
        RegistroDTO registroDTO = mock(RegistroDTO.class);

        when(registroDTO.getNumeroDocumento()).thenReturn("123456789");
        when(registroDTO.getEmail()).thenReturn("usuario@test.com");

        when(usuarioRepository.existsByNumeroDocumento("123456789")).thenReturn(false);
        when(usuarioRepository.existsByEmail("usuario@test.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarPersona(registroDTO)
        );

        assertEquals("Error: El email ya está registrado", exception.getMessage());

        verify(usuarioRepository).existsByNumeroDocumento("123456789");
        verify(usuarioRepository).existsByEmail("usuario@test.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registrarPersona_DeberiaLanzarErrorCuandoContrasenasNoCoinciden() {
        RegistroDTO registroDTO = mock(RegistroDTO.class);

        when(registroDTO.getNumeroDocumento()).thenReturn("123456789");
        when(registroDTO.getEmail()).thenReturn("usuario@test.com");
        when(registroDTO.getContraseña()).thenReturn("123456");
        when(registroDTO.getConfirmarContraseña()).thenReturn("654321");

        when(usuarioRepository.existsByNumeroDocumento("123456789")).thenReturn(false);
        when(usuarioRepository.existsByEmail("usuario@test.com")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarPersona(registroDTO)
        );

        assertEquals("Error: Las contraseñas no coinciden", exception.getMessage());

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registrarPersona_DeberiaLanzarErrorCuandoContrasenaEsMuyCorta() {
        RegistroDTO registroDTO = mock(RegistroDTO.class);

        when(registroDTO.getNumeroDocumento()).thenReturn("123456789");
        when(registroDTO.getEmail()).thenReturn("usuario@test.com");
        when(registroDTO.getContraseña()).thenReturn("123");
        when(registroDTO.getConfirmarContraseña()).thenReturn("123");

        when(usuarioRepository.existsByNumeroDocumento("123456789")).thenReturn(false);
        when(usuarioRepository.existsByEmail("usuario@test.com")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarPersona(registroDTO)
        );

        assertEquals("Error: La contraseña debe tener al menos 6 caracteres", exception.getMessage());

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void obtenerPorNumeroDocumento_DeberiaRetornarUsuarioCuandoExiste() {
        Usuario usuario = crearUsuarioValido();

        when(usuarioRepository.findByNumeroDocumento("123456789")).thenReturn(Optional.of(usuario));

        UsuarioDTO resultado = usuarioService.obtenerPorNumeroDocumento("123456789");

        assertNotNull(resultado);
        assertEquals("123456789", resultado.getNumeroDocumento());
        assertEquals("usuario@test.com", resultado.getEmail());
        assertEquals("Usuario Prueba", resultado.getNombrePersona());
    }

    @Test
    void obtenerPorNumeroDocumento_DeberiaLanzarErrorCuandoNoExiste() {
        when(usuarioRepository.findByNumeroDocumento("999999999")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> usuarioService.obtenerPorNumeroDocumento("999999999")
        );

        assertEquals("Persona no encontrada con documento: 999999999", exception.getMessage());
    }

    @Test
    void obtenerPorEmail_DeberiaRetornarUsuarioCuandoExiste() {
        Usuario usuario = crearUsuarioValido();

        when(usuarioRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(usuario));

        UsuarioDTO resultado = usuarioService.obtenerPorEmail("usuario@test.com");

        assertNotNull(resultado);
        assertEquals("123456789", resultado.getNumeroDocumento());
        assertEquals("usuario@test.com", resultado.getEmail());
        assertEquals("Usuario Prueba", resultado.getNombrePersona());
    }

    @Test
    void obtenerPorEmail_DeberiaLanzarErrorCuandoNoExiste() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> usuarioService.obtenerPorEmail("noexiste@test.com")
        );

        assertEquals("Persona no encontrada con email: noexiste@test.com", exception.getMessage());
    }

    @Test
    void obtenerTodas_DeberiaRetornarListaDeUsuarios() {
        Usuario usuarioUno = crearUsuarioValido();

        Usuario usuarioDos = Usuario.builder()
                .numeroDocumento("987654321")
                .numeroCelular("3017654321")
                .email("usuario2@test.com")
                .nombrePersona("Usuario Dos")
                .contrasenaHasheada("password-hasheada")
                .idRol(3)
                .idEstado(1)
                .build();

        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioUno, usuarioDos));

        List<UsuarioDTO> resultado = usuarioService.obtenerTodas();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("123456789", resultado.get(0).getNumeroDocumento());
        assertEquals("987654321", resultado.get(1).getNumeroDocumento());
    }

    @Test
    void obtenerBarberos_DeberiaRetornarUsuariosConRolBarbero() {
        Usuario barbero = Usuario.builder()
                .numeroDocumento("222222222")
                .numeroCelular("3021234567")
                .email("barbero@test.com")
                .nombrePersona("Barbero Prueba")
                .contrasenaHasheada("password-hasheada")
                .idRol(2)
                .idEstado(1)
                .build();

        when(usuarioRepository.findByIdRol(2)).thenReturn(List.of(barbero));

        List<UsuarioDTO> resultado = usuarioService.obtenerBarberos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("222222222", resultado.get(0).getNumeroDocumento());
        assertEquals(2, resultado.get(0).getIdRol());
    }

    @Test
    void cambiarRol_DeberiaActualizarRolCorrectamente() {
        Usuario usuario = crearUsuarioValido();

        when(usuarioRepository.findByNumeroDocumento("123456789")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioDTO resultado = usuarioService.cambiarRol("123456789", 2);

        assertNotNull(resultado);
        assertEquals("123456789", resultado.getNumeroDocumento());
        assertEquals(2, resultado.getIdRol());

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarRol_DeberiaLanzarErrorCuandoUsuarioNoExiste() {
        when(usuarioRepository.findByNumeroDocumento("999999999")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> usuarioService.cambiarRol("999999999", 2)
        );

        assertEquals("Persona no encontrada con documento: 999999999", exception.getMessage());

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    private RegistroDTO crearRegistroDTOValido() {
        RegistroDTO registroDTO = mock(RegistroDTO.class);

        when(registroDTO.getNumeroDocumento()).thenReturn("123456789");
        when(registroDTO.getNumeroCelular()).thenReturn("3001234567");
        when(registroDTO.getEmail()).thenReturn("usuario@test.com");
        when(registroDTO.getNombrePersona()).thenReturn("Usuario Prueba");
        when(registroDTO.getContraseña()).thenReturn("123456");
        when(registroDTO.getConfirmarContraseña()).thenReturn("123456");

        return registroDTO;
    }

    private Usuario crearUsuarioValido() {
        return Usuario.builder()
                .numeroDocumento("123456789")
                .numeroCelular("3001234567")
                .email("usuario@test.com")
                .nombrePersona("Usuario Prueba")
                .contrasenaHasheada("password-hasheada")
                .idRol(3)
                .idEstado(1)
                .build();
    }
}