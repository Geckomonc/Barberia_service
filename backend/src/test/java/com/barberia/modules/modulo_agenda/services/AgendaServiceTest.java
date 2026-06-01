package com.barberia.modules.modulo_agenda.services;

import com.barberia.modules.modulo_agenda.models.dtos.AgendaResponseDTO;
import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.modules.modulo_servicios.models.entities.Servicio;
import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link AgendaService} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private AgendaService agendaService;

    private Cita cita;
    private Usuario cliente;
    private Servicio servicio;

    @BeforeEach
    void setUp() {
        cita = Cita.builder()
                .noCita(1L)
                .numeroDocumentoCliente("100")
                .numeroDocumentoPeluquero("200")
                .idServicio(10L)
                .fechaCita(LocalDate.of(2026, 6, 1))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .idEstado(1L)
                .build();

        cliente = Usuario.builder()
                .numeroDocumento("100")
                .nombrePersona("Juan Pérez")
                .build();

        servicio = Servicio.builder()
                .idServicio(10L)
                .nombreServicio("Corte de cabello")
                .duracion(60)
                .costo(new BigDecimal("20000"))
                .build();
    }

    @Nested
    @DisplayName("obtenerAgendaPeluquero - estructura básica")
    class EstructuraBasica {

        @Test
        @DisplayName("retorna DTO con datos completos cuando cliente y servicio existen")
        void obtenerAgendaPeluquero_conCitaCompleta_retornaDTOConDatos() {
            // Arrange
            when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc("200", 1L))
                    .thenReturn(Arrays.asList(cita));
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById(10L)).thenReturn(Optional.of(servicio));

            // Act
            List<AgendaResponseDTO> resultado = agendaService.obtenerAgendaPeluquero("200");

            // Assert
            assertEquals(1, resultado.size());
            AgendaResponseDTO dto = resultado.get(0);
            assertEquals(1L, dto.getNoCita());
            assertEquals("Juan Pérez", dto.getNombreCliente());
            assertEquals("Corte de cabello", dto.getNombreServicio());
            assertEquals(LocalDate.of(2026, 6, 1), dto.getFecha());
            assertEquals(LocalTime.of(9, 0), dto.getHoraInicio());
            assertEquals(LocalTime.of(10, 0), dto.getHoraFin());
            assertEquals("ACTIVO", dto.getEstado());
        }

        @Test
        @DisplayName("retorna lista vacía cuando el peluquero no tiene citas activas")
        void obtenerAgendaPeluquero_sinCitas_retornaListaVacia() {
            // Arrange
            when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc("200", 1L))
                    .thenReturn(Collections.emptyList());

            // Act
            List<AgendaResponseDTO> resultado = agendaService.obtenerAgendaPeluquero("200");

            // Assert
            assertTrue(resultado.isEmpty());
            verify(usuarioRepository, never()).findByNumeroDocumento(anyString());
            verify(servicioRepository, never()).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("obtenerAgendaPeluquero - fallbacks por entidad faltante")
    class Fallbacks {

        @Test
        @DisplayName("retorna nombreCliente 'Desconocido' cuando el cliente no existe")
        void obtenerAgendaPeluquero_sinCliente_retornaNombreDesconocido() {
            // Arrange
            when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc("200", 1L))
                    .thenReturn(Arrays.asList(cita));
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.empty());
            when(servicioRepository.findById(10L)).thenReturn(Optional.of(servicio));

            // Act
            List<AgendaResponseDTO> resultado = agendaService.obtenerAgendaPeluquero("200");

            // Assert
            assertEquals("Desconocido", resultado.get(0).getNombreCliente());
            assertEquals("Corte de cabello", resultado.get(0).getNombreServicio());
        }

        @Test
        @DisplayName("retorna nombreServicio 'Desconocido' cuando el servicio no existe")
        void obtenerAgendaPeluquero_sinServicio_retornaNombreDesconocido() {
            // Arrange
            when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc("200", 1L))
                    .thenReturn(Arrays.asList(cita));
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById(10L)).thenReturn(Optional.empty());

            // Act
            List<AgendaResponseDTO> resultado = agendaService.obtenerAgendaPeluquero("200");

            // Assert
            assertEquals("Juan Pérez", resultado.get(0).getNombreCliente());
            assertEquals("Desconocido", resultado.get(0).getNombreServicio());
        }
    }

    @Nested
    @DisplayName("obtenerAgendaPeluquero - mapeo de estados")
    class MapeoEstados {

        @Test
        @DisplayName("idEstado 1 → ACTIVO")
        void obtenerAgendaPeluquero_estado1_retornaActivo() {
            // Arrange
            cita.setIdEstado(1L);
            when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc("200", 1L))
                    .thenReturn(Arrays.asList(cita));
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById(10L)).thenReturn(Optional.of(servicio));

            // Act
            List<AgendaResponseDTO> resultado = agendaService.obtenerAgendaPeluquero("200");

            // Assert
            assertEquals("ACTIVO", resultado.get(0).getEstado());
        }

        @Test
        @DisplayName("idEstado 3 → CANCELADA (cuando se mapea manualmente desde otra cita)")
        void obtenerAgendaPeluquero_estado3_retornaCancelada() {
            // Arrange
            cita.setIdEstado(3L);
            when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc("200", 1L))
                    .thenReturn(Arrays.asList(cita));
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById(10L)).thenReturn(Optional.of(servicio));

            // Act
            List<AgendaResponseDTO> resultado = agendaService.obtenerAgendaPeluquero("200");

            // Assert
            assertEquals("CANCELADA", resultado.get(0).getEstado());
        }

        @Test
        @DisplayName("idEstado 6 → COMPLETADA")
        void obtenerAgendaPeluquero_estado6_retornaCompletada() {
            // Arrange
            cita.setIdEstado(6L);
            when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc("200", 1L))
                    .thenReturn(Arrays.asList(cita));
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById(10L)).thenReturn(Optional.of(servicio));

            // Act
            List<AgendaResponseDTO> resultado = agendaService.obtenerAgendaPeluquero("200");

            // Assert
            assertEquals("COMPLETADA", resultado.get(0).getEstado());
        }

        @Test
        @DisplayName("idEstado null → DESCONOCIDO")
        void obtenerAgendaPeluquero_estadoNull_retornaDesconocido() {
            // Arrange
            cita.setIdEstado(null);
            when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc("200", 1L))
                    .thenReturn(Arrays.asList(cita));
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById(10L)).thenReturn(Optional.of(servicio));

            // Act
            List<AgendaResponseDTO> resultado = agendaService.obtenerAgendaPeluquero("200");

            // Assert
            assertEquals("DESCONOCIDO", resultado.get(0).getEstado());
        }

        @Test
        @DisplayName("idEstado desconocido (ej. 99) → retorna el número como string")
        void obtenerAgendaPeluquero_estadoNoMapeado_retornaIdComoString() {
            // Arrange
            cita.setIdEstado(99L);
            when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc("200", 1L))
                    .thenReturn(Arrays.asList(cita));
            when(usuarioRepository.findByNumeroDocumento("100")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById(10L)).thenReturn(Optional.of(servicio));

            // Act
            List<AgendaResponseDTO> resultado = agendaService.obtenerAgendaPeluquero("200");

            // Assert
            assertEquals("99", resultado.get(0).getEstado());
        }
    }
}
