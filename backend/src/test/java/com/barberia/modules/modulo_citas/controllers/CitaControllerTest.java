package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CitaControllerTest {

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private CitaController citaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(citaController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void obtenerTodas_DeberiaRetornarOk() throws Exception {
        when(citaRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/citas"))
                .andExpect(status().isOk());

        verify(citaRepository).findAll();
    }

    @Test
    void obtenerPorId_DeberiaRetornarOkCuandoExiste() throws Exception {
        Cita cita = mock(Cita.class);

        when(citaRepository.findById(10L)).thenReturn(Optional.of(cita));

        mockMvc.perform(get("/api/v1/citas/buscar/10"))
                .andExpect(status().isOk());

        verify(citaRepository).findById(10L);
    }

    @Test
    void obtenerPorId_DeberiaRetornarNotFoundCuandoNoExiste() throws Exception {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/citas/buscar/99"))
                .andExpect(status().isNotFound());

        verify(citaRepository).findById(99L);
    }
}