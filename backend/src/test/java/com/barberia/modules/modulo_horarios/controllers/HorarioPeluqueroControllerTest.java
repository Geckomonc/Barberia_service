package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.dto.ActualizarHorarioPeluqueroDTO;
import com.barberia.modules.modulo_horarios.models.HorarioPeluquero;
import com.barberia.modules.modulo_horarios.services.HorarioPeluqueroService;
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
 * Pruebas unitarias para {@link HorarioPeluqueroController} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class HorarioPeluqueroControllerTest {

    @Mock
    private HorarioPeluqueroService horarioPeluqueroService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HorarioPeluqueroController horarioPeluqueroController;

    private ActualizarHorarioPeluqueroDTO dtoValido;

    @BeforeEach
    void setUp() {
        dtoValido = new ActualizarHorarioPeluqueroDTO();
        dtoValido.setIdDia(1L);
        dtoValido.setHoraInicioHorario("09:00:00");
        dtoValido.setHoraFinHorario("17:00:00");
    }

    @Test
    @DisplayName("actualizarHorario retorna 200 OK cuando los datos son válidos (numeroDocumento desde Map)")
    void actualizarHorario_conNumeroDocumentoEnMap_retorna200() {
        // Arrange
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));
        doNothing().when(horarioPeluqueroService)
                .actualizarHorario(eq("123"), any(ActualizarHorarioPeluqueroDTO.class));

        // Act
        ResponseEntity<?> response = horarioPeluqueroController.actualizarHorario(dtoValido, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Horario actualizado correctamente", response.getBody());
        verify(horarioPeluqueroService).actualizarHorario(eq("123"), any(ActualizarHorarioPeluqueroDTO.class));
    }

    @Test
    @DisplayName("actualizarHorario usa authentication.getName() cuando no hay Map en details")
    void actualizarHorario_sinDetailsMap_usaAuthenticationName() {
        // Arrange
        when(authentication.getDetails()).thenReturn(null);
        when(authentication.getName()).thenReturn("456");
        doNothing().when(horarioPeluqueroService)
                .actualizarHorario(eq("456"), any(ActualizarHorarioPeluqueroDTO.class));

        // Act
        ResponseEntity<?> response = horarioPeluqueroController.actualizarHorario(dtoValido, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(horarioPeluqueroService).actualizarHorario(eq("456"), any(ActualizarHorarioPeluqueroDTO.class));
    }

    @Test
    @DisplayName("obtenerHorario retorna 200 OK con la lista del peluquero")
    void obtenerHorario_retornaListaDelPeluquero() {
        // Arrange
        HorarioPeluquero horario = new HorarioPeluquero();
        horario.setIdDia(1L);
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));
        when(horarioPeluqueroService.obtenerHorario("123"))
                .thenReturn(Collections.singletonList(horario));

        // Act
        ResponseEntity<List<HorarioPeluquero>> response =
                horarioPeluqueroController.obtenerHorario(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("obtenerHorario retorna 200 OK con lista vacía cuando el peluquero no tiene horarios")
    void obtenerHorario_sinHorarios_retornaListaVacia() {
        // Arrange
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "999"));
        when(horarioPeluqueroService.obtenerHorario("999")).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<HorarioPeluquero>> response =
                horarioPeluqueroController.obtenerHorario(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}
