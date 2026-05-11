package com.barberia.shared.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound_DeberiaRetornarNotFoundConBodyCorrecto() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Recurso no encontrado");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleResourceNotFound(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Map<String, Object> body = response.getBody();

        assertNotNull(body);
        assertInstanceOf(LocalDateTime.class, body.get("timestamp"));
        assertEquals(404, body.get("status"));
        assertEquals("Recurso no encontrado", body.get("error"));
        assertEquals("Recurso no encontrado", body.get("message"));
    }

    @Test
    void handleGeneralException_DeberiaRetornarInternalServerErrorConBodyCorrecto() {
        Exception exception = new Exception("Error inesperado");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGeneralException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = response.getBody();

        assertNotNull(body);
        assertInstanceOf(LocalDateTime.class, body.get("timestamp"));
        assertEquals(500, body.get("status"));
        assertEquals("Error interno del servidor", body.get("error"));
        assertEquals("Error inesperado", body.get("message"));
    }
}