package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link CitaController} siguiendo el patrón AAA.
 *
 * Los endpoints usan HATEOAS (linkTo + methodOn), que requieren un contexto
 * de request para construir URIs. Se bindea un MockHttpServletRequest en
 * RequestContextHolder antes de cada prueba.
 */
@ExtendWith(MockitoExtension.class)
class CitaControllerTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CitaController citaController;

    private Cita cita;

    @BeforeEach
    void setUp() {
        // Bindear request context para que HATEOAS pueda construir URIs
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/citas");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        cita = Cita.builder()
                .noCita(1L)
                .numeroDocumentoCliente("100")
                .numeroDocumentoPeluquero("200")
                .idServicio(10L)
                .idEstado(1L)
                .build();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // ============================================================
    // obtenerTodas
    // ============================================================
    @Test
    @DisplayName("obtenerTodas retorna 200 OK con la lista de citas")
    void obtenerTodas_conCitas_retornaLista() {
        // Arrange
        when(citaRepository.findAll()).thenReturn(Arrays.asList(cita));

        // Act
        ResponseEntity<ApiResponse<List<Cita>>> response = citaController.obtenerTodas();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    @DisplayName("obtenerTodas retorna 200 OK con lista vacía cuando no hay citas")
    void obtenerTodas_sinCitas_retornaListaVacia() {
        // Arrange
        when(citaRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse<List<Cita>>> response = citaController.obtenerTodas();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().isEmpty());
    }

    // ============================================================
    // obtenerPorId
    // ============================================================
    @Test
    @DisplayName("obtenerPorId retorna 200 OK con EntityModel cuando la cita existe")
    void obtenerPorId_conIdValido_retorna200ConEntityModel() {
        // Arrange
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        // Act
        ResponseEntity<ApiResponse<EntityModel<Cita>>> response = citaController.obtenerPorId(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData().getContent());
        assertEquals(1L, response.getBody().getData().getContent().getNoCita());
    }

    @Test
    @DisplayName("obtenerPorId retorna 404 NOT_FOUND cuando la cita no existe")
    void obtenerPorId_conIdInexistente_retorna404() {
        // Arrange
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<EntityModel<Cita>>> response = citaController.obtenerPorId(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    // ============================================================
    // historialClienteAdmin
    // ============================================================
    @Test
    @DisplayName("historialClienteAdmin retorna 200 OK con CollectionModel del historial")
    void historialClienteAdmin_conCitas_retornaCollectionModel() {
        // Arrange
        when(citaRepository.findByNumeroDocumentoCliente("100"))
                .thenReturn(Arrays.asList(cita));

        // Act
        ResponseEntity<ApiResponse<CollectionModel<EntityModel<Cita>>>> response =
                citaController.historialClienteAdmin("100");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getContent().size());
    }

    @Test
    @DisplayName("historialClienteAdmin retorna 200 OK con colección vacía cuando no hay citas")
    void historialClienteAdmin_sinCitas_retornaColeccionVacia() {
        // Arrange
        when(citaRepository.findByNumeroDocumentoCliente("999"))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse<CollectionModel<EntityModel<Cita>>>> response =
                citaController.historialClienteAdmin("999");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().getContent().isEmpty());
    }

    // ============================================================
    // miHistorial
    // ============================================================
    @Test
    @DisplayName("miHistorial retorna 200 OK usando numeroDocumento del Map en details")
    void miHistorial_conNumeroDocumentoEnMap_retorna200() {
        // Arrange
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "100"));
        when(citaRepository.findByNumeroDocumentoCliente("100"))
                .thenReturn(Arrays.asList(cita));

        // Act
        ResponseEntity<ApiResponse<CollectionModel<EntityModel<Cita>>>> response =
                citaController.miHistorial(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().getContent().size());
        verify(citaRepository).findByNumeroDocumentoCliente("100");
    }

    @Test
    @DisplayName("miHistorial usa authentication.getName() cuando no hay Map en details")
    void miHistorial_sinDetailsMap_usaAuthenticationName() {
        // Arrange
        when(authentication.getDetails()).thenReturn(null);
        when(authentication.getName()).thenReturn("100");
        when(citaRepository.findByNumeroDocumentoCliente("100"))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse<CollectionModel<EntityModel<Cita>>>> response =
                citaController.miHistorial(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(citaRepository).findByNumeroDocumentoCliente("100");
    }
}
