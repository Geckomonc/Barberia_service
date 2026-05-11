package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_agendamiento.repositories.CitaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConflictoCitaCheckerTest {

    private static final Long ESTADO_ACTIVO = 1L;
    private static final String DOC_PELUQUERO = "222222222";
    private static final LocalDate FECHA_CITA = LocalDate.of(2026, 5, 4);
    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(10, 0);
    private static final Long NO_CITA = 10L;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private ConflictoCitaChecker conflictoChecker;

    // ---------- hayConflicto ----------

    @Test
    void hayConflicto_DeberiaRetornarFalseCuandoNoHayConflictos() {
        when(citaRepository.findConflicts(DOC_PELUQUERO, FECHA_CITA, ESTADO_ACTIVO, HORA_INICIO, HORA_FIN))
                .thenReturn(List.of());

        boolean resultado = conflictoChecker.hayConflicto(DOC_PELUQUERO, FECHA_CITA, HORA_INICIO, HORA_FIN);

        assertFalse(resultado);
        verify(citaRepository).findConflicts(DOC_PELUQUERO, FECHA_CITA, ESTADO_ACTIVO, HORA_INICIO, HORA_FIN);
    }

    @Test
    void hayConflicto_DeberiaRetornarTrueCuandoExisteAlMenosUnaCitaSolapada() {
        when(citaRepository.findConflicts(DOC_PELUQUERO, FECHA_CITA, ESTADO_ACTIVO, HORA_INICIO, HORA_FIN))
                .thenReturn(List.of(new Cita()));

        boolean resultado = conflictoChecker.hayConflicto(DOC_PELUQUERO, FECHA_CITA, HORA_INICIO, HORA_FIN);

        assertTrue(resultado);
    }

    @Test
    void hayConflicto_DeberiaRetornarTrueCuandoExistenVariasCitasSolapadas() {
        when(citaRepository.findConflicts(DOC_PELUQUERO, FECHA_CITA, ESTADO_ACTIVO, HORA_INICIO, HORA_FIN))
                .thenReturn(List.of(new Cita(), new Cita(), new Cita()));

        boolean resultado = conflictoChecker.hayConflicto(DOC_PELUQUERO, FECHA_CITA, HORA_INICIO, HORA_FIN);

        assertTrue(resultado);
    }

    // ---------- validarSinConflictosParaAgendar ----------

    @Test
    void validarSinConflictosParaAgendar_DeberiaPasarCuandoNoHayConflictos() {
        when(citaRepository.findConflictsForUpdate(DOC_PELUQUERO, FECHA_CITA, ESTADO_ACTIVO, HORA_INICIO, HORA_FIN))
                .thenReturn(List.of());

        assertDoesNotThrow(() ->
                conflictoChecker.validarSinConflictosParaAgendar(DOC_PELUQUERO, FECHA_CITA, HORA_INICIO, HORA_FIN)
        );

        verify(citaRepository)
                .findConflictsForUpdate(DOC_PELUQUERO, FECHA_CITA, ESTADO_ACTIVO, HORA_INICIO, HORA_FIN);
    }

    @Test
    void validarSinConflictosParaAgendar_DeberiaLanzarErrorCuandoExisteConflicto() {
        when(citaRepository.findConflictsForUpdate(DOC_PELUQUERO, FECHA_CITA, ESTADO_ACTIVO, HORA_INICIO, HORA_FIN))
                .thenReturn(List.of(new Cita()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> conflictoChecker.validarSinConflictosParaAgendar(DOC_PELUQUERO, FECHA_CITA, HORA_INICIO, HORA_FIN)
        );

        assertEquals("No se puede agendar: existe una cita solapada", exception.getMessage());
    }

    // ---------- validarSinConflictosParaReprogramar ----------

    @Test
    void validarSinConflictosParaReprogramar_DeberiaPasarCuandoNoHayConflictos() {
        Cita cita = crearCita();
        when(citaRepository.findConflictsForUpdateExcludingNoCita(
                DOC_PELUQUERO, NO_CITA, FECHA_CITA, ESTADO_ACTIVO, HORA_INICIO, HORA_FIN))
                .thenReturn(List.of());

        assertDoesNotThrow(() ->
                conflictoChecker.validarSinConflictosParaReprogramar(cita, FECHA_CITA, HORA_INICIO, HORA_FIN)
        );
    }

    @Test
    void validarSinConflictosParaReprogramar_DeberiaLanzarErrorCuandoExisteConflicto() {
        Cita cita = crearCita();
        when(citaRepository.findConflictsForUpdateExcludingNoCita(
                DOC_PELUQUERO, NO_CITA, FECHA_CITA, ESTADO_ACTIVO, HORA_INICIO, HORA_FIN))
                .thenReturn(List.of(new Cita()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> conflictoChecker.validarSinConflictosParaReprogramar(cita, FECHA_CITA, HORA_INICIO, HORA_FIN)
        );

        assertEquals("No se puede reprogramar: existe una cita solapada", exception.getMessage());
    }

    @Test
    void validarSinConflictosParaReprogramar_DeberiaExcluirLaCitaActualDeLaBusqueda() {
        Cita cita = crearCita();
        when(citaRepository.findConflictsForUpdateExcludingNoCita(
                DOC_PELUQUERO, NO_CITA, FECHA_CITA, ESTADO_ACTIVO, HORA_INICIO, HORA_FIN))
                .thenReturn(List.of());

        conflictoChecker.validarSinConflictosParaReprogramar(cita, FECHA_CITA, HORA_INICIO, HORA_FIN);

        verify(citaRepository).findConflictsForUpdateExcludingNoCita(
                DOC_PELUQUERO, NO_CITA, FECHA_CITA, ESTADO_ACTIVO, HORA_INICIO, HORA_FIN);
        verifyNoMoreInteractions(citaRepository);
    }

    private Cita crearCita() {
        return Cita.builder()
                .noCita(NO_CITA)
                .numeroDocumentoPeluquero(DOC_PELUQUERO)
                .build();
    }
}
