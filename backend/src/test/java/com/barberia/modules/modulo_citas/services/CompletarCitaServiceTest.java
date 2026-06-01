package com.barberia.modules.modulo_citas.services;

import com.barberia.modules.modulo_citas.dto.CompletarCitaRequestDTO;
import com.barberia.modules.modulo_citas.dto.CompletarCitaResponseDTO;
import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link CompletarCitaService} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class CompletarCitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CompletarCitaService completarCitaService;

    private CompletarCitaRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new CompletarCitaRequestDTO();
        request.setIdCita(1L);
    }

    @Test
    @DisplayName("completarCita cambia el estado a 6 y retorna response con estadoActual COMPLETADA")
    void completarCita_conPeluqueroValidoYCitaExistente_retornaResponseExitoso() {
        // Arrange
        Cita cita = Cita.builder()
                .noCita(1L)
                .numeroDocumentoPeluquero("123")
                .idEstado(5L)
                .build();
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));

        // Act
        CompletarCitaResponseDTO response = completarCitaService.completarCita(request, authentication);

        // Assert
        assertEquals(1L, response.getIdCita());
        assertEquals("5", response.getEstadoAnterior());
        assertEquals("COMPLETADA", response.getEstadoActual());
        assertEquals("Cita completada exitosamente", response.getMensaje());
        assertEquals(6L, cita.getIdEstado());
        verify(citaRepository).save(cita);
    }

    @Test
    @DisplayName("completarCita lanza IllegalArgumentException cuando la cita no existe")
    void completarCita_conCitaInexistente_lanzaIllegalArgumentException() {
        // Arrange
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());
        request.setIdCita(99L);

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> completarCitaService.completarCita(request, authentication));
        assertTrue(ex.getMessage().contains("no encontrada"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("completarCita lanza SecurityException cuando el usuario no es el peluquero asignado")
    void completarCita_conUsuarioNoAsignado_lanzaSecurityException() {
        // Arrange
        Cita cita = Cita.builder()
                .noCita(1L)
                .numeroDocumentoPeluquero("999")
                .idEstado(5L)
                .build();
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));

        // Act + Assert
        assertThrows(SecurityException.class,
                () -> completarCitaService.completarCita(request, authentication));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("completarCita retorna estadoAnterior DESCONOCIDO cuando idEstado es null")
    void completarCita_conIdEstadoNull_retornaEstadoAnteriorDesconocido() {
        // Arrange
        Cita cita = Cita.builder()
                .noCita(1L)
                .numeroDocumentoPeluquero("123")
                .idEstado(null)
                .build();
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));

        // Act
        CompletarCitaResponseDTO response = completarCitaService.completarCita(request, authentication);

        // Assert
        assertEquals("DESCONOCIDO", response.getEstadoAnterior());
        assertEquals(6L, cita.getIdEstado());
    }

    @Test
    @DisplayName("completarCita usa authentication.getName() cuando no hay Map en details")
    void completarCita_sinDetailsMap_usaAuthenticationName() {
        // Arrange
        Cita cita = Cita.builder()
                .noCita(1L)
                .numeroDocumentoPeluquero("789")
                .idEstado(4L)
                .build();
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(authentication.getDetails()).thenReturn(null);
        when(authentication.getName()).thenReturn("789");

        // Act
        CompletarCitaResponseDTO response = completarCitaService.completarCita(request, authentication);

        // Assert
        assertEquals("COMPLETADA", response.getEstadoActual());
        assertEquals(6L, cita.getIdEstado());
    }

    @Test
    @DisplayName("completarCita actualiza la cita independientemente del estado previo")
    void completarCita_conEstadoCualquiera_actualizaEstadoA6() {
        // Arrange
        Cita cita = Cita.builder()
                .noCita(1L)
                .numeroDocumentoPeluquero("123")
                .idEstado(2L)
                .build();
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));

        // Act
        CompletarCitaResponseDTO response = completarCitaService.completarCita(request, authentication);

        // Assert
        assertEquals("2", response.getEstadoAnterior());
        assertEquals(6L, cita.getIdEstado());
        verify(citaRepository, times(1)).save(cita);
    }
}
