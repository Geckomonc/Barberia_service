package com.barberia.modules.modulo_usuarios.controllers;

import com.barberia.modules.modulo_usuarios.models.dtos.RegistroDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UpdatePerfilDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.services.UsuarioService;
import com.barberia.shared.exceptions.ResourceNotFoundException;
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
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link UsuarioController} siguiendo el patrón AAA.
 * Las pruebas invocan los métodos del controller directamente y validan
 * el ResponseEntity retornado.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UsuarioController usuarioController;

    private UsuarioDTO usuarioDTO;

    @BeforeEach
    void setUp() {
        usuarioDTO = UsuarioDTO.builder()
                .numeroDocumento("100")
                .numeroCelular("3001234567")
                .email("test@mail.com")
                .nombrePersona("Test")
                .idEstado(1)
                .idRol(3)
                .build();
    }

    // ============================================================
    // registrarPersona
    // ============================================================
    @Test
    @DisplayName("registrarPersona retorna 201 CREATED cuando el registro es exitoso")
    void registrarPersona_conDatosValidos_retorna201() {
        // Arrange
        RegistroDTO dto = RegistroDTO.builder().numeroDocumento("100").build();
        when(usuarioService.registrarPersona(dto)).thenReturn(usuarioDTO);

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.registrarPersona(dto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("100", response.getBody().getData().getNumeroDocumento());
    }

    @Test
    @DisplayName("registrarPersona retorna 400 BAD_REQUEST cuando el service lanza IllegalArgumentException")
    void registrarPersona_conDatosInvalidos_retorna400() {
        // Arrange
        RegistroDTO dto = RegistroDTO.builder().numeroDocumento("100").build();
        when(usuarioService.registrarPersona(dto))
                .thenThrow(new IllegalArgumentException("Documento duplicado"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.registrarPersona(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Documento duplicado", response.getBody().getMessage());
    }

    // ============================================================
    // obtenerPorDocumento
    // ============================================================
    @Test
    @DisplayName("obtenerPorDocumento retorna 200 OK cuando el usuario existe")
    void obtenerPorDocumento_conDocumentoValido_retorna200() {
        // Arrange
        when(usuarioService.obtenerPorNumeroDocumento("100")).thenReturn(usuarioDTO);

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.obtenerPorDocumento("100");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("100", response.getBody().getData().getNumeroDocumento());
    }

    @Test
    @DisplayName("obtenerPorDocumento retorna 404 NOT_FOUND cuando no existe")
    void obtenerPorDocumento_conDocumentoInexistente_retorna404() {
        // Arrange
        when(usuarioService.obtenerPorNumeroDocumento("999"))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.obtenerPorDocumento("999");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    // ============================================================
    // obtenerPorEmail
    // ============================================================
    @Test
    @DisplayName("obtenerPorEmail retorna 200 OK cuando el usuario existe")
    void obtenerPorEmail_conEmailValido_retorna200() {
        // Arrange
        when(usuarioService.obtenerPorEmail("test@mail.com")).thenReturn(usuarioDTO);

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.obtenerPorEmail("test@mail.com");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test@mail.com", response.getBody().getData().getEmail());
    }

    @Test
    @DisplayName("obtenerPorEmail retorna 404 NOT_FOUND cuando no existe")
    void obtenerPorEmail_conEmailInexistente_retorna404() {
        // Arrange
        when(usuarioService.obtenerPorEmail("no@mail.com"))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.obtenerPorEmail("no@mail.com");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ============================================================
    // obtenerBarberos / obtenerTodas / obtenerUsuariosBloqueados
    // ============================================================
    @Test
    @DisplayName("obtenerBarberos retorna 200 OK con la lista de barberos")
    void obtenerBarberos_retornaListaDeBarberos() {
        // Arrange
        List<UsuarioDTO> lista = Collections.singletonList(usuarioDTO);
        when(usuarioService.obtenerBarberos()).thenReturn(lista);

        // Act
        ResponseEntity<ApiResponse<List<UsuarioDTO>>> response = usuarioController.obtenerBarberos();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    @DisplayName("obtenerTodas retorna 200 OK con la lista completa")
    void obtenerTodas_retornaListaCompleta() {
        // Arrange
        when(usuarioService.obtenerTodas()).thenReturn(Collections.singletonList(usuarioDTO));

        // Act
        ResponseEntity<ApiResponse<List<UsuarioDTO>>> response = usuarioController.obtenerTodas();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    @DisplayName("obtenerUsuariosBloqueados retorna 200 OK con la lista")
    void obtenerUsuariosBloqueados_retornaLista() {
        // Arrange
        when(usuarioService.obtenerUsuariosBloqueados()).thenReturn(Collections.singletonList(usuarioDTO));

        // Act
        ResponseEntity<ApiResponse<List<UsuarioDTO>>> response = usuarioController.obtenerUsuariosBloqueados();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());
    }

    // ============================================================
    // actualizarPerfil
    // ============================================================
    @Test
    @DisplayName("actualizarPerfil retorna 200 OK cuando es exitoso")
    void actualizarPerfil_conDatosValidos_retorna200() {
        // Arrange
        UpdatePerfilDTO dto = UpdatePerfilDTO.builder().nombrePersona("Nuevo").build();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(usuarioService.actualizarPerfil("100", dto)).thenReturn(usuarioDTO);

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.actualizarPerfil(dto, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("actualizarPerfil usa authentication.getName() cuando no hay numeroDocumento en details")
    void actualizarPerfil_sinDetailsMap_usaAuthenticationName() {
        // Arrange
        UpdatePerfilDTO dto = UpdatePerfilDTO.builder().nombrePersona("Nuevo").build();
        when(authentication.getDetails()).thenReturn(null);
        when(authentication.getName()).thenReturn("100");
        when(usuarioService.actualizarPerfil("100", dto)).thenReturn(usuarioDTO);

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.actualizarPerfil(dto, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("actualizarPerfil retorna 400 BAD_REQUEST con IllegalArgumentException")
    void actualizarPerfil_conIllegalArgument_retorna400() {
        // Arrange
        UpdatePerfilDTO dto = UpdatePerfilDTO.builder().build();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(usuarioService.actualizarPerfil(eq("100"), any(UpdatePerfilDTO.class)))
                .thenThrow(new IllegalArgumentException("Sin campos"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.actualizarPerfil(dto, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("actualizarPerfil retorna 500 INTERNAL_SERVER_ERROR ante una excepción inesperada")
    void actualizarPerfil_conExcepcionInesperada_retorna500() {
        // Arrange
        UpdatePerfilDTO dto = UpdatePerfilDTO.builder().nombrePersona("X").build();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(usuarioService.actualizarPerfil(eq("100"), any(UpdatePerfilDTO.class)))
                .thenThrow(new RuntimeException("BD caída"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.actualizarPerfil(dto, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ============================================================
    // cambiarRol
    // ============================================================
    @Test
    @DisplayName("cambiarRol retorna 200 OK cuando el cambio es exitoso")
    void cambiarRol_conUsuarioValido_retorna200() {
        // Arrange
        when(usuarioService.cambiarRol("100", 2)).thenReturn(usuarioDTO);

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.cambiarRol("100", 2);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("cambiarRol retorna 404 NOT_FOUND cuando el usuario no existe")
    void cambiarRol_conUsuarioInexistente_retorna404() {
        // Arrange
        when(usuarioService.cambiarRol("999", 2))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.cambiarRol("999", 2);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ============================================================
    // bloquearUsuario / desbloquearUsuario
    // ============================================================
    @Test
    @DisplayName("bloquearUsuario retorna 200 OK cuando es exitoso")
    void bloquearUsuario_conClienteValido_retorna200() {
        // Arrange
        when(usuarioService.bloquearUsuario("100")).thenReturn(usuarioDTO);

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.bloquearUsuario("100");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("bloquearUsuario retorna 400 BAD_REQUEST cuando es IllegalArgumentException")
    void bloquearUsuario_conNoCliente_retorna400() {
        // Arrange
        when(usuarioService.bloquearUsuario("100"))
                .thenThrow(new IllegalArgumentException("No es cliente"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.bloquearUsuario("100");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("bloquearUsuario retorna 404 NOT_FOUND cuando el usuario no existe")
    void bloquearUsuario_conUsuarioInexistente_retorna404() {
        // Arrange
        when(usuarioService.bloquearUsuario("999"))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.bloquearUsuario("999");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("desbloquearUsuario retorna 200 OK cuando es exitoso")
    void desbloquearUsuario_conClienteBloqueado_retorna200() {
        // Arrange
        when(usuarioService.desbloquearUsuario("100")).thenReturn(usuarioDTO);

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.desbloquearUsuario("100");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("desbloquearUsuario retorna 400 BAD_REQUEST cuando es IllegalArgumentException")
    void desbloquearUsuario_conClienteActivo_retorna400() {
        // Arrange
        when(usuarioService.desbloquearUsuario("100"))
                .thenThrow(new IllegalArgumentException("No está bloqueado"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.desbloquearUsuario("100");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("desbloquearUsuario retorna 404 NOT_FOUND cuando el usuario no existe")
    void desbloquearUsuario_conUsuarioInexistente_retorna404() {
        // Arrange
        when(usuarioService.desbloquearUsuario("999"))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.desbloquearUsuario("999");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ============================================================
    // deshabilitarBarbero / habilitarBarbero
    // ============================================================
    @Test
    @DisplayName("deshabilitarBarbero retorna 200 OK cuando es exitoso")
    void deshabilitarBarbero_conBarbero_retorna200() {
        // Arrange
        when(usuarioService.deshabilitarBarbero("200")).thenReturn(usuarioDTO);

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.deshabilitarBarbero("200");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("deshabilitarBarbero retorna 400 BAD_REQUEST cuando es IllegalArgumentException")
    void deshabilitarBarbero_conNoBarbero_retorna400() {
        // Arrange
        when(usuarioService.deshabilitarBarbero("100"))
                .thenThrow(new IllegalArgumentException("No es barbero"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.deshabilitarBarbero("100");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("deshabilitarBarbero retorna 404 NOT_FOUND cuando no existe")
    void deshabilitarBarbero_conUsuarioInexistente_retorna404() {
        // Arrange
        when(usuarioService.deshabilitarBarbero("999"))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.deshabilitarBarbero("999");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("habilitarBarbero retorna 200 OK cuando es exitoso")
    void habilitarBarbero_conBarberoDeshabilitado_retorna200() {
        // Arrange
        when(usuarioService.habilitarBarbero("200")).thenReturn(usuarioDTO);

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.habilitarBarbero("200");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("habilitarBarbero retorna 400 BAD_REQUEST con IllegalArgumentException")
    void habilitarBarbero_conNoBarbero_retorna400() {
        // Arrange
        when(usuarioService.habilitarBarbero("100"))
                .thenThrow(new IllegalArgumentException("No es barbero"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.habilitarBarbero("100");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("habilitarBarbero retorna 404 NOT_FOUND cuando no existe")
    void habilitarBarbero_conUsuarioInexistente_retorna404() {
        // Arrange
        when(usuarioService.habilitarBarbero("999"))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<UsuarioDTO>> response = usuarioController.habilitarBarbero("999");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
