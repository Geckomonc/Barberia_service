package com.barberia.modules.modulo_agendamiento.mappers;

import com.barberia.modules.modulo_agendamiento.models.dtos.CitaCreateDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadRequestDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadResponseDTO;
import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class CitaMapperTest {

    private static final Long ESTADO_ACTIVO = 1L;
    private static final String DOC_CLIENTE = "111111111";
    private static final String DOC_PELUQUERO = "222222222";
    private static final Long ID_SERVICIO = 1L;
    private static final LocalDate FECHA_CITA = LocalDate.of(2026, 5, 4);
    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(10, 0);

    private CitaMapper citaMapper;

    @BeforeEach
    void setUp() {
        citaMapper = new CitaMapper();
    }

    // ---------- toEntity ----------

    @Test
    void toEntity_DeberiaMapearTodosLosCamposDelRequest() {
        CitaCreateDTO request = crearRequestAgendar();

        Cita resultado = citaMapper.toEntity(request, DOC_CLIENTE);

        assertEquals(DOC_CLIENTE, resultado.getNumeroDocumentoCliente());
        assertEquals(DOC_PELUQUERO, resultado.getNumeroDocumentoPeluquero());
        assertEquals(ID_SERVICIO, resultado.getIdServicio());
        assertEquals(FECHA_CITA, resultado.getFechaCita());
        assertEquals(HORA_INICIO, resultado.getHoraInicioCita());
        assertEquals(HORA_FIN, resultado.getHoraFinCita());
    }

    @Test
    void toEntity_DeberiaAsignarEstadoActivoYConfirmadaEnFalsePorDefecto() {
        Cita resultado = citaMapper.toEntity(crearRequestAgendar(), DOC_CLIENTE);

        assertEquals(ESTADO_ACTIVO, resultado.getIdEstado());
        assertFalse(resultado.getCitaConfirmada());
    }

    @Test
    void toEntity_DeberiaAsignarFechaCreacionDentroDelRangoDeLaInvocacion() {
        Instant antes = Instant.now();

        Cita resultado = citaMapper.toEntity(crearRequestAgendar(), DOC_CLIENTE);

        Instant despues = Instant.now();
        assertNotNull(resultado.getFechaCreacion());
        assertFalse(resultado.getFechaCreacion().isBefore(antes));
        assertFalse(resultado.getFechaCreacion().isAfter(despues));
    }

    @Test
    void toEntity_DeberiaDejarNoCitaEnNullParaQueLaBaseDeDatosLoAsigne() {
        Cita resultado = citaMapper.toEntity(crearRequestAgendar(), DOC_CLIENTE);

        assertNull(resultado.getNoCita());
    }

    // ---------- toDto ----------

    @Test
    void toDto_DeberiaMapearTodosLosCamposDeLaEntidad() {
        Cita cita = crearCitaCompleta();

        CitaDTO resultado = citaMapper.toDto(cita);

        assertEquals(10L, resultado.getNoCita());
        assertEquals(DOC_CLIENTE, resultado.getNumeroDocumentoCliente());
        assertEquals(DOC_PELUQUERO, resultado.getNumeroDocumentoPeluquero());
        assertEquals(ID_SERVICIO, resultado.getIdServicio());
        assertEquals(FECHA_CITA, resultado.getFechaCita());
        assertEquals(HORA_INICIO, resultado.getHoraInicioCita());
        assertEquals(HORA_FIN, resultado.getHoraFinCita());
        assertEquals(ESTADO_ACTIVO, resultado.getIdEstado());
        assertTrue(resultado.getCitaConfirmada());
        assertEquals(Instant.parse("2026-05-04T12:00:00Z"), resultado.getFechaCreacion());
    }

    @Test
    void toDto_DeberiaPropagarValoresNullSinFallar() {
        Cita cita = Cita.builder().build();

        CitaDTO resultado = citaMapper.toDto(cita);

        assertNotNull(resultado);
        assertNull(resultado.getNoCita());
        assertNull(resultado.getNumeroDocumentoCliente());
        assertNull(resultado.getCitaConfirmada());
    }

    // ---------- toDisponibilidadResponse ----------

    @Test
    void toDisponibilidadResponse_DeberiaIndicarDisponibleConMensajeCorrespondiente() {
        CitaDisponibilidadRequestDTO request = crearRequestDisponibilidad();

        CitaDisponibilidadResponseDTO resultado = citaMapper.toDisponibilidadResponse(request, true);

        assertTrue(resultado.isDisponible());
        assertEquals("Disponible", resultado.getMensaje());
    }

    @Test
    void toDisponibilidadResponse_DeberiaIndicarNoDisponibleConMensajeCorrespondiente() {
        CitaDisponibilidadRequestDTO request = crearRequestDisponibilidad();

        CitaDisponibilidadResponseDTO resultado = citaMapper.toDisponibilidadResponse(request, false);

        assertFalse(resultado.isDisponible());
        assertEquals("No disponible: existe una cita solapada", resultado.getMensaje());
    }

    @Test
    void toDisponibilidadResponse_DeberiaPropagarTodosLosDatosDelRequest() {
        CitaDisponibilidadRequestDTO request = crearRequestDisponibilidad();

        CitaDisponibilidadResponseDTO resultado = citaMapper.toDisponibilidadResponse(request, true);

        assertEquals(DOC_PELUQUERO, resultado.getNumeroDocumentoPeluquero());
        assertEquals(ID_SERVICIO, resultado.getIdServicio());
        assertEquals(FECHA_CITA, resultado.getFechaCita());
        assertEquals(HORA_INICIO, resultado.getHoraInicioCita());
        assertEquals(HORA_FIN, resultado.getHoraFinCita());
    }

    // ---------- helpers ----------

    private CitaCreateDTO crearRequestAgendar() {
        return CitaCreateDTO.builder()
                .numeroDocumentoPeluquero(DOC_PELUQUERO)
                .idServicio(ID_SERVICIO)
                .fechaCita(FECHA_CITA)
                .horaInicioCita(HORA_INICIO)
                .horaFinCita(HORA_FIN)
                .build();
    }

    private CitaDisponibilidadRequestDTO crearRequestDisponibilidad() {
        return CitaDisponibilidadRequestDTO.builder()
                .numeroDocumentoPeluquero(DOC_PELUQUERO)
                .idServicio(ID_SERVICIO)
                .fechaCita(FECHA_CITA)
                .horaInicioCita(HORA_INICIO)
                .horaFinCita(HORA_FIN)
                .build();
    }

    private Cita crearCitaCompleta() {
        return Cita.builder()
                .noCita(10L)
                .numeroDocumentoCliente(DOC_CLIENTE)
                .numeroDocumentoPeluquero(DOC_PELUQUERO)
                .idServicio(ID_SERVICIO)
                .fechaCita(FECHA_CITA)
                .horaInicioCita(HORA_INICIO)
                .horaFinCita(HORA_FIN)
                .idEstado(ESTADO_ACTIVO)
                .citaConfirmada(true)
                .fechaCreacion(Instant.parse("2026-05-04T12:00:00Z"))
                .build();
    }
}
