package com.barberia.modules.modulo_agendamiento.controllers;

import com.barberia.modules.modulo_agendamiento.models.dtos.CancelarCitaDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaCreateDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadRequestDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadResponseDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaReprogramarDTO;
import com.barberia.modules.modulo_agendamiento.services.CitaAgendamientoService;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import com.barberia.shared.utils.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link AgendamientoController} siguiendo el patrón AAA.
 *
 * El endpoint agendar usa HATEOAS, por lo que se bindea un MockHttpServletRequest
 * para que linkTo + methodOn puedan construir URIs.
 */
@ExtendWith(MockitoExtension.class)
class AgendamientoControllerTest {

    @Mock
    private CitaAgendamientoService citaAgendamientoService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AgendamientoController agendamientoController;

    private CitaDTO citaDTO;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/agendamientos");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        citaDTO = CitaDTO.builder()
                .noCita(1L)
                .numeroDocumentoCliente("100")
                .numeroDocumentoPeluquero("200")
                .idServicio(10L)
                .fechaCita(LocalDate.of(2026, 6, 1))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .idEstado(1L)
                .citaConfirmada(false)
                .build();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // ============================================================
    // consultarDisponibilidad
    // ============================================================
    @Test
    @DisplayName("consultarDisponibilidad retorna 200 OK cuando el service responde exitosamente")
    void consultarDisponibilidad_conServiceExitoso_retorna200() {
        // Arrange
        CitaDisponibilidadRequestDTO request = new CitaDisponibilidadRequestDTO();
        CitaDisponibilidadResponseDTO serviceResponse = CitaDisponibilidadResponseDTO.builder()
                .disponible(true)
                .mensaje("Disponible")
                .build();
        when(citaAgendamientoService.consultarDisponibilidad(request)).thenReturn(serviceResponse);

        // Act
        ResponseEntity<ApiResponse<CitaDisponibilidadResponseDTO>> response =
                agendamientoController.consultarDisponibilidad(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData().isDisponible());
    }

