package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_agendamiento.models.dtos.CitaCreateDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadRequestDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadResponseDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaReprogramarDTO;
import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_agendamiento.repositories.CitaRepository;
import com.barberia.modules.modulo_horarios.models.entities.HorarioNegocio;
import com.barberia.modules.modulo_horarios.repositories.HorarioNegocioRepository;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotSerializeTransactionException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link CitaAgendamientoService} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class CitaAgendamientoServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private HorarioNegocioRepository horarioNegocioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CitaAgendamientoService citaAgendamientoService;

    private CitaCreateDTO createDTO;
    private Usuario cliente;
    private Usuario peluquero;
    private HorarioNegocio horarioNegocio;
    // 2026-06-01 es lunes (idDia = 1)
    private final LocalDate fecha = LocalDate.of(2026, 6, 1);

    @BeforeEach
    void setUp() {
        createDTO = CitaCreateDTO.builder()
                .numeroDocumentoPeluquero("200")
                .idServicio(10L)
                .fechaCita(fecha)
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .build();

        cliente = Usuario.builder()
                .numeroDocumento("100")
                .nombrePersona("Cliente Test")
                .idRol(3)
                .idEstado(1)
                .build();

        peluquero = Usuario.builder()
                .numeroDocumento("200")
                .nombrePersona("Barbero Test")
                .idRol(2)
                .idEstado(1)
                .build();

        horarioNegocio = HorarioNegocio.builder()
                .idHorarioNegocio(1L)
                .idDia(1L)
                .horaApertura(LocalTime.of(8, 0))
                .horaCierre(LocalTime.of(18, 0))
                .localAbierto(true)
                .build();
    }

    // ============================================================
    // consultarDisponibilidad
    // ============================================================
    @Nested
    @DisplayName("consultarDisponibilidad")
    class ConsultarDisponibilidad {

        private CitaDisponibilidadRequestDTO requestValido() {
            return CitaDisponibilidadRequestDTO.builder()
                    .numeroDocumentoPeluquero("200")
                    .idServicio(10L)
                    .fechaCita(fecha)
                    .horaInicioCita(LocalTime.of(9, 0))
                    .horaFinCita(LocalTime.of(10, 0))
                    .build();
        }

        @Test
        @DisplayName("retorna disponible=true cuando no hay conflictos")
        void consultarDisponibilidad_sinConflictos_retornaDisponibleTrue() {
            // Arrange
            CitaDisponibilidadRequestDTO request = requestValido();
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horarioNegocio));
            when(citaRepository.findConflicts(anyString(), any(), anyLong(), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            CitaDisponibilidadResponseDTO response = citaAgendamientoService.consultarDisponibilidad(request);

            // Assert
            assertTrue(response.isDisponible());
            assertEquals("Disponible", response.getMensaje());
            assertEquals("200", response.getNumeroDocumentoPeluquero());
        }

        @Test
        @DisplayName("retorna disponible=false cuando existe cita solapada")
        void consultarDisponibilidad_conConflictos_retornaDisponibleFalse() {
            // Arrange
            CitaDisponibilidadRequestDTO request = requestValido();
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horarioNegocio));
            when(citaRepository.findConflicts(anyString(), any(), anyLong(), any(), any()))
                    .thenReturn(Arrays.asList(new Cita()));

            // Act
            CitaDisponibilidadResponseDTO response = citaAgendamientoService.consultarDisponibilidad(request);

            // Assert
            assertFalse(response.isDisponible());
            assertTrue(response.getMensaje().contains("solapada"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el request es null")
        void consultarDisponibilidad_conRequestNull_lanzaIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.consultarDisponibilidad(null));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando numeroDocumentoPeluquero es blanco")
        void consultarDisponibilidad_conPeluqueroBlanco_lanzaIllegalArgumentException() {
            // Arrange
            CitaDisponibilidadRequestDTO request = requestValido();
            request.setNumeroDocumentoPeluquero(" ");

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.consultarDisponibilidad(request));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando idServicio es null")
        void consultarDisponibilidad_conIdServicioNull_lanzaIllegalArgumentException() {
            // Arrange
            CitaDisponibilidadRequestDTO request = requestValido();
            request.setIdServicio(null);

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.consultarDisponibilidad(request));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando fecha u hora son null")
        void consultarDisponibilidad_conFechaNull_lanzaIllegalArgumentException() {
            // Arrange
            CitaDisponibilidadRequestDTO request = requestValido();
            request.setFechaCita(null);

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.consultarDisponibilidad(request));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el local está cerrado")
        void consultarDisponibilidad_conLocalCerrado_lanzaIllegalArgumentException() {
            // Arrange
            CitaDisponibilidadRequestDTO request = requestValido();
            horarioNegocio.setLocalAbierto(false);
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horarioNegocio));

            // Act + Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.consultarDisponibilidad(request));
            assertTrue(ex.getMessage().contains("no está abierto"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando hora de inicio es antes de apertura")
        void consultarDisponibilidad_conHoraAntesDeApertura_lanzaIllegalArgumentException() {
            // Arrange
            CitaDisponibilidadRequestDTO request = requestValido();
            request.setHoraInicioCita(LocalTime.of(7, 0));
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horarioNegocio));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.consultarDisponibilidad(request));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando hora de fin excede cierre")
        void consultarDisponibilidad_conHoraDespuesDeCierre_lanzaIllegalArgumentException() {
            // Arrange
            CitaDisponibilidadRequestDTO request = requestValido();
            request.setHoraFinCita(LocalTime.of(19, 0));
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horarioNegocio));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.consultarDisponibilidad(request));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando no hay horario para el día")
        void consultarDisponibilidad_sinHorarioDia_lanzaResourceNotFound() {
            // Arrange
            CitaDisponibilidadRequestDTO request = requestValido();
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> citaAgendamientoService.consultarDisponibilidad(request));
        }
    }

    // ============================================================
    // agendar
    // ============================================================
    @Nested
    @DisplayName("agendar")
    class Agendar {

        @Test
        @DisplayName("retorna CitaDTO cuando todo es válido")
        void agendar_conDatosValidos_retornaCitaDTO() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(usuarioRepository.findByNumeroDocumento("200")).thenReturn(Optional.of(peluquero));
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horarioNegocio));
            when(citaRepository.findConflictsForUpdate(anyString(), any(), anyLong(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
                Cita c = inv.getArgument(0);
                c.setNoCita(1L);
                return c;
            });

            // Act
            CitaDTO resultado = citaAgendamientoService.agendar(createDTO, "100");

            // Assert
            assertEquals(1L, resultado.getNoCita());
            assertEquals("100", resultado.getNumeroDocumentoCliente());
            assertEquals("200", resultado.getNumeroDocumentoPeluquero());
            assertEquals(1L, resultado.getIdEstado());
            assertFalse(resultado.getCitaConfirmada());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando hay conflicto de horario")
        void agendar_conConflicto_lanzaIllegalArgumentException() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(usuarioRepository.findByNumeroDocumento("200")).thenReturn(Optional.of(peluquero));
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horarioNegocio));
            when(citaRepository.findConflictsForUpdate(anyString(), any(), anyLong(), any(), any()))
                    .thenReturn(Arrays.asList(new Cita()));

            // Act + Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.agendar(createDTO, "100"));
            assertTrue(ex.getMessage().contains("solapada"));
            verify(citaRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el cliente está bloqueado")
        void agendar_conClienteBloqueado_lanzaIllegalArgumentException() {
            // Arrange
            cliente.setIdEstado(4);
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));

            // Act + Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.agendar(createDTO, "100"));
            assertTrue(ex.getMessage().contains("bloqueada"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el barbero está deshabilitado")
        void agendar_conBarberoDeshabilitado_lanzaIllegalArgumentException() {
            // Arrange
            peluquero.setIdEstado(5);
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(usuarioRepository.findByNumeroDocumento("200")).thenReturn(Optional.of(peluquero));

            // Act + Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.agendar(createDTO, "100"));
            assertTrue(ex.getMessage().contains("deshabilitado"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el usuario asignado no es barbero")
        void agendar_conPeluqueroNoBarbero_lanzaIllegalArgumentException() {
            // Arrange
            peluquero.setIdRol(3);
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(usuarioRepository.findByNumeroDocumento("200")).thenReturn(Optional.of(peluquero));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.agendar(createDTO, "100"));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando el cliente no existe")
        void agendar_conClienteInexistente_lanzaResourceNotFound() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> citaAgendamientoService.agendar(createDTO, "100"));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando el peluquero no existe")
        void agendar_conPeluqueroInexistente_lanzaResourceNotFound() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(usuarioRepository.findByNumeroDocumento("200")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> citaAgendamientoService.agendar(createDTO, "100"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el request es null")
        void agendar_conRequestNull_lanzaIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.agendar(null, "100"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando numeroDocumentoCliente es blanco")
        void agendar_conClienteBlanco_lanzaIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.agendar(createDTO, " "));
        }

        @Test
        @DisplayName("reintenta una vez ante CannotSerializeTransactionException y termina exitosamente")
        void agendar_conReintentoExitoso_retornaCitaDTO() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(usuarioRepository.findByNumeroDocumento("200")).thenReturn(Optional.of(peluquero));
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horarioNegocio));
            when(citaRepository.findConflictsForUpdate(anyString(), any(), anyLong(), any(), any()))
                    .thenThrow(new CannotSerializeTransactionException("conflict"))
                    .thenReturn(Collections.emptyList());
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
                Cita c = inv.getArgument(0);
                c.setNoCita(1L);
                return c;
            });

            // Act
            CitaDTO resultado = citaAgendamientoService.agendar(createDTO, "100");

            // Assert
            assertEquals(1L, resultado.getNoCita());
            verify(citaRepository, times(2))
                    .findConflictsForUpdate(anyString(), any(), anyLong(), any(), any());
        }

        @Test
        @DisplayName("propaga CannotSerializeTransactionException cuando ambos intentos fallan")
        void agendar_conReintentosAgotados_propagaExcepcion() {
            // Arrange
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(usuarioRepository.findByNumeroDocumento("200")).thenReturn(Optional.of(peluquero));
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horarioNegocio));
            when(citaRepository.findConflictsForUpdate(anyString(), any(), anyLong(), any(), any()))
                    .thenThrow(new CannotSerializeTransactionException("siempre falla"));

            // Act + Assert
            assertThrows(CannotSerializeTransactionException.class,
                    () -> citaAgendamientoService.agendar(createDTO, "100"));
        }
    }

    // ============================================================
    // cancelar
    // ============================================================
    @Nested
    @DisplayName("cancelar")
    class Cancelar {

        private Cita citaActiva() {
            return Cita.builder()
                    .noCita(1L)
                    .numeroDocumentoCliente("100")
                    .numeroDocumentoPeluquero("200")
                    .idEstado(1L)
                    .build();
        }

        @Test
        @DisplayName("cancela la cita cuando el cliente la solicita")
        void cancelar_porClienteValido_cambiaEstadoA3() {
            // Arrange
            Cita cita = citaActiva();
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            CitaDTO resultado = citaAgendamientoService.cancelar(1L, "No puedo asistir", "100");

            // Assert
            assertEquals(3L, resultado.getIdEstado());
            assertEquals("No puedo asistir", resultado.getMotivoCancelacion());
        }

        @Test
        @DisplayName("cancela la cita cuando el peluquero la solicita")
        void cancelar_porPeluqueroValido_cambiaEstadoA3() {
            // Arrange
            Cita cita = citaActiva();
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            CitaDTO resultado = citaAgendamientoService.cancelar(1L, "Reagendar", "200");

            // Assert
            assertEquals(3L, resultado.getIdEstado());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el solicitante no es ni cliente ni peluquero")
        void cancelar_porUsuarioAjeno_lanzaIllegalArgumentException() {
            // Arrange
            Cita cita = citaActiva();
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.cancelar(1L, "motivo", "999"));
            verify(citaRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando la cita no está activa")
        void cancelar_conCitaNoActiva_lanzaIllegalArgumentException() {
            // Arrange
            Cita cita = citaActiva();
            cita.setIdEstado(3L);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.cancelar(1L, "motivo", "100"));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando la cita no existe")
        void cancelar_conCitaInexistente_lanzaResourceNotFound() {
            // Arrange
            when(citaRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> citaAgendamientoService.cancelar(99L, "motivo", "100"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando noCita es null")
        void cancelar_conNoCitaNull_lanzaIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.cancelar(null, "motivo", "100"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el motivo es blanco")
        void cancelar_conMotivoBlanco_lanzaIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.cancelar(1L, " ", "100"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el solicitante es blanco")
        void cancelar_conSolicitanteBlanco_lanzaIllegalArgumentException() {
            // Arrange
            Cita cita = citaActiva();
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.cancelar(1L, "motivo", " "));
        }
    }

    // ============================================================
    // reprogramar
    // ============================================================
    @Nested
    @DisplayName("reprogramar")
    class Reprogramar {

        private CitaReprogramarDTO requestValido() {
            return CitaReprogramarDTO.builder()
                    .fechaCita(fecha)
                    .horaInicioCita(LocalTime.of(11, 0))
                    .horaFinCita(LocalTime.of(12, 0))
                    .build();
        }

        private Cita citaActiva() {
            return Cita.builder()
                    .noCita(1L)
                    .numeroDocumentoCliente("100")
                    .numeroDocumentoPeluquero("200")
                    .idEstado(1L)
                    .build();
        }

        @Test
        @DisplayName("reprograma la cita cuando los datos son válidos")
        void reprogramar_conDatosValidos_actualizaCita() {
            // Arrange
            Cita cita = citaActiva();
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horarioNegocio));
            when(citaRepository.findConflictsForUpdateExcludingNoCita(anyString(), anyLong(), any(), anyLong(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            CitaDTO resultado = citaAgendamientoService.reprogramar(1L, requestValido(), "100");

            // Assert
            assertEquals(LocalTime.of(11, 0), resultado.getHoraInicioCita());
            assertEquals(LocalTime.of(12, 0), resultado.getHoraFinCita());
            assertEquals(1L, resultado.getIdEstado());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando hay conflicto en el nuevo horario")
        void reprogramar_conConflicto_lanzaIllegalArgumentException() {
            // Arrange
            Cita cita = citaActiva();
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
            when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horarioNegocio));
            when(citaRepository.findConflictsForUpdateExcludingNoCita(anyString(), anyLong(), any(), anyLong(), any(), any()))
                    .thenReturn(Arrays.asList(new Cita()));

            // Act + Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.reprogramar(1L, requestValido(), "100"));
            assertTrue(ex.getMessage().contains("solapada"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el solicitante no es el cliente ni peluquero")
        void reprogramar_porUsuarioAjeno_lanzaIllegalArgumentException() {
            // Arrange
            Cita cita = citaActiva();
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.reprogramar(1L, requestValido(), "999"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando la cita no está activa")
        void reprogramar_conCitaNoActiva_lanzaIllegalArgumentException() {
            // Arrange
            Cita cita = citaActiva();
            cita.setIdEstado(6L); // completada
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.reprogramar(1L, requestValido(), "100"));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando la cita no existe")
        void reprogramar_conCitaInexistente_lanzaResourceNotFound() {
            // Arrange
            when(citaRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> citaAgendamientoService.reprogramar(99L, requestValido(), "100"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el cliente del token es blanco")
        void reprogramar_conClienteBlanco_lanzaIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.reprogramar(1L, requestValido(), " "));
        }
    }

    // ============================================================
    // confirmar
    // ============================================================
    @Nested
    @DisplayName("confirmar")
    class Confirmar {

        private Cita citaActiva() {
            return Cita.builder()
                    .noCita(1L)
                    .numeroDocumentoCliente("100")
                    .numeroDocumentoPeluquero("200")
                    .idEstado(1L)
                    .citaConfirmada(false)
                    .build();
        }

        @Test
        @DisplayName("confirma la cita cuando el peluquero asignado es el solicitante")
        void confirmar_porPeluqueroValido_marcaConfirmada() {
            // Arrange
            Cita cita = citaActiva();
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
            when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            CitaDTO resultado = citaAgendamientoService.confirmar(1L, "200");

            // Assert
            assertTrue(resultado.getCitaConfirmada());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando el solicitante no es el peluquero asignado")
        void confirmar_porPeluqueroAjeno_lanzaIllegalArgumentException() {
            // Arrange
            Cita cita = citaActiva();
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.confirmar(1L, "999"));
            verify(citaRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando la cita no está activa")
        void confirmar_conCitaNoActiva_lanzaIllegalArgumentException() {
            // Arrange
            Cita cita = citaActiva();
            cita.setIdEstado(3L);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            // Act + Assert
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.confirmar(1L, "200"));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando la cita no existe")
        void confirmar_conCitaInexistente_lanzaResourceNotFound() {
            // Arrange
            when(citaRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> citaAgendamientoService.confirmar(99L, "200"));
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando noCita es null")
        void confirmar_conNoCitaNull_lanzaIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> citaAgendamientoService.confirmar(null, "200"));
        }
    }
}
