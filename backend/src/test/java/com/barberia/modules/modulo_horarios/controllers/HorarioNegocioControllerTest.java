package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.models.dtos.HorarioNegocioDTO;
import com.barberia.modules.modulo_horarios.services.HorarioNegocioService;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
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

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HorarioNegocioControllerTest {

    @Mock
    private HorarioNegocioService horarioNegocioService;

    @InjectMocks
    private HorarioNegocioController horarioNegocioController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(horarioNegocioController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void obtenerTodos_DeberiaRetornarOk() throws Exception {
        when(horarioNegocioService.obtenerTodos()).thenReturn(List.of(crearHorarioDTO()));

        mockMvc.perform(get("/api/v1/horarios"))
                .andExpect(status().isOk());

        verify(horarioNegocioService).obtenerTodos();
    }

    @Test
    void obtenerPorDia_DeberiaRetornarOk() throws Exception {
        when(horarioNegocioService.obtenerPorDia(1L)).thenReturn(crearHorarioDTO());

        mockMvc.perform(get("/api/v1/horarios/buscar/1"))
                .andExpect(status().isOk());

        verify(horarioNegocioService).obtenerPorDia(1L);
    }

    @Test
    void obtenerPorDia_DeberiaRetornarNotFound() throws Exception {
        when(horarioNegocioService.obtenerPorDia(9L))
                .thenThrow(new ResourceNotFoundException("Horario no encontrado para el día: 9"));

        mockMvc.perform(get("/api/v1/horarios/buscar/9"))
                .andExpect(status().isNotFound());

        verify(horarioNegocioService).obtenerPorDia(9L);
    }

    @Test
    void actualizarHorario_DeberiaRetornarOk() throws Exception {
        when(horarioNegocioService.actualizarHorario(any(), any())).thenReturn(crearHorarioDTO());

        String body = """
                {
                    "horaApertura": "07:00:00",
                    "horaCierre": "18:00:00"
                }
                """;

        mockMvc.perform(put("/api/v1/horarios/actualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(horarioNegocioService).actualizarHorario(any(), any());
    }

    @Test
    void actualizarHorario_DeberiaRetornarBadRequest() throws Exception {
        when(horarioNegocioService.actualizarHorario(any(), any()))
                .thenThrow(new IllegalArgumentException("Error: La hora de cierre no puede ser anterior a la de apertura"));

        String body = """
                {
                    "horaApertura": "18:00:00",
                    "horaCierre": "07:00:00"
                }
                """;

        mockMvc.perform(put("/api/v1/horarios/actualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(horarioNegocioService).actualizarHorario(any(), any());
    }

    @Test
    void actualizarHorario_DeberiaRetornarNotFound() throws Exception {
        when(horarioNegocioService.actualizarHorario(any(), any()))
                .thenThrow(new ResourceNotFoundException("Horario no encontrado para el día: 9"));

        String body = """
                {
                    "horaApertura": "07:00:00",
                    "horaCierre": "18:00:00"
                }
                """;

        mockMvc.perform(put("/api/v1/horarios/actualizar/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        verify(horarioNegocioService).actualizarHorario(any(), any());
    }

    @Test
    void cerrarDia_DeberiaRetornarOk() throws Exception {
        when(horarioNegocioService.cerrarDia(1L)).thenReturn(crearHorarioDTO());

        mockMvc.perform(put("/api/v1/horarios/cerrar/1"))
                .andExpect(status().isOk());

        verify(horarioNegocioService).cerrarDia(1L);
    }

    @Test
    void cerrarDia_DeberiaRetornarNotFound() throws Exception {
        when(horarioNegocioService.cerrarDia(9L))
                .thenThrow(new ResourceNotFoundException("Horario no encontrado para el día: 9"));

        mockMvc.perform(put("/api/v1/horarios/cerrar/9"))
                .andExpect(status().isNotFound());

        verify(horarioNegocioService).cerrarDia(9L);
    }

    private HorarioNegocioDTO crearHorarioDTO() {
        return HorarioNegocioDTO.builder()
                .idHorarioNegocio(1L)
                .idDia(1L)
                .horaApertura(LocalTime.of(7, 0))
                .horaCierre(LocalTime.of(18, 0))
                .localAbierto(true)
                .build();
    }
}