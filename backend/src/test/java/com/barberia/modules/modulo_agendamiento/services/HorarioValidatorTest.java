package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_horarios.models.entities.HorarioNegocio;
import com.barberia.modules.modulo_horarios.repositories.HorarioNegocioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioValidatorTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 5, 4); // lunes -> idDia 1
    private static final long ID_DIA_LUNES = 1L;

    @Mock
    private HorarioNegocioRepository horarioNegocioRepository;

    @InjectMocks
    private HorarioValidator horarioValidator;

    @Test
    void validar_DeberiaPasarCuandoLocalAbiertoYHorarioDentroDeRango() {
        when(horarioNegocioRepository.findByIdDia(ID_DIA_LUNES))
                .thenReturn(Optional.of(crearHorarioAbierto()));

        assertDoesNotThrow(() ->
                horarioValidator.validar(LUNES, LocalTime.of(9, 0), LocalTime.of(10, 0))
        );

        verify(horarioNegocioRepository).findByIdDia(ID_DIA_LUNES);
    }

    @Test
    void validar_DeberiaLanzarErrorCuandoNoExisteHorarioParaElDia() {
        when(horarioNegocioRepository.findByIdDia(ID_DIA_LUNES)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> horarioValidator.validar(LUNES, LocalTime.of(9, 0), LocalTime.of(10, 0))
        );

        assertEquals("Horario no encontrado para el día: 1", exception.getMessage());
    }

    @Test
    void validar_DeberiaLanzarErrorCuandoLocalEstaCerrado() {
        when(horarioNegocioRepository.findByIdDia(ID_DIA_LUNES))
                .thenReturn(Optional.of(crearHorarioCerrado()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> horarioValidator.validar(LUNES, LocalTime.of(9, 0), LocalTime.of(10, 0))
        );

        assertEquals("El local no está abierto para la fecha seleccionada", exception.getMessage());
    }

    @Test
    void validar_DeberiaLanzarErrorCuandoLocalAbiertoEsNull() {
        HorarioNegocio horario = crearHorarioAbierto();
        horario.setLocalAbierto(null);

        when(horarioNegocioRepository.findByIdDia(ID_DIA_LUNES))
                .thenReturn(Optional.of(horario));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> horarioValidator.validar(LUNES, LocalTime.of(9, 0), LocalTime.of(10, 0))
        );

        assertEquals("El local no está abierto para la fecha seleccionada", exception.getMessage());
    }

    @Test
    void validar_DeberiaLanzarErrorCuandoHoraInicioEsAntesDeApertura() {
        when(horarioNegocioRepository.findByIdDia(ID_DIA_LUNES))
                .thenReturn(Optional.of(crearHorarioAbierto()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> horarioValidator.validar(LUNES, LocalTime.of(7, 0), LocalTime.of(10, 0))
        );

        assertEquals("La hora de inicio está fuera del horario de apertura", exception.getMessage());
    }

    @Test
    void validar_DeberiaLanzarErrorCuandoHoraFinEsDespuesDeCierre() {
        when(horarioNegocioRepository.findByIdDia(ID_DIA_LUNES))
                .thenReturn(Optional.of(crearHorarioAbierto()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> horarioValidator.validar(LUNES, LocalTime.of(9, 0), LocalTime.of(19, 0))
        );

        assertEquals("La cita excede el horario de cierre", exception.getMessage());
    }

    @Test
    void validar_DeberiaPasarEnLosLimitesExactosDeAperturaYCierre() {
        when(horarioNegocioRepository.findByIdDia(ID_DIA_LUNES))
                .thenReturn(Optional.of(crearHorarioAbierto()));

        assertDoesNotThrow(() ->
                horarioValidator.validar(LUNES, LocalTime.of(8, 0), LocalTime.of(18, 0))
        );
    }

    @Test
    void validar_DeberiaCalcularIdDiaCorrectamenteParaDiaDistintoDeLunes() {
        // viernes 8 de mayo 2026 -> idDia 5
        LocalDate viernes = LocalDate.of(2026, 5, 8);
        when(horarioNegocioRepository.findByIdDia(5L))
                .thenReturn(Optional.of(crearHorarioAbierto()));

        assertDoesNotThrow(() ->
                horarioValidator.validar(viernes, LocalTime.of(9, 0), LocalTime.of(10, 0))
        );

        verify(horarioNegocioRepository).findByIdDia(5L);
    }

    private HorarioNegocio crearHorarioAbierto() {
        return HorarioNegocio.builder()
                .idHorarioNegocio(1L)
                .idDia(ID_DIA_LUNES)
                .horaApertura(LocalTime.of(8, 0))
                .horaCierre(LocalTime.of(18, 0))
                .localAbierto(true)
                .build();
    }

    private HorarioNegocio crearHorarioCerrado() {
        return HorarioNegocio.builder()
                .idHorarioNegocio(1L)
                .idDia(ID_DIA_LUNES)
                .horaApertura(LocalTime.of(8, 0))
                .horaCierre(LocalTime.of(18, 0))
                .localAbierto(false)
                .build();
    }
}
