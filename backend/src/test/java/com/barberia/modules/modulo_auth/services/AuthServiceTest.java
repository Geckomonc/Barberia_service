package com.barberia.modules.modulo_auth.services;

import com.barberia.modules.modulo_auth.models.dtos.LoginDTO;
import com.barberia.modules.modulo_auth.models.dtos.LoginResponseDTO;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link AuthService} siguiendo el patrón AAA.
 */
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

    private Usuario usuario;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .numeroDocumento("100")
                .numeroCelular("3001234567")
                .email("user@mail.com")
                .nombrePersona("Usuario Test")
                .contrasenaHasheada("hashedPassword")
                .idRol(3)
                .idEstado(1)
                .fechaRegistro(LocalDateTime.now())
                .build();

        loginDTO = LoginDTO.builder()
                .email("user@mail.com")
                .contraseña("password123")
                .build();
    }

    @Test
    @DisplayName("iniciarSesion retorna LoginResponseDTO con token y usuario cuando las credenciales son correctas")
    void iniciarSesion_conCredencialesValidas_retornaLoginResponseDTO() {
        // Arrange
        when(usuarioRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("user@mail.com", "100", 3)).thenReturn("jwt-token");

        // Act
        LoginResponseDTO response = authService.iniciarSesion(loginDTO);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertNotNull(response.getUsuario());
        assertEquals("user@mail.com", response.getUsuario().getEmail());
        assertEquals("100", response.getUsuario().getNumeroDocumento());
    }

    @Test
    @DisplayName("iniciarSesion lanza IllegalArgumentException cuando el email no existe")
    void iniciarSesion_conEmailInexistente_lanzaIllegalArgumentException() {
        // Arrange
        when(usuarioRepository.findByEmail("user@mail.com")).thenReturn(Optional.empty());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.iniciarSesion(loginDTO));
        assertTrue(ex.getMessage().contains("Credenciales"));
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("iniciarSesion lanza IllegalArgumentException cuando la contraseña es incorrecta")
    void iniciarSesion_conContraseñaIncorrecta_lanzaIllegalArgumentException() {
        // Arrange
        when(usuarioRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(false);

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.iniciarSesion(loginDTO));
        assertTrue(ex.getMessage().contains("Credenciales"));
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("iniciarSesion no expone la contraseña hasheada en la respuesta")
    void iniciarSesion_noExponeContraseñaHasheada() {
        // Arrange
        when(usuarioRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyString(), anyInt())).thenReturn("jwt-token");

        // Act
        LoginResponseDTO response = authService.iniciarSesion(loginDTO);

        // Assert
        assertNotNull(response.getUsuario());
        // El UsuarioDTO no tiene campo de contraseña, solo verificamos campos seguros
        assertEquals(3, response.getUsuario().getIdRol());
        assertEquals(1, response.getUsuario().getIdEstado());
        assertEquals("Usuario Test", response.getUsuario().getNombrePersona());
    }

    @Test
    @DisplayName("iniciarSesion genera token con email, documento y rol correctos")
    void iniciarSesion_generaTokenConDatosDelUsuario() {
        // Arrange
        when(usuarioRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("user@mail.com", "100", 3)).thenReturn("jwt-token");

        // Act
        authService.iniciarSesion(loginDTO);

        // Assert
        verify(jwtTokenProvider, times(1)).generateToken("user@mail.com", "100", 3);
    }
}
