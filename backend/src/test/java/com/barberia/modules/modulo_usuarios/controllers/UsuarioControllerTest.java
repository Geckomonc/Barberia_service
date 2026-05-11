package com.barberia.modules.modulo_usuarios.controllers;

import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.services.UsuarioService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(usuarioController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void registrarPersona_DeberiaRetornarCreated() throws Exception {
        UsuarioDTO usuarioDTO = crearUsuarioDTO();

        when(usuarioService.registrarPersona(any())).thenReturn(usuarioDTO);

        String body = """
                {
                    "numeroDocumento": "123456789",
                    "numeroCelular": "3001234567",
                    "email": "usuario@test.com",
                    "nombrePersona": "Usuario Prueba",
                    "contraseña": "123456",
                    "confirmarContraseña": "123456"
                }
                """;

        mockMvc.perform(post("/api/v1/personas/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(usuarioService).registrarPersona(any());
    }

    @Test
    void registrarPersona_DeberiaRetornarBadRequestCuandoHayError() throws Exception {
        when(usuarioService.registrarPersona(any()))
                .thenThrow(new IllegalArgumentException("Error: El email ya está registrado"));

        String body = """
                {
                    "numeroDocumento": "123456789",
                    "email": "usuario@test.com",
                    "contraseña": "123456",
                    "confirmarContraseña": "123456"
                }
                """;

        mockMvc.perform(post("/api/v1/personas/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(usuarioService).registrarPersona(any());
    }

    @Test
    void obtenerPorDocumento_DeberiaRetornarOk() throws Exception {
        when(usuarioService.obtenerPorNumeroDocumento("123456789")).thenReturn(crearUsuarioDTO());

        mockMvc.perform(get("/api/v1/personas/documento/123456789"))
                .andExpect(status().isOk());

        verify(usuarioService).obtenerPorNumeroDocumento("123456789");
    }

    @Test
    void obtenerPorDocumento_DeberiaRetornarNotFound() throws Exception {
        when(usuarioService.obtenerPorNumeroDocumento("999999999"))
                .thenThrow(new ResourceNotFoundException("Persona no encontrada con documento: 999999999"));

        mockMvc.perform(get("/api/v1/personas/documento/999999999"))
                .andExpect(status().isNotFound());

        verify(usuarioService).obtenerPorNumeroDocumento("999999999");
    }

    @Test
    void obtenerPorEmail_DeberiaRetornarOk() throws Exception {
        when(usuarioService.obtenerPorEmail("usuario@test.com")).thenReturn(crearUsuarioDTO());

        mockMvc.perform(get("/api/v1/personas/email/usuario@test.com"))
                .andExpect(status().isOk());

        verify(usuarioService).obtenerPorEmail("usuario@test.com");
    }

    @Test
    void obtenerPorEmail_DeberiaRetornarNotFound() throws Exception {
        when(usuarioService.obtenerPorEmail("noexiste@test.com"))
                .thenThrow(new ResourceNotFoundException("Persona no encontrada con email: noexiste@test.com"));

        mockMvc.perform(get("/api/v1/personas/email/noexiste@test.com"))
                .andExpect(status().isNotFound());

        verify(usuarioService).obtenerPorEmail("noexiste@test.com");
    }

    @Test
    void obtenerBarberos_DeberiaRetornarOk() throws Exception {
        when(usuarioService.obtenerBarberos()).thenReturn(List.of(crearUsuarioDTO()));

        mockMvc.perform(get("/api/v1/personas/barberos"))
                .andExpect(status().isOk());

        verify(usuarioService).obtenerBarberos();
    }

    @Test
    void obtenerTodas_DeberiaRetornarOk() throws Exception {
        when(usuarioService.obtenerTodas()).thenReturn(List.of(crearUsuarioDTO()));

        mockMvc.perform(get("/api/v1/personas"))
                .andExpect(status().isOk());

        verify(usuarioService).obtenerTodas();
    }

    @Test
    void cambiarRol_DeberiaRetornarOk() throws Exception {
        UsuarioDTO usuarioDTO = crearUsuarioDTO();

        when(usuarioService.cambiarRol("123456789", 2)).thenReturn(usuarioDTO);

        mockMvc.perform(put("/api/v1/personas/123456789/rol")
                        .param("nuevoRol", "2"))
                .andExpect(status().isOk());

        verify(usuarioService).cambiarRol("123456789", 2);
    }

    @Test
    void cambiarRol_DeberiaRetornarNotFound() throws Exception {
        when(usuarioService.cambiarRol("999999999", 2))
                .thenThrow(new ResourceNotFoundException("Persona no encontrada con documento: 999999999"));

        mockMvc.perform(put("/api/v1/personas/999999999/rol")
                        .param("nuevoRol", "2"))
                .andExpect(status().isNotFound());

        verify(usuarioService).cambiarRol("999999999", 2);
    }

    private UsuarioDTO crearUsuarioDTO() {
        return UsuarioDTO.builder()
                .numeroDocumento("123456789")
                .numeroCelular("3001234567")
                .email("usuario@test.com")
                .nombrePersona("Usuario Prueba")
                .idEstado(1)
                .idRol(3)
                .build();
    }
}