package com.barberia.modules.modulo_auth.controllers;

import com.barberia.modules.modulo_auth.models.dtos.LoginResponseDTO;
import com.barberia.modules.modulo_auth.services.AuthService;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void login_DeberiaRetornarOkCuandoCredencialesSonValidas() throws Exception {
        when(authService.iniciarSesion(any())).thenReturn(crearLoginResponseDTO());

        String body = """
                {
                    "email": "usuario@test.com",
                    "contraseña": "123456"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(authService).iniciarSesion(any());
    }

    @Test
    void login_DeberiaRetornarUnauthorizedCuandoCredencialesSonInvalidas() throws Exception {
        when(authService.iniciarSesion(any()))
                .thenThrow(new IllegalArgumentException("Error: Credenciales inválidas"));

        String body = """
                {
                    "email": "usuario@test.com",
                    "contraseña": "incorrecta"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        verify(authService).iniciarSesion(any());
    }

    @Test
    void login_DeberiaRetornarInternalServerErrorCuandoOcurreErrorGeneral() throws Exception {
        when(authService.iniciarSesion(any()))
                .thenThrow(new RuntimeException("Error inesperado"));

        String body = """
                {
                    "email": "usuario@test.com",
                    "contraseña": "123456"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError());

        verify(authService).iniciarSesion(any());
    }

    private LoginResponseDTO crearLoginResponseDTO() {
        UsuarioDTO usuarioDTO = UsuarioDTO.builder()
                .numeroDocumento("123456789")
                .email("usuario@test.com")
                .nombrePersona("Usuario Prueba")
                .idRol(3)
                .idEstado(1)
                .build();

        return LoginResponseDTO.builder()
                .token("jwt-token-prueba")
                .type("Bearer")
                .usuario(usuarioDTO)
                .build();
    }
}