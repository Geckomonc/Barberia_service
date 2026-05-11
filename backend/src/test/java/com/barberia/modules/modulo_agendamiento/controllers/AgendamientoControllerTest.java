package com.barberia.modules.modulo_agendamiento.controllers;

import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadResponseDTO;
import com.barberia.modules.modulo_agendamiento.services.CitaAgendamientoService;
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
import static org.mockito.Mockito.never;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.barberia.shared.exceptions.GlobalExceptionHandler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AgendamientoControllerTest {

    @Mock
    private CitaAgendamientoService citaAgendamientoService;

    @InjectMocks
    private AgendamientoController agendamientoController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(agendamientoController)
                .setControllerAdvice(new GlobalExceptionHandler())   // <-- añadir
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void consultarDisponibilidad_DeberiaRetornarOk() throws Exception {
        when(citaAgendamientoService.consultarDisponibilidad(any())).thenReturn(crearDisponibilidadResponse());

        String body = """
                {
                    "numeroDocumentoPeluquero": "222222222",
                    "idServicio": 1,
                    "fechaCita": "2026-05-04",
                    "horaInicioCita": "09:00:00",
                    "horaFinCita": "10:00:00"
                }
                """;

        mockMvc.perform(post("/api/v1/agendamiento/disponibilidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(citaAgendamientoService).consultarDisponibilidad(any());
    }

    @Test
    void consultarDisponibilidad_DeberiaRetornarBadRequestCuandoBodyEsInvalido() throws Exception {
        String body = "{}";

        mockMvc.perform(post("/api/v1/agendamiento/disponibilidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(citaAgendamientoService, never()).consultarDisponibilidad(any());
    }

    @Test
    void agendar_DeberiaRetornarCreated() throws Exception {
        Authentication authentication = crearAuthentication("111111111");

        when(citaAgendamientoService.agendar(any(), eq("111111111"))).thenReturn(crearCitaDTO());

        String body = """
                {
                    "numeroDocumentoPeluquero": "222222222",
                    "idServicio": 1,
                    "fechaCita": "2026-05-04",
                    "horaInicioCita": "09:00:00",
                    "horaFinCita": "10:00:00"
                }
                """;

        mockMvc.perform(post("/api/v1/agendamiento/agendar")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(citaAgendamientoService).agendar(any(), eq("111111111"));
    }

    @Test
    void agendar_DeberiaRetornarBadRequest() throws Exception {
        Authentication authentication = crearAuthentication("111111111");

        when(citaAgendamientoService.agendar(any(), eq("111111111")))
                .thenThrow(new IllegalArgumentException("No se puede agendar: existe una cita solapada"));

        String body = """
                {
                    "numeroDocumentoPeluquero": "222222222",
                    "idServicio": 1,
                    "fechaCita": "2026-05-04",
                    "horaInicioCita": "09:00:00",
                    "horaFinCita": "10:00:00"
                }
                """;

        mockMvc.perform(post("/api/v1/agendamiento/agendar")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(citaAgendamientoService).agendar(any(), eq("111111111"));
    }

    @Test
    void cancelar_DeberiaRetornarOk() throws Exception {
        when(citaAgendamientoService.cancelar(10L)).thenReturn(crearCitaDTO());

        mockMvc.perform(post("/api/v1/agendamiento/cancelar/10"))
                .andExpect(status().isOk());

        verify(citaAgendamientoService).cancelar(10L);
    }

    @Test
    void cancelar_DeberiaRetornarNotFound() throws Exception {
        when(citaAgendamientoService.cancelar(99L))
                .thenThrow(new ResourceNotFoundException("Cita no encontrada para noCita: 99"));

        mockMvc.perform(post("/api/v1/agendamiento/cancelar/99"))
                .andExpect(status().isNotFound());

        verify(citaAgendamientoService).cancelar(99L);
    }

    @Test
    void confirmar_DeberiaRetornarOk() throws Exception {
        Authentication authentication = crearAuthentication("222222222");

        when(citaAgendamientoService.confirmar(10L, "222222222")).thenReturn(crearCitaDTO());

        mockMvc.perform(put("/api/v1/agendamiento/confirmar/10")
                        .principal(authentication))
                .andExpect(status().isOk());

        verify(citaAgendamientoService).confirmar(10L, "222222222");
    }

    @Test
    void confirmar_DeberiaRetornarBadRequest() throws Exception {
        Authentication authentication = crearAuthentication("999999999");

        when(citaAgendamientoService.confirmar(10L, "999999999"))
                .thenThrow(new IllegalArgumentException("No puedes confirmar una cita que no te pertenece"));

        mockMvc.perform(put("/api/v1/agendamiento/confirmar/10")
                        .principal(authentication))
                .andExpect(status().isBadRequest());

        verify(citaAgendamientoService).confirmar(10L, "999999999");
    }

    @Test
    void reprogramar_DeberiaRetornarOk() throws Exception {
        when(citaAgendamientoService.reprogramar(eq(10L), any())).thenReturn(crearCitaDTO());

        String body = """
                {
                    "fechaCita": "2026-05-04",
                    "horaInicioCita": "09:00:00",
                    "horaFinCita": "10:00:00"
                }
                """;

        mockMvc.perform(put("/api/v1/agendamiento/reprogramar/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(citaAgendamientoService).reprogramar(eq(10L), any());
    }

    @Test
    void reprogramar_DeberiaRetornarBadRequest() throws Exception {
        when(citaAgendamientoService.reprogramar(eq(10L), any()))
                .thenThrow(new IllegalArgumentException("No se puede reprogramar: existe una cita solapada"));

        String body = """
                {
                    "fechaCita": "2026-05-04",
                    "horaInicioCita": "09:00:00",
                    "horaFinCita": "10:00:00"
                }
                """;

        mockMvc.perform(put("/api/v1/agendamiento/reprogramar/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(citaAgendamientoService).reprogramar(eq(10L), any());
    }

    private Authentication crearAuthentication(String numeroDocumento) {
        Authentication authentication = mock(Authentication.class);

        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", numeroDocumento));

        return authentication;
    }

    private CitaDisponibilidadResponseDTO crearDisponibilidadResponse() {
        return CitaDisponibilidadResponseDTO.builder()
                .disponible(true)
                .mensaje("Disponible")
                .numeroDocumentoPeluquero("222222222")
                .idServicio(1L)
                .fechaCita(LocalDate.of(2026, 5, 4))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .build();
    }

    private CitaDTO crearCitaDTO() {
        return CitaDTO.builder()
                .noCita(10L)
                .numeroDocumentoCliente("111111111")
                .numeroDocumentoPeluquero("222222222")
                .idServicio(1L)
                .fechaCita(LocalDate.of(2026, 5, 4))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .idEstado(1L)
                .citaConfirmada(false)
                .fechaCreacion(Instant.parse("2026-05-04T12:00:00Z"))
                .build();
    }
}