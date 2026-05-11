package com.barberia.modules.modulo_servicios.controllers;

import com.barberia.modules.modulo_servicios.models.dtos.ServicioDTO;
import com.barberia.modules.modulo_servicios.services.ServicioService;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServicioControllerTest {

    @Mock
    private ServicioService servicioService;

    @InjectMocks
    private ServicioController servicioController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(servicioController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void obtenerTodos_DeberiaRetornarOk() throws Exception {
        when(servicioService.obtenerTodos()).thenReturn(List.of(crearServicioDTO()));

        mockMvc.perform(get("/api/v1/servicios"))
                .andExpect(status().isOk());

        verify(servicioService).obtenerTodos();
    }

    @Test
    void obtenerPorId_DeberiaRetornarOk() throws Exception {
        when(servicioService.obtenerPorId(1L)).thenReturn(crearServicioDTO());

        mockMvc.perform(get("/api/v1/servicios/buscar/1"))
                .andExpect(status().isOk());

        verify(servicioService).obtenerPorId(1L);
    }

    @Test
    void obtenerPorId_DeberiaRetornarNotFound() throws Exception {
        when(servicioService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Servicio no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/servicios/buscar/99"))
                .andExpect(status().isNotFound());

        verify(servicioService).obtenerPorId(99L);
    }

    @Test
    void crear_DeberiaRetornarCreated() throws Exception {
        when(servicioService.crear(any())).thenReturn(crearServicioDTO());

        String body = """
                {
                    "nombreServicio": "Corte clásico",
                    "descripcion": "Corte de cabello tradicional"
                }
                """;

        mockMvc.perform(post("/api/v1/servicios/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(servicioService).crear(any());
    }

    @Test
    void crear_DeberiaRetornarBadRequest() throws Exception {
        when(servicioService.crear(any()))
                .thenThrow(new IllegalArgumentException("Error al crear servicio"));

        String body = """
                {
                    "nombreServicio": "Corte clásico"
                }
                """;

        mockMvc.perform(post("/api/v1/servicios/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(servicioService).crear(any());
    }

    private ServicioDTO crearServicioDTO() {
        return ServicioDTO.builder()
                .idServicio(1L)
                .nombreServicio("Corte clásico")
                .descripcion("Corte de cabello tradicional")
                .idEstado(1L)
                .build();
    }
}