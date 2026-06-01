package com.barberia.modules.modulo_agenda.controllers;

import com.barberia.modules.modulo_agenda.models.dtos.AgendaResponseDTO;
import com.barberia.modules.modulo_agenda.services.AgendaService;
import com.barberia.shared.utils.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link AgendaController} siguiendo el patrón AAA.
 *
 * El controller usa HATEOAS, por lo que se bindea un MockHttpServletRequest
 * para que linkTo + methodOn puedan construir URIs.
 */
@ExtendWith(MockitoExtension.class)
class AgendaControllerTest {

    @Mock
    private AgendaService agendaService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AgendaController agendaController;

    private AgendaResponseDTO agendaDTO;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/agendas/mis-citas");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        agendaDTO = AgendaResponseDTO.builder()
                .noCita(1L)
                .fecha(LocalDate.of(2026, 6, 1))
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(10, 0))
                .nombreCliente("Juan Pérez")
                .idServicio(10L)
                .nombreServicio("Corte de cabello")
                .estado("ACTIVO")
                .build();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("miAgenda retorna 200 OK con CollectionModel cuando hay citas")
    void miAgenda_conCitas_retornaCollectionModel() {
        // Arrange
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "200"));
        when(agendaService.obtenerAgendaPeluquero("200")).thenReturn(Arrays.asList(agendaDTO));

        // Act
        ResponseEntity<ApiResponse<CollectionModel<EntityModel<AgendaResponseDTO>>>> response =
                agendaController.miAgenda(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().getContent().size());
    }

    @Test
    @DisplayName("miAgenda retorna 200 OK con colección vacía cuando el peluquero no tiene citas")
    void miAgenda_sinCitas_retornaColeccionVacia() {
        // Arrange
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "200"));
        when(agendaService.obtenerAgendaPeluquero("200")).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse<CollectionModel<EntityModel<AgendaResponseDTO>>>> response =
                agendaController.miAgenda(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().getContent().isEmpty());
    }

    @Test
    @DisplayName("miAgenda usa numeroDocumento desde Map en details")
    void miAgenda_conNumeroDocumentoEnMap_lopasaAlService() {
        // Arrange
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "200"));
        when(agendaService.obtenerAgendaPeluquero("200")).thenReturn(Collections.emptyList());

        // Act
        agendaController.miAgenda(authentication);

        // Assert
        verify(agendaService).obtenerAgendaPeluquero("200");
    }

    @Test
    @DisplayName("miAgenda usa authentication.getName() cuando no hay Map en details")
    void miAgenda_sinDetailsMap_usaAuthenticationName() {
        // Arrange
        when(authentication.getDetails()).thenReturn(null);
        when(authentication.getName()).thenReturn("999");
        when(agendaService.obtenerAgendaPeluquero("999")).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse<CollectionModel<EntityModel<AgendaResponseDTO>>>> response =
                agendaController.miAgenda(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(agendaService).obtenerAgendaPeluquero("999");
    }
}
