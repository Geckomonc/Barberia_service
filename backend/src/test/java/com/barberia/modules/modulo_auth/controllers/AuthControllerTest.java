package com.barberia.modules.modulo_auth.controllers;

import com.barberia.modules.modulo_auth.models.dtos.LoginDTO;
import com.barberia.modules.modulo_auth.models.dtos.LoginResponseDTO;
import com.barberia.modules.modulo_auth.services.AuthService;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.shared.utils.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link AuthController} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginDTO loginDTO;
    private LoginResponseDTO loginResponseDTO;

    @BeforeEach
    void setUp() {
        loginDTO = LoginDTO.builder()
                .email("user@mail.com")
                .contraseña("password123")
                .build();

        UsuarioDTO usuarioDTO = UsuarioDTO.builder()
                .numeroDocumento("100")
                .email("user@mail.com")
                .idRol(3)
                .idEstado(1)
                .build();

        loginResponseDTO = LoginResponseDTO.builder()
                .token("jwt-token")
                .type("Bearer")
                .usuario(usuarioDTO)
                .build();
    }

    @Test
    @DisplayName("login retorna 200 OK con token cuando las credenciales son correctas")
    void login_conCredencialesValidas_retorna200() {
        // Arrange
        when(authService.iniciarSesion(loginDTO)).thenReturn(loginResponseDTO);

        // Act
        ResponseEntity<ApiResponse<LoginResponseDTO>> response = authController.login(loginDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Login exitoso", response.getBody().getMessage());
        assertEquals("jwt-token", response.getBody().getData().getToken());
        assertEquals("Bearer", response.getBody().getData().getType());
    }

    @Test
    @DisplayName("login retorna 401 UNAUTHORIZED cuando el service lanza IllegalArgumentException")
    void login_conCredencialesInvalidas_retorna401() {
        // Arrange
        when(authService.iniciarSesion(loginDTO))
                .thenThrow(new IllegalArgumentException("Error: Credenciales inválidas"));

        // Act
        ResponseEntity<ApiResponse<LoginResponseDTO>> response = authController.login(loginDTO);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Credenciales"));
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("login retorna 500 INTERNAL_SERVER_ERROR ante una excepción inesperada")
    void login_conExcepcionInesperada_retorna500() {
        // Arrange
        when(authService.iniciarSesion(loginDTO))
                .thenThrow(new RuntimeException("Conexión perdida"));

        // Act
        ResponseEntity<ApiResponse<LoginResponseDTO>> response = authController.login(loginDTO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("servidor"));
    }

    @Test
    @DisplayName("login invoca exactamente una vez al service")
    void login_invocaServiceExactamenteUnaVez() {
        // Arrange
        when(authService.iniciarSesion(loginDTO)).thenReturn(loginResponseDTO);

        // Act
        authController.login(loginDTO);

        // Assert
        verify(authService, times(1)).iniciarSesion(loginDTO);
    }
}
