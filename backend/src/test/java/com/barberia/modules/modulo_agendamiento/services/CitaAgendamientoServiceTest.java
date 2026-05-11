package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_agendamiento.mappers.CitaMapper;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaCreateDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadRequestDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadResponseDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaReprogramarDTO;
import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_agendamiento.repositories.CitaRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaAgendamientoServiceTest {

    private static final Long ESTADO_ACTIVO = 1L;
    private static final Long ESTADO_CANCELADA = 3L;
    private static final LocalDate FECHA_CITA = LocalDate.of(2026, 5, 4);
    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(10, 0);
    private static final String DOC_CLIENTE = "111111111";
    private static final String DOC_PELUQUERO = "222222222";
    private static final String DOC_OTRO_PELUQUERO = "999999999";

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private HorarioValidator horarioValidator;

    @Mock
    private ConflictoCitaChecker conflictoChecker;

    @Mock
    private CitaMapper citaMapper;

    @InjectMocks
    private CitaAgendamientoService citaAgendamientoService;

    // ---------- consultarDisponibilidad ----------

    @Test
    void consultarDisponibilidad_DeberiaRetornarDisponibleCuandoNoHayConflictos() {
        CitaDisponibilidadRequestDTO request = crearRequestDisponibilidadValido();
        CitaDisponibilidadResponseDTO esperado = crearRespuestaDisponibilidad(true, "Disponible");

        when(conflictoChecker.hayConflicto(DOC_PELUQUERO, FECHA_CITA, HORA_INICIO, HORA_FIN))
                .thenReturn(false);
        when(citaMapper.toDisponibilidadResponse(request, true)).thenReturn(esperado);

        CitaDisponibilidadResponseDTO resultado = citaAgendamientoService.consultarDisponibilidad(request);

        assertSame(esperado, resultado);
        verify(horarioValidator).validar(FECHA_CITA, HORA_INICIO, HORA_FIN);
        verify(conflictoChecker).hayConflicto(DOC_PELUQUERO, FECHA_CITA, HORA_INICIO, HORA_FIN);
        verify(citaMapper).toDisponibilidadResponse(request, true);
    }

    @Test
    void consultarDisponibilidad_DeberiaRetornarNoDisponibleCuandoHayConflicto() {
        CitaDisponibilidadRequestDTO request = crearRequestDisponibilidadValido();
        CitaDisponibilidadResponseDTO esperado =
                crearRespuestaDisponibilidad(false, "No disponible: existe una cita solapada");

        when(conflictoChecker.hayConflicto(DOC_PELUQUERO, FECHA_CITA, HORA_INICIO, HORA_FIN))
                .thenReturn(true);
        when(citaMapper.toDisponibilidadResponse(request, false)).thenReturn(esperado);

        CitaDisponibilidadResponseDTO resultado = citaAgendamientoService.consultarDisponibilidad(request);

        assertSame(esperado, resultado);
        assertFalse(resultado.isDisponible());
    }

    @Test
    void consultarDisponibilidad_DeberiaPropagarErrorCuandoHorarioInvalido() {
        CitaDisponibilidadRequestDTO request = crearRequestDisponibilidadValido();

        doThrow(new IllegalArgumentException("El local no está abierto para la fecha seleccionada"))
                .when(horarioValidator).validar(FECHA_CITA, HORA_INICIO, HORA_FIN);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> citaAgendamientoService.consultarDisponibilidad(request)
        );

        assertEquals("El local no está abierto para la fecha seleccionada", exception.getMessage());
        verifyNoInteractions(conflictoChecker);
        verifyNoInteractions(citaMapper);
    }

    // ---------- agendar ----------

    @Test
    void agendar_DeberiaCrearCitaCuandoNoHayConflictos() {
        CitaCreateDTO request = crearRequestAgendarValido();
        Cita citaParaGuardar = crearCitaSinId();
        Cita citaGuardada = crearCitaActiva();
        CitaDTO esperado = crearCitaDTOEsperado();

        when(citaMapper.toEntity(request, DOC_CLIENTE)).thenReturn(citaParaGuardar);
        when(citaRepository.save(citaParaGuardar)).thenReturn(citaGuardada);
        when(citaMapper.toDto(citaGuardada)).thenReturn(esperado);

        CitaDTO resultado = citaAgendamientoService.agendar(request, DOC_CLIENTE);

        assertSame(esperado, resultado);
        verify(horarioValidator).validar(FECHA_CITA, HORA_INICIO, HORA_FIN);
        verify(conflictoChecker)
                .validarSinConflictosParaAgendar(DOC_PELUQUERO, FECHA_CITA, HORA_INICIO, HORA_FIN);
        verify(citaRepository).save(citaParaGuardar);
    }

    @Test
    void agendar_DeberiaLanzarErrorCuandoClienteNoExisteEnToken() {
        CitaCreateDTO request = CitaCreateDTO.builder().build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> citaAgendamientoService.agendar(request, "")
        );

        assertEquals("numeroDocumentoCliente no encontrado en el token", exception.getMessage());
        verifyNoInteractions(citaRepository);
        verifyNoInteractions(horarioValidator);
        verifyNoInteractions(conflictoChecker);
        verifyNoInteractions(citaMapper);
    }

    @Test
    void agendar_DeberiaLanzarErrorCuandoClienteEsNull() {
        CitaCreateDTO request = CitaCreateDTO.builder().build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> citaAgendamientoService.agendar(request, null)
        );

        assertEquals("numeroDocumentoCliente no encontrado en el token", exception.getMessage());
        verifyNoInteractions(citaRepository);
    }

    @Test
    void agendar_DeberiaLanzarErrorCuandoExisteConflicto() {
        CitaCreateDTO request = crearRequestAgendarValido();

        doThrow(new IllegalArgumentException("No se puede agendar: existe una cita solapada"))
                .when(conflictoChecker)
                .validarSinConflictosParaAgendar(DOC_PELUQUERO, FECHA_CITA, HORA_INICIO, HORA_FIN);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> citaAgendamientoService.agendar(request, DOC_CLIENTE)
        );

        assertEquals("No se puede agendar: existe una cita solapada", exception.getMessage());
        verify(citaRepository, never()).save(any(Cita.class));
        verify(citaMapper, never()).toDto(any());
    }

    // ---------- cancelar ----------

    @Test
    void cancelar_DeberiaCancelarCitaActiva() {
        Cita cita = crearCitaActiva();
        CitaDTO esperado = crearCitaDTOEsperado();

        when(citaRepository.findById(10L)).thenReturn(Optional.of(cita));
        when(citaRepository.save(cita)).thenReturn(cita);
        when(citaMapper.toDto(cita)).thenReturn(esperado);

        CitaDTO resultado = citaAgendamientoService.cancelar(10L);

        assertSame(esperado, resultado);
        assertEquals(ESTADO_CANCELADA, cita.getIdEstado());
        verify(citaRepository).save(cita);
    }

    @Test
    void cancelar_DeberiaLanzarErrorCuandoCitaNoExiste() {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> citaAgendamientoService.cancelar(99L)
        );

        assertEquals("Cita no encontrada para noCita: 99", exception.getMessage());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void cancelar_DeberiaLanzarErrorCuandoCitaNoEstaActiva() {
        Cita citaCancelada = crearCitaActiva();
        citaCancelada.setIdEstado(ESTADO_CANCELADA);

        when(citaRepository.findById(10L)).thenReturn(Optional.of(citaCancelada));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> citaAgendamientoService.cancelar(10L)
        );

        assertEquals("La cita no está activa o ya fue gestionada", exception.getMessage());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    // ---------- reprogramar ----------

    @Test
    void reprogramar_DeberiaActualizarFechaYHoraCuandoNoHayConflictos() {
        CitaReprogramarDTO request = crearRequestReprogramarValido();
        Cita cita = crearCitaActiva();
        CitaDTO esperado = crearCitaDTOEsperado();

        when(citaRepository.findById(10L)).thenReturn(Optional.of(cita));
        when(citaRepository.save(cita)).thenReturn(cita);
        when(citaMapper.toDto(cita)).thenReturn(esperado);

        CitaDTO resultado = citaAgendamientoService.reprogramar(10L, request);

        assertSame(esperado, resultado);
        assertEquals(FECHA_CITA, cita.getFechaCita());
        assertEquals(HORA_INICIO, cita.getHoraInicioCita());
        assertEquals(HORA_FIN, cita.getHoraFinCita());
        assertEquals(ESTADO_ACTIVO, cita.getIdEstado());

        verify(horarioValidator).validar(FECHA_CITA, HORA_INICIO, HORA_FIN);
        verify(conflictoChecker).validarSinConflictosParaReprogramar(cita, FECHA_CITA, HORA_INICIO, HORA_FIN);
    }

    @Test
    void reprogramar_DeberiaLanzarErrorCuandoCitaNoExiste() {
        CitaReprogramarDTO request = crearRequestReprogramarValido();

        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> citaAgendamientoService.reprogramar(99L, request)
        );

        assertEquals("Cita no encontrada para noCita: 99", exception.getMessage());
        verifyNoInteractions(horarioValidator);
        verifyNoInteractions(conflictoChecker);
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void reprogramar_DeberiaLanzarErrorCuandoExisteConflicto() {
        CitaReprogramarDTO request = crearRequestReprogramarValido();
        Cita cita = crearCitaActiva();

        when(citaRepository.findById(10L)).thenReturn(Optional.of(cita));
        doThrow(new IllegalArgumentException("No se puede reprogramar: existe una cita solapada"))
                .when(conflictoChecker)
                .validarSinConflictosParaReprogramar(cita, FECHA_CITA, HORA_INICIO, HORA_FIN);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> citaAgendamientoService.reprogramar(10L, request)
        );

        assertEquals("No se puede reprogramar: existe una cita solapada", exception.getMessage());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    // ---------- confirmar ----------

    @Test
    void confirmar_DeberiaConfirmarCitaCuandoPerteneceAlPeluquero() {
        Cita cita = crearCitaActiva();
        CitaDTO esperado = crearCitaDTOEsperado();

        when(citaRepository.findById(10L)).thenReturn(Optional.of(cita));
        when(citaRepository.save(cita)).thenReturn(cita);
        when(citaMapper.toDto(cita)).thenReturn(esperado);

        CitaDTO resultado = citaAgendamientoService.confirmar(10L, DOC_PELUQUERO);

        assertSame(esperado, resultado);
        assertTrue(cita.getCitaConfirmada());
        verify(citaRepository).save(cita);
    }

    @Test
    void confirmar_DeberiaLanzarErrorCuandoCitaNoPerteneceAlPeluquero() {
        Cita cita = crearCitaActiva();

        when(citaRepository.findById(10L)).thenReturn(Optional.of(cita));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> citaAgendamientoService.confirmar(10L, DOC_OTRO_PELUQUERO)
        );

        assertEquals("No puedes confirmar una cita que no te pertenece", exception.getMessage());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void confirmar_DeberiaLanzarErrorCuandoPeluqueroEsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> citaAgendamientoService.confirmar(10L, null)
        );

        assertEquals("numeroDocumentoPeluquero es requerido", exception.getMessage());
        verifyNoInteractions(citaRepository);
    }

    @Test
    void confirmar_DeberiaLanzarErrorCuandoPeluqueroEsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> citaAgendamientoService.confirmar(10L, "  ")
        );

        assertEquals("numeroDocumentoPeluquero es requerido", exception.getMessage());
        verifyNoInteractions(citaRepository);
    }

    @Test
    void confirmar_DeberiaLanzarErrorCuandoCitaNoEstaActiva() {
        Cita citaInactiva = crearCitaActiva();
        citaInactiva.setIdEstado(ESTADO_CANCELADA);

        when(citaRepository.findById(10L)).thenReturn(Optional.of(citaInactiva));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> citaAgendamientoService.confirmar(10L, DOC_PELUQUERO)
        );

        assertEquals("La cita no está activa", exception.getMessage());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    // ---------- helpers ----------

    private CitaDisponibilidadRequestDTO crearRequestDisponibilidadValido() {
        return CitaDisponibilidadRequestDTO.builder()
                .numeroDocumentoPeluquero(DOC_PELUQUERO)
                .idServicio(1L)
                .fechaCita(FECHA_CITA)
                .horaInicioCita(HORA_INICIO)
                .horaFinCita(HORA_FIN)
                .build();
    }

    private CitaCreateDTO crearRequestAgendarValido() {
        return CitaCreateDTO.builder()
                .numeroDocumentoPeluquero(DOC_PELUQUERO)
                .idServicio(1L)
                .fechaCita(FECHA_CITA)
                .horaInicioCita(HORA_INICIO)
                .horaFinCita(HORA_FIN)
                .build();
    }

    private CitaReprogramarDTO crearRequestReprogramarValido() {
        return CitaReprogramarDTO.builder()
                .fechaCita(FECHA_CITA)
                .horaInicioCita(HORA_INICIO)
                .horaFinCita(HORA_FIN)
                .build();
    }

    private Cita crearCitaSinId() {
        return Cita.builder()
                .numeroDocumentoCliente(DOC_CLIENTE)
                .numeroDocumentoPeluquero(DOC_PELUQUERO)
                .idServicio(1L)
                .fechaCita(FECHA_CITA)
                .horaInicioCita(HORA_INICIO)
                .horaFinCita(HORA_FIN)
                .idEstado(ESTADO_ACTIVO)
                .citaConfirmada(false)
                .fechaCreacion(Instant.parse("2026-05-04T12:00:00Z"))
                .build();
    }

    private Cita crearCitaActiva() {
        return Cita.builder()
                .noCita(10L)
                .numeroDocumentoCliente(DOC_CLIENTE)
                .numeroDocumentoPeluquero(DOC_PELUQUERO)
                .idServicio(1L)
                .fechaCita(FECHA_CITA)
                .horaInicioCita(HORA_INICIO)
                .horaFinCita(HORA_FIN)
                .idEstado(ESTADO_ACTIVO)
                .citaConfirmada(false)
                .fechaCreacion(Instant.parse("2026-05-04T12:00:00Z"))
                .build();
    }

    private CitaDTO crearCitaDTOEsperado() {
        return CitaDTO.builder()
                .noCita(10L)
                .numeroDocumentoCliente(DOC_CLIENTE)
                .numeroDocumentoPeluquero(DOC_PELUQUERO)
                .idServicio(1L)
                .fechaCita(FECHA_CITA)
                .horaInicioCita(HORA_INICIO)
                .horaFinCita(HORA_FIN)
                .idEstado(ESTADO_ACTIVO)
                .citaConfirmada(false)
                .fechaCreacion(Instant.parse("2026-05-04T12:00:00Z"))
                .build();
    }

    private CitaDisponibilidadResponseDTO crearRespuestaDisponibilidad(boolean disponible, String mensaje) {
        return CitaDisponibilidadResponseDTO.builder()
                .disponible(disponible)
                .mensaje(mensaje)
                .numeroDocumentoPeluquero(DOC_PELUQUERO)
                .idServicio(1L)
                .fechaCita(FECHA_CITA)
                .horaInicioCita(HORA_INICIO)
                .horaFinCita(HORA_FIN)
                .build();
    }
}