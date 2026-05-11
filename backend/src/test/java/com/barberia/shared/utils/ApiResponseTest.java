package com.barberia.shared.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void success_DeberiaCrearRespuestaExitosaConData() {
        String data = "resultado";

        ApiResponse<String> response = ApiResponse.success("Operación exitosa", data);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Operación exitosa", response.getMessage());
        assertEquals("resultado", response.getData());
    }

    @Test
    void error_DeberiaCrearRespuestaDeErrorSinData() {
        ApiResponse<String> response = ApiResponse.error("Ocurrió un error");

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Ocurrió un error", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void of_DeberiaCrearRespuestaPersonalizada() {
        Integer data = 10;

        ApiResponse<Integer> response = ApiResponse.of(true, "Respuesta personalizada", data);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Respuesta personalizada", response.getMessage());
        assertEquals(10, response.getData());
    }

    @Test
    void constructorVacio_DeberiaPermitirAsignarValoresConSetters() {
        ApiResponse<String> response = new ApiResponse<>();

        response.setSuccess(true);
        response.setMessage("Mensaje");
        response.setData("Data");

        assertTrue(response.isSuccess());
        assertEquals("Mensaje", response.getMessage());
        assertEquals("Data", response.getData());
    }

    @Test
    void constructorCompleto_DeberiaAsignarTodosLosValores() {
        ApiResponse<String> response = new ApiResponse<>(true, "Mensaje completo", "Data completa");

        assertTrue(response.isSuccess());
        assertEquals("Mensaje completo", response.getMessage());
        assertEquals("Data completa", response.getData());
    }
}