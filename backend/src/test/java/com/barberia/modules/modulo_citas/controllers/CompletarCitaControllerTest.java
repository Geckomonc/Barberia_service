package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.dto.CompletarCitaRequestDTO;
import com.barberia.modules.modulo_citas.dto.CompletarCitaResponseDTO;
import com.barberia.modules.modulo_citas.services.CompletarCitaService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link CompletarCitaController} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class CompletarCitaControllerTest {

    @Mock
    private CompletarCitaService completarCitaService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CompletarCitaController completarCitaController;

    private CompletarCitaRequestDTO request;
    private CompletarCitaResponseDTO response;

    @BeforeEach
    void setUp() {
        request = new CompletarCitaRequestDTO();
        request.setIdCita(1L);

        response = CompletarCitaResponseDTO.builder()
                .idCita(1L)
                .estadoAnterior("5")
                .estadoActual("COMPLETADA")
                .mensaje("Cita completada exitosamente")
                .build();
    }

    @Test
    @DisplayName("completarCita retorna 200 OK cuando el service ejecuta exitosamente")
    void completarCita_conServiceExitoso_retorna200() {
        // Arrange
        when(completarCitaService.completarCita(any(CompletarCitaRequestDTO.class), eq(authentication)))
                .thenReturn(response);

        // Act
        ResponseEntity<ApiResponse<CompletarCitaResponseDTO>> result =
                completarCitaController.completarCita(request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Cita completada", result.getBody().getMessage());
        assertEquals("COMPLETADA", result.getBody().getData().getEstadoActual());
    }

    @Test
    @DisplayName("completarCita invoca el service exactamente una vez")
    void completarCita_invocaServiceExactamenteUnaVez() {
        // Arrange
        when(completarCitaService.completarCita(any(CompletarCitaRequestDTO.class), eq(authentication)))
                .thenReturn(response);

        // Act
        completarCitaController.completarCita(request, authentication);

        // Assert
        verify(completarCitaService, times(1)).completarCita(request, authentication);
    }

    @Test
    @DisplayName("completarCita propaga IllegalArgumentException del service")
    void completarCita_conServiceLanzaIllegalArgument_propagaExcepcion() {
        // Arrange
        when(completarCitaService.completarCita(any(CompletarCitaRequestDTO.class), eq(authentication)))
                .thenThrow(new IllegalArgumentException("Cita no encontrada"));

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> completarCitaController.completarCita(request, authentication));
    }

    @Test
    @DisplayName("completarCita propaga SecurityException del service")
    void completarCita_conServiceLanzaSecurityException_propagaExcepcion() {
        // Arrange
        when(completarCitaService.completarCita(any(CompletarCitaRequestDTO.class), eq(authentication)))
                .thenThrow(new SecurityException("Sin permiso"));

        // Act + Assert
        assertThrows(SecurityException.class,
                () -> completarCitaController.completarCita(request, authentication));
    }
}
