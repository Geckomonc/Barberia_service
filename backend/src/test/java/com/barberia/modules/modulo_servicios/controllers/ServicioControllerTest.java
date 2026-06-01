package com.barberia.modules.modulo_servicios.controllers;

import com.barberia.modules.modulo_servicios.models.dtos.ServicioDTO;
import com.barberia.modules.modulo_servicios.services.ServicioService;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link ServicioController} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class ServicioControllerTest {

    @Mock
    private ServicioService servicioService;

    @InjectMocks
    private ServicioController servicioController;

    private ServicioDTO servicioDTO;

    @BeforeEach
    void setUp() {
        servicioDTO = ServicioDTO.builder()
                .idServicio(1L)
                .nombreServicio("Corte de cabello")
                .descripcion("Corte clásico")
                .duracion(30)
                .costo(new BigDecimal("20000"))
                .idEstado(1L)
                .build();
    }

    // ============================================================
    // obtenerTodos
    // ============================================================
    @Test
    @DisplayName("obtenerTodos retorna 200 OK con la lista de servicios")
    void obtenerTodos_retornaListaServicios() {
        // Arrange
        when(servicioService.obtenerTodos()).thenReturn(Collections.singletonList(servicioDTO));

        // Act
        ResponseEntity<ApiResponse<List<ServicioDTO>>> response = servicioController.obtenerTodos();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    @DisplayName("obtenerTodos retorna 200 OK con lista vacía cuando no hay servicios")
    void obtenerTodos_sinServicios_retornaListaVacia() {
        // Arrange
        when(servicioService.obtenerTodos()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse<List<ServicioDTO>>> response = servicioController.obtenerTodos();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().isEmpty());
    }

    // ============================================================
    // obtenerPorId
    // ============================================================
    @Test
    @DisplayName("obtenerPorId retorna 200 OK cuando el servicio existe")
    void obtenerPorId_conIdValido_retorna200() {
        // Arrange
        when(servicioService.obtenerPorId(1L)).thenReturn(servicioDTO);

        // Act
        ResponseEntity<ApiResponse<ServicioDTO>> response = servicioController.obtenerPorId(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getData().getIdServicio());
    }

    @Test
    @DisplayName("obtenerPorId retorna 404 NOT_FOUND cuando el servicio no existe")
    void obtenerPorId_conIdInexistente_retorna404() {
        // Arrange
        when(servicioService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Servicio no encontrado con id: 99"));

        // Act
        ResponseEntity<ApiResponse<ServicioDTO>> response = servicioController.obtenerPorId(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    // ============================================================
    // crear
    // ============================================================
    @Test
    @DisplayName("crear retorna 201 CREATED cuando el servicio es válido")
    void crear_conDatosValidos_retorna201() {
        // Arrange
        when(servicioService.crear(any(ServicioDTO.class))).thenReturn(servicioDTO);

        // Act
        ResponseEntity<ApiResponse<ServicioDTO>> response = servicioController.crear(servicioDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Corte de cabello", response.getBody().getData().getNombreServicio());
    }

    @Test
    @DisplayName("crear retorna 400 BAD_REQUEST ante cualquier excepción")
    void crear_conError_retorna400() {
        // Arrange
        when(servicioService.crear(any(ServicioDTO.class)))
                .thenThrow(new RuntimeException("Datos inválidos"));

        // Act
        ResponseEntity<ApiResponse<ServicioDTO>> response = servicioController.crear(servicioDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    // ============================================================
    // actualizar
    // ============================================================
    @Test
    @DisplayName("actualizar retorna 200 OK cuando es exitoso")
    void actualizar_conDatosValidos_retorna200() {
        // Arrange
        when(servicioService.actualizar(eq(1L), any(ServicioDTO.class))).thenReturn(servicioDTO);

        // Act
        ResponseEntity<ApiResponse<ServicioDTO>> response = servicioController.actualizar(1L, servicioDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("actualizar retorna 400 BAD_REQUEST cuando el servicio no existe")
    void actualizar_conIdInexistente_retorna400() {
        // Arrange
        when(servicioService.actualizar(eq(99L), any(ServicioDTO.class)))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<ServicioDTO>> response = servicioController.actualizar(99L, servicioDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ============================================================
    // eliminar
    // ============================================================
    @Test
    @DisplayName("eliminar retorna 200 OK cuando es exitoso")
    void eliminar_conIdValido_retorna200() {
        // Arrange
        doNothing().when(servicioService).eliminar(1L);

        // Act
        ResponseEntity<ApiResponse<Void>> response = servicioController.eliminar(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(servicioService).eliminar(1L);
    }

    @Test
    @DisplayName("eliminar retorna 400 BAD_REQUEST cuando hay citas activas")
    void eliminar_conCitasActivas_retorna400() {
        // Arrange
        doThrow(new IllegalStateException("Tiene citas activas")).when(servicioService).eliminar(1L);

        // Act
        ResponseEntity<ApiResponse<Void>> response = servicioController.eliminar(1L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("eliminar retorna 400 BAD_REQUEST cuando el servicio no existe")
    void eliminar_conIdInexistente_retorna400() {
        // Arrange
        doThrow(new ResourceNotFoundException("No existe")).when(servicioService).eliminar(99L);

        // Act
        ResponseEntity<ApiResponse<Void>> response = servicioController.eliminar(99L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ============================================================
    // deshabilitar
    // ============================================================
    @Test
    @DisplayName("deshabilitar retorna 200 OK cuando es exitoso")
    void deshabilitar_conIdValido_retorna200() {
        // Arrange
        when(servicioService.deshabilitar(1L)).thenReturn(servicioDTO);

        // Act
        ResponseEntity<ApiResponse<ServicioDTO>> response = servicioController.deshabilitar(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("deshabilitar retorna 400 BAD_REQUEST cuando el servicio no existe")
    void deshabilitar_conIdInexistente_retorna400() {
        // Arrange
        when(servicioService.deshabilitar(99L))
                .thenThrow(new ResourceNotFoundException("No existe"));

        // Act
        ResponseEntity<ApiResponse<ServicioDTO>> response = servicioController.deshabilitar(99L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
