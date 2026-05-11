package com.barberia.modules.modulo_auth.services;

import com.barberia.modules.modulo_auth.models.dtos.LoginDTO;
import com.barberia.modules.modulo_auth.models.dtos.LoginResponseDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void iniciarSesion_DeberiaRetornarTokenCuandoCredencialesSonValidas() {
        LoginDTO loginDTO = mock(LoginDTO.class);
        Usuario usuario = mock(Usuario.class);

        when(loginDTO.getEmail()).thenReturn("usuario@test.com");
        when(loginDTO.getContraseña()).thenReturn("123456");

        when(usuario.getEmail()).thenReturn("usuario@test.com");
        when(usuario.getNumeroDocumento()).thenReturn("123456789");
        when(usuario.getNumeroCelular()).thenReturn("3001234567");
        when(usuario.getNombrePersona()).thenReturn("Usuario Prueba");
        when(usuario.getContrasenaHasheada()).thenReturn("password-hasheada");
        when(usuario.getIdEstado()).thenReturn(1);
        when(usuario.getIdRol()).thenReturn(3);

        when(usuarioRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", "password-hasheada")).thenReturn(true);
        when(jwtTokenProvider.generateToken("usuario@test.com", "123456789", 3)).thenReturn("jwt-token-prueba");

        LoginResponseDTO resultado = authService.iniciarSesion(loginDTO);

        assertNotNull(resultado);
        assertEquals("jwt-token-prueba", resultado.getToken());
        assertEquals("Bearer", resultado.getType());

        UsuarioDTO usuarioDTO = resultado.getUsuario();

        assertNotNull(usuarioDTO);
        assertEquals("123456789", usuarioDTO.getNumeroDocumento());
        assertEquals("3001234567", usuarioDTO.getNumeroCelular());
        assertEquals("usuario@test.com", usuarioDTO.getEmail());
        assertEquals("Usuario Prueba", usuarioDTO.getNombrePersona());
        assertEquals(1, usuarioDTO.getIdEstado());
        assertEquals(3, usuarioDTO.getIdRol());

        verify(usuarioRepository).findByEmail("usuario@test.com");
        verify(passwordEncoder).matches("123456", "password-hasheada");
        verify(jwtTokenProvider).generateToken("usuario@test.com", "123456789", 3);
    }

    @Test
    void iniciarSesion_DeberiaLanzarErrorCuandoUsuarioNoExiste() {
        LoginDTO loginDTO = mock(LoginDTO.class);

        when(loginDTO.getEmail()).thenReturn("noexiste@test.com");
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.iniciarSesion(loginDTO)
        );

        assertEquals("Error: Credenciales inválidas", exception.getMessage());

        verify(usuarioRepository).findByEmail("noexiste@test.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString(), anyInt());
    }

    @Test
    void iniciarSesion_DeberiaLanzarErrorCuandoContrasenaEsIncorrecta() {
        LoginDTO loginDTO = mock(LoginDTO.class);
        Usuario usuario = mock(Usuario.class);

        when(loginDTO.getEmail()).thenReturn("usuario@test.com");
        when(loginDTO.getContraseña()).thenReturn("clave-incorrecta");

        when(usuario.getContrasenaHasheada()).thenReturn("password-hasheada");

        when(usuarioRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave-incorrecta", "password-hasheada")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.iniciarSesion(loginDTO)
        );

        assertEquals("Error: Credenciales inválidas", exception.getMessage());

        verify(usuarioRepository).findByEmail("usuario@test.com");
        verify(passwordEncoder).matches("clave-incorrecta", "password-hasheada");
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString(), anyInt());
    }
}