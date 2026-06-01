package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.dto.ActualizarEspecialidadesPeluqueroDTO;
import com.barberia.modules.modulo_horarios.services.EspecialidadesPeluqueroService;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link EspecialidadesPeluqueroController} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class EspecialidadesPeluqueroControllerTest {

    @Mock
    private EspecialidadesPeluqueroService especialidadesPeluqueroService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EspecialidadesPeluqueroController especialidadesPeluqueroController;

    private ActualizarEspecialidadesPeluqueroDTO dto;

    @BeforeEach
    void setUp() {
        dto = new ActualizarEspecialidadesPeluqueroDTO();
        dto.setIdServicio(10L);
    }

    @Test
    @DisplayName("actualizarEspecialidades retorna 200 OK cuando el documento viene del Map")
    void actualizarEspecialidades_conNumeroDocumentoEnMap_retorna200() {
        // Arrange
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));
        doNothing().when(especialidadesPeluqueroService).asociarServicio("123", 10L);

        // Act
        ResponseEntity<?> response =
                especialidadesPeluqueroController.actualizarEspecialidades(dto, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Servicio asociado correctamente al peluquero", response.getBody());
        verify(especialidadesPeluqueroService).asociarServicio("123", 10L);
    }

    @Test
    @DisplayName("actualizarEspecialidades usa authentication.getName() cuando no hay Map en details")
    void actualizarEspecialidades_sinDetailsMap_usaAuthenticationName() {
        // Arrange
        when(authentication.getDetails()).thenReturn(null);
        when(authentication.getName()).thenReturn("456");
        doNothing().when(especialidadesPeluqueroService).asociarServicio("456", 10L);

        // Act
        ResponseEntity<?> response =
                especialidadesPeluqueroController.actualizarEspecialidades(dto, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(especialidadesPeluqueroService).asociarServicio("456", 10L);
    }

    @Test
    @DisplayName("actualizarEspecialidades invoca el service exactamente una vez")
    void actualizarEspecialidades_invocaServiceExactamenteUnaVez() {
        // Arrange
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));
        doNothing().when(especialidadesPeluqueroService).asociarServicio("123", 10L);

        // Act
        especialidadesPeluqueroController.actualizarEspecialidades(dto, authentication);

        // Assert
        verify(especialidadesPeluqueroService, times(1)).asociarServicio("123", 10L);
    }
}