    @Test
    @DisplayName("consultarDisponibilidad retorna 400 BAD_REQUEST con IllegalArgumentException")
    void consultarDisponibilidad_conIllegalArgument_retorna400() {
        // Arrange
        CitaDisponibilidadRequestDTO request = new CitaDisponibilidadRequestDTO();
        when(citaAgendamientoService.consultarDisponibilidad(request))
                .thenThrow(new IllegalArgumentException("Inválido"));

        // Act
        ResponseEntity<ApiResponse<CitaDisponibilidadResponseDTO>> response =
                agendamientoController.consultarDisponibilidad(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("consultarDisponibilidad retorna 500 INTERNAL_SERVER_ERROR ante excepción inesperada")
    void consultarDisponibilidad_conExcepcionInesperada_retorna500() {
        // Arrange
        CitaDisponibilidadRequestDTO request = new CitaDisponibilidadRequestDTO();
        when(citaAgendamientoService.consultarDisponibilidad(request))
                .thenThrow(new RuntimeException("Error inesperado"));

        // Act
        ResponseEntity<ApiResponse<CitaDisponibilidadResponseDTO>> response =
                agendamientoController.consultarDisponibilidad(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ============================================================
    // agendar
    // ============================================================
    @Test
    @DisplayName("agendar retorna 201 CREATED con EntityModel cuando es exitoso")
    void agendar_conDatosValidos_retorna201ConEntityModel() {
        // Arrange
        CitaCreateDTO request = new CitaCreateDTO();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(citaAgendamientoService.agendar(eq(request), eq("100"))).thenReturn(citaDTO);

        // Act
        ResponseEntity<ApiResponse<EntityModel<CitaDTO>>> response =
                agendamientoController.agendar(request, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1L, response.getBody().getData().getContent().getNoCita());
    }

    @Test
    @DisplayName("agendar retorna 400 BAD_REQUEST con IllegalArgumentException")
    void agendar_conIllegalArgument_retorna400() {
        // Arrange
        CitaCreateDTO request = new CitaCreateDTO();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(citaAgendamientoService.agendar(any(CitaCreateDTO.class), anyString()))
                .thenThrow(new IllegalArgumentException("Conflicto"));

        // Act
        ResponseEntity<ApiResponse<EntityModel<CitaDTO>>> response =
                agendamientoController.agendar(request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("agendar retorna 500 INTERNAL_SERVER_ERROR ante excepción inesperada")
    void agendar_conExcepcionInesperada_retorna500() {
        // Arrange
        CitaCreateDTO request = new CitaCreateDTO();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(citaAgendamientoService.agendar(any(CitaCreateDTO.class), anyString()))
                .thenThrow(new RuntimeException("Error inesperado"));

        // Act
        ResponseEntity<ApiResponse<EntityModel<CitaDTO>>> response =
                agendamientoController.agendar(request, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("agendar usa authentication.getName() cuando no hay Map en details")
    void agendar_sinDetailsMap_usaAuthenticationName() {
        // Arrange
        CitaCreateDTO request = new CitaCreateDTO();
        when(authentication.getDetails()).thenReturn(null);
        when(authentication.getName()).thenReturn("100");
        when(citaAgendamientoService.agendar(eq(request), eq("100"))).thenReturn(citaDTO);

        // Act
        ResponseEntity<ApiResponse<EntityModel<CitaDTO>>> response =
                agendamientoController.agendar(request, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(citaAgendamientoService).agendar(request, "100");
    }

    // ============================================================
    // cancelar
    // ============================================================
    @Test
    @DisplayName("cancelar retorna 200 OK cuando es exitoso")
    void cancelar_conDatosValidos_retorna200() {
        // Arrange
        CancelarCitaDTO request = CancelarCitaDTO.builder().motivoCancelacion("No puedo").build();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(citaAgendamientoService.cancelar(eq(1L), eq("No puedo"), eq("100")))
                .thenReturn(citaDTO);

        // Act
        ResponseEntity<ApiResponse<CitaDTO>> response =
                agendamientoController.cancelar(1L, request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("cancelar retorna 400 BAD_REQUEST con IllegalArgumentException")
    void cancelar_conIllegalArgument_retorna400() {
        // Arrange
        CancelarCitaDTO request = CancelarCitaDTO.builder().motivoCancelacion(" ").build();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(citaAgendamientoService.cancelar(anyLong(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Motivo requerido"));

        // Act
        ResponseEntity<ApiResponse<CitaDTO>> response =
                agendamientoController.cancelar(1L, request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("cancelar retorna 404 NOT_FOUND cuando la cita no existe")
    void cancelar_conCitaInexistente_retorna404() {
        // Arrange
        CancelarCitaDTO request = CancelarCitaDTO.builder().motivoCancelacion("motivo").build();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(citaAgendamientoService.cancelar(anyLong(), anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<CitaDTO>> response =
                agendamientoController.cancelar(99L, request, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ============================================================
    // confirmar
    // ============================================================
    @Test
    @DisplayName("confirmar retorna 200 OK cuando es exitoso")
    void confirmar_conDatosValidos_retorna200() {
        // Arrange
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "200"));
        when(citaAgendamientoService.confirmar(1L, "200")).thenReturn(citaDTO);

        // Act
        ResponseEntity<ApiResponse<CitaDTO>> response =
                agendamientoController.confirmar(1L, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("confirmar retorna 400 BAD_REQUEST con IllegalArgumentException")
    void confirmar_conIllegalArgument_retorna400() {
        // Arrange
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "200"));
        when(citaAgendamientoService.confirmar(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Cita no activa"));

        // Act
        ResponseEntity<ApiResponse<CitaDTO>> response =
                agendamientoController.confirmar(1L, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("confirmar retorna 404 NOT_FOUND cuando la cita no existe")
    void confirmar_conCitaInexistente_retorna404() {
        // Arrange
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "200"));
        when(citaAgendamientoService.confirmar(anyLong(), anyString()))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<CitaDTO>> response =
                agendamientoController.confirmar(99L, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ============================================================
    // reprogramar
    // ============================================================
    @Test
    @DisplayName("reprogramar retorna 200 OK cuando es exitoso")
    void reprogramar_conDatosValidos_retorna200() {
        // Arrange
        CitaReprogramarDTO request = CitaReprogramarDTO.builder()
                .fechaCita(LocalDate.of(2026, 6, 2))
                .horaInicioCita(LocalTime.of(11, 0))
                .horaFinCita(LocalTime.of(12, 0))
                .build();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(citaAgendamientoService.reprogramar(eq(1L), eq(request), eq("100")))
                .thenReturn(citaDTO);

        // Act
        ResponseEntity<ApiResponse<CitaDTO>> response =
                agendamientoController.reprogramar(1L, request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("reprogramar retorna 400 BAD_REQUEST con IllegalArgumentException")
    void reprogramar_conIllegalArgument_retorna400() {
        // Arrange
        CitaReprogramarDTO request = new CitaReprogramarDTO();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(citaAgendamientoService.reprogramar(anyLong(), any(CitaReprogramarDTO.class), anyString()))
                .thenThrow(new IllegalArgumentException("Conflicto"));

        // Act
        ResponseEntity<ApiResponse<CitaDTO>> response =
                agendamientoController.reprogramar(1L, request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("reprogramar retorna 404 NOT_FOUND cuando la cita no existe")
    void reprogramar_conCitaInexistente_retorna404() {
        // Arrange
        CitaReprogramarDTO request = new CitaReprogramarDTO();
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(citaAgendamientoService.reprogramar(anyLong(), any(CitaReprogramarDTO.class), anyString()))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<CitaDTO>> response =
                agendamientoController.reprogramar(99L, request, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
