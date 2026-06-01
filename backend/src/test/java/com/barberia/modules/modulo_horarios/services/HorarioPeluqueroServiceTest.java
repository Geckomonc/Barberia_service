package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_horarios.dto.ActualizarHorarioPeluqueroDTO;
import com.barberia.modules.modulo_horarios.exceptions.HorarioInvalidoException;
import com.barberia.modules.modulo_horarios.models.HorarioPeluquero;
import com.barberia.modules.modulo_horarios.repositories.HorarioPeluqueroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link HorarioPeluqueroService} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class HorarioPeluqueroServiceTest {

    @Mock
    private HorarioPeluqueroRepository horarioPeluqueroRepository;

    @InjectMocks
    private HorarioPeluqueroService horarioPeluqueroService;

    private ActualizarHorarioPeluqueroDTO dtoValido;

    @BeforeEach
    void setUp() {
        dtoValido = new ActualizarHorarioPeluqueroDTO();
        dtoValido.setIdDia(2L);
        dtoValido.setHoraInicioHorario("09:00:00");
        dtoValido.setHoraFinHorario("17:00:00");
    }

    @Test
    @DisplayName("actualizarHorario crea horario nuevo cuando no existe previo")
    void actualizarHorario_sinHorarioExistente_creaNuevo() {
        // Arrange
        when(horarioPeluqueroRepository.findByNumeroDocumentoPeluqueroAndIdDia("123", 2L))
                .thenReturn(Optional.empty());

        // Act
        horarioPeluqueroService.actualizarHorario("123", dtoValido);

        // Assert
        ArgumentCaptor<HorarioPeluquero> captor = ArgumentCaptor.forClass(HorarioPeluquero.class);
        verify(horarioPeluqueroRepository).save(captor.capture());
        HorarioPeluquero guardado = captor.getValue();
        assertEquals("123", guardado.getNumeroDocumentoPeluquero());
        assertEquals(2L, guardado.getIdDia());
        assertEquals(LocalTime.of(9, 0), guardado.getHoraInicio());
        assertEquals(LocalTime.of(17, 0), guardado.getHoraFin());
        assertNotNull(guardado.getFechaCreacion());
        assertNotNull(guardado.getFechaActualizacion());
    }

    @Test
    @DisplayName("actualizarHorario conserva fechaCreacion cuando ya existe")
    void actualizarHorario_conHorarioExistente_conservaFechaCreacion() {
        // Arrange
        LocalDateTime fechaCreacionOriginal = LocalDateTime.of(2024, 1, 1, 10, 0);
        HorarioPeluquero existente = new HorarioPeluquero();
        existente.setFechaCreacion(fechaCreacionOriginal);
        when(horarioPeluqueroRepository.findByNumeroDocumentoPeluqueroAndIdDia("123", 2L))
                .thenReturn(Optional.of(existente));

        // Act
        horarioPeluqueroService.actualizarHorario("123", dtoValido);

        // Assert
        ArgumentCaptor<HorarioPeluquero> captor = ArgumentCaptor.forClass(HorarioPeluquero.class);
        verify(horarioPeluqueroRepository).save(captor.capture());
        assertEquals(fechaCreacionOriginal, captor.getValue().getFechaCreacion());
    }

    @Test
    @DisplayName("actualizarHorario lanza HorarioInvalidoException cuando inicio > fin")
    void actualizarHorario_conInicioMayorQueFin_lanzaHorarioInvalidoException() {
        // Arrange
        ActualizarHorarioPeluqueroDTO dto = new ActualizarHorarioPeluqueroDTO();
        dto.setIdDia(1L);
        dto.setHoraInicioHorario("18:00:00");
        dto.setHoraFinHorario("08:00:00");

        // Act + Assert
        assertThrows(HorarioInvalidoException.class,
                () -> horarioPeluqueroService.actualizarHorario("123", dto));
        verify(horarioPeluqueroRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarHorario lanza HorarioInvalidoException cuando inicio == fin")
    void actualizarHorario_conInicioIgualAFin_lanzaHorarioInvalidoException() {
        // Arrange
        ActualizarHorarioPeluqueroDTO dto = new ActualizarHorarioPeluqueroDTO();
        dto.setIdDia(1L);
        dto.setHoraInicioHorario("10:00:00");
        dto.setHoraFinHorario("10:00:00");

        // Act + Assert
        assertThrows(HorarioInvalidoException.class,
                () -> horarioPeluqueroService.actualizarHorario("123", dto));
        verify(horarioPeluqueroRepository, never()).save(any());
    }

    @Test
    @DisplayName("obtenerHorario retorna la lista de horarios del peluquero")
    void obtenerHorario_conPeluqueroConHorarios_retornaLista() {
        // Arrange
        HorarioPeluquero h1 = new HorarioPeluquero();
        h1.setIdDia(1L);
        HorarioPeluquero h2 = new HorarioPeluquero();
        h2.setIdDia(2L);
        when(horarioPeluqueroRepository.findByNumeroDocumentoPeluquero("123"))
                .thenReturn(Arrays.asList(h1, h2));

        // Act
        List<HorarioPeluquero> resultado = horarioPeluqueroService.obtenerHorario("123");

        // Assert
        assertEquals(2, resultado.size());
        verify(horarioPeluqueroRepository).findByNumeroDocumentoPeluquero("123");
    }

    @Test
    @DisplayName("obtenerHorario retorna lista vacía cuando el peluquero no tiene horarios")
    void obtenerHorario_sinHorarios_retornaListaVacia() {
        // Arrange
        when(horarioPeluqueroRepository.findByNumeroDocumentoPeluquero("999"))
                .thenReturn(Collections.emptyList());

        // Act
        List<HorarioPeluquero> resultado = horarioPeluqueroService.obtenerHorario("999");

        // Assert
        assertTrue(resultado.isEmpty());
    }
}
