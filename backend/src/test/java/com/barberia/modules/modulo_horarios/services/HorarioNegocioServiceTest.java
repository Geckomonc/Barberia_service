package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_horarios.models.dtos.HorarioNegocioDTO;
import com.barberia.modules.modulo_horarios.models.dtos.HorarioUpdateDTO;
import com.barberia.modules.modulo_horarios.models.entities.HorarioNegocio;
import com.barberia.modules.modulo_horarios.repositories.HorarioNegocioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link HorarioNegocioService} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class HorarioNegocioServiceTest {

    @Mock
    private HorarioNegocioRepository horarioNegocioRepository;

    @InjectMocks
    private HorarioNegocioService horarioNegocioService;

    private HorarioNegocio horario;

    @BeforeEach
    void setUp() {
        horario = HorarioNegocio.builder()
                .idHorarioNegocio(1L)
                .idDia(1L)
                .horaApertura(LocalTime.of(8, 0))
                .horaCierre(LocalTime.of(18, 0))
                .localAbierto(true)
                .build();
    }

    @Nested
    @DisplayName("obtenerTodos")
    class ObtenerTodos {

        @Test
        @DisplayName("retorna la lista ordenada por idDia")
        void obtenerTodos_conHorarios_retornaLista() {
            // Arrange
            when(horarioNegocioRepository.findAllByOrderByIdDiaAsc())
                    .thenReturn(Arrays.asList(horario));

            // Act
            List<HorarioNegocioDTO> resultado = horarioNegocioService.obtenerTodos();

            // Assert
            assertEquals(1, resultado.size());
            assertEquals(1L, resultado.get(0).getIdDia());
            verify(horarioNegocioRepository).findAllByOrderByIdDiaAsc();
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay horarios")
        void obtenerTodos_sinHorarios_retornaListaVacia() {
            // Arrange
            when(horarioNegocioRepository.findAllByOrderByIdDiaAsc())
                    .thenReturn(Collections.emptyList());

            // Act
            List<HorarioNegocioDTO> resultado = horarioNegocioService.obtenerTodos();

            // Assert
            assertTrue(resultado.isEmpty());
        }
    }

    @Nested
    @DisplayName("obtenerPorDia")
    class ObtenerPorDia {

        @Test
        @DisplayName("retorna HorarioNegocioDTO cuando existe")
        void obtenerPorDia_conIdValido_retornaHorarioDTO() {
            // Arrange
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));

            // Act
            HorarioNegocioDTO resultado = horarioNegocioService.obtenerPorDia(1L);

            // Assert
            assertEquals(1L, resultado.getIdDia());
            assertEquals(LocalTime.of(8, 0), resultado.getHoraApertura());
            assertTrue(resultado.getLocalAbierto());
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando no existe")
        void obtenerPorDia_conIdInexistente_lanzaResourceNotFound() {
            // Arrange
            when(horarioNegocioRepository.findByIdDia(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> horarioNegocioService.obtenerPorDia(99L));
        }
    }

    @Nested
    @DisplayName("actualizarHorario")
    class ActualizarHorario {

        @Test
        @DisplayName("actualiza horario con datos válidos")
        void actualizarHorario_conDatosValidos_actualizaHorario() {
            // Arrange
            HorarioUpdateDTO dto = new HorarioUpdateDTO("09:00:00", "19:00:00");
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));
            when(horarioNegocioRepository.save(any(HorarioNegocio.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            HorarioNegocioDTO resultado = horarioNegocioService.actualizarHorario(1L, dto);

            // Assert
            assertEquals(LocalTime.of(9, 0), resultado.getHoraApertura());
            assertEquals(LocalTime.of(19, 0), resultado.getHoraCierre());
            assertTrue(resultado.getLocalAbierto());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando hora apertura es null")
        void actualizarHorario_conAperturaNull_lanzaIllegalArgumentException() {
            // Arrange
            HorarioUpdateDTO dto = new HorarioUpdateDTO(null, "19:00:00");
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> horarioNegocioService.actualizarHorario(1L, dto));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando hora cierre es null")
        void actualizarHorario_conCierreNull_lanzaIllegalArgumentException() {
            // Arrange
            HorarioUpdateDTO dto = new HorarioUpdateDTO("09:00:00", null);
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> horarioNegocioService.actualizarHorario(1L, dto));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException con formato de hora inválido")
        void actualizarHorario_conFormatoInvalido_lanzaIllegalArgumentException() {
            // Arrange
            HorarioUpdateDTO dto = new HorarioUpdateDTO("XX:YY", "19:00:00");
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));

            // Act + Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> horarioNegocioService.actualizarHorario(1L, dto));
            assertTrue(ex.getMessage().contains("Formato"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando cierre es antes de apertura")
        void actualizarHorario_conCierreAntesDeApertura_lanzaIllegalArgumentException() {
            // Arrange
            HorarioUpdateDTO dto = new HorarioUpdateDTO("18:00:00", "08:00:00");
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> horarioNegocioService.actualizarHorario(1L, dto));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando el día no existe")
        void actualizarHorario_conDiaInexistente_lanzaResourceNotFound() {
            // Arrange
            HorarioUpdateDTO dto = new HorarioUpdateDTO("09:00:00", "19:00:00");
            when(horarioNegocioRepository.findByIdDia(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> horarioNegocioService.actualizarHorario(99L, dto));
        }
    }

    @Nested
    @DisplayName("cerrarDia")
    class CerrarDia {

        @Test
        @DisplayName("marca el día como cerrado (localAbierto = false)")
        void cerrarDia_conIdValido_marcaCerrado() {
            // Arrange
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));
            when(horarioNegocioRepository.save(any(HorarioNegocio.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            HorarioNegocioDTO resultado = horarioNegocioService.cerrarDia(1L);

            // Assert
            assertFalse(resultado.getLocalAbierto());
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando el día no existe")
        void cerrarDia_conIdInexistente_lanzaResourceNotFound() {
            // Arrange
            when(horarioNegocioRepository.findByIdDia(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> horarioNegocioService.cerrarDia(99L));
            verify(horarioNegocioRepository, never()).save(any());
        }
    }
}
