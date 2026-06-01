package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.models.dtos.HorarioNegocioDTO;
import com.barberia.modules.modulo_horarios.models.dtos.HorarioUpdateDTO;
import com.barberia.modules.modulo_horarios.services.HorarioNegocioService;
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

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link HorarioNegocioController} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class HorarioNegocioControllerTest {

    @Mock
    private HorarioNegocioService horarioNegocioService;

    @InjectMocks
    private HorarioNegocioController horarioNegocioController;

    private HorarioNegocioDTO horarioDTO;

    @BeforeEach
    void setUp() {
        horarioDTO = HorarioNegocioDTO.builder()
                .idHorarioNegocio(1L)
                .idDia(1L)
                .horaApertura(LocalTime.of(8, 0))
                .horaCierre(LocalTime.of(18, 0))
                .localAbierto(true)
                .build();
    }

    @Test
    @DisplayName("obtenerTodos retorna 200 OK con la lista")
    void obtenerTodos_retornaListaHorarios() {
        // Arrange
        when(horarioNegocioService.obtenerTodos()).thenReturn(Collections.singletonList(horarioDTO));

        // Act
        ResponseEntity<ApiResponse<List<HorarioNegocioDTO>>> response = horarioNegocioController.obtenerTodos();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    @DisplayName("obtenerPorDia retorna 200 OK cuando el horario existe")
    void obtenerPorDia_conIdValido_retorna200() {
        // Arrange
        when(horarioNegocioService.obtenerPorDia(1L)).thenReturn(horarioDTO);

        // Act
        ResponseEntity<ApiResponse<HorarioNegocioDTO>> response = horarioNegocioController.obtenerPorDia(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getData().getIdDia());
    }

    @Test
    @DisplayName("obtenerPorDia retorna 404 NOT_FOUND cuando no existe")
    void obtenerPorDia_conIdInexistente_retorna404() {
        // Arrange
        when(horarioNegocioService.obtenerPorDia(99L))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<HorarioNegocioDTO>> response = horarioNegocioController.obtenerPorDia(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("actualizarHorario retorna 200 OK cuando es exitoso")
    void actualizarHorario_conDatosValidos_retorna200() {
        // Arrange
        HorarioUpdateDTO dto = new HorarioUpdateDTO("09:00:00", "19:00:00");
        when(horarioNegocioService.actualizarHorario(eq(1L), any(HorarioUpdateDTO.class)))
                .thenReturn(horarioDTO);

        // Act
        ResponseEntity<ApiResponse<HorarioNegocioDTO>> response =
                horarioNegocioController.actualizarHorario(1L, dto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("actualizarHorario retorna 400 BAD_REQUEST con IllegalArgumentException")
    void actualizarHorario_conIllegalArgument_retorna400() {
        // Arrange
        HorarioUpdateDTO dto = new HorarioUpdateDTO(null, null);
        when(horarioNegocioService.actualizarHorario(eq(1L), any(HorarioUpdateDTO.class)))
                .thenThrow(new IllegalArgumentException("Hora inválida"));

        // Act
        ResponseEntity<ApiResponse<HorarioNegocioDTO>> response =
                horarioNegocioController.actualizarHorario(1L, dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("actualizarHorario retorna 404 NOT_FOUND cuando el día no existe")
    void actualizarHorario_conDiaInexistente_retorna404() {
        // Arrange
        HorarioUpdateDTO dto = new HorarioUpdateDTO("09:00:00", "19:00:00");
        when(horarioNegocioService.actualizarHorario(eq(99L), any(HorarioUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<HorarioNegocioDTO>> response =
                horarioNegocioController.actualizarHorario(99L, dto);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("cerrarDia retorna 200 OK cuando es exitoso")
    void cerrarDia_conIdValido_retorna200() {
        // Arrange
        when(horarioNegocioService.cerrarDia(1L)).thenReturn(horarioDTO);

        // Act
        ResponseEntity<ApiResponse<HorarioNegocioDTO>> response = horarioNegocioController.cerrarDia(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("cerrarDia retorna 404 NOT_FOUND cuando el día no existe")
    void cerrarDia_conIdInexistente_retorna404() {
        // Arrange
        when(horarioNegocioService.cerrarDia(99L))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<HorarioNegocioDTO>> response = horarioNegocioController.cerrarDia(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
