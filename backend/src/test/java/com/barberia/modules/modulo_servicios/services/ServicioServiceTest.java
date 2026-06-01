package com.barberia.modules.modulo_servicios.services;

import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.modules.modulo_servicios.models.dtos.ServicioDTO;
import com.barberia.modules.modulo_servicios.models.entities.Servicio;
import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link ServicioService} siguiendo el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private ServicioService servicioService;

    private Servicio servicio;
    private ServicioDTO servicioDTO;

    @BeforeEach
    void setUp() {
        servicio = Servicio.builder()
                .idServicio(1L)
                .nombreServicio("Corte de cabello")
                .descripcion("Corte clásico")
                .duracion(30)
                .costo(new BigDecimal("20000"))
                .idEstado(1L)
                .build();

        servicioDTO = ServicioDTO.builder()
                .idServicio(1L)
                .nombreServicio("Corte de cabello")
                .descripcion("Corte clásico")
                .duracion(30)
                .costo(new BigDecimal("20000"))
                .idEstado(1L)
                .build();
    }

    // ============================================================
    // obtenerTodos
    // ============================================================
    @Nested
    @DisplayName("obtenerTodos")
    class ObtenerTodos {

        @Test
        @DisplayName("retorna solo los servicios activos (idEstado = 1)")
        void obtenerTodos_conServiciosActivos_retornaListaActiva() {
            // Arrange
            when(servicioRepository.findByIdEstado(1L))
                    .thenReturn(Arrays.asList(servicio));

            // Act
            List<ServicioDTO> resultado = servicioService.obtenerTodos();

            // Assert
            assertEquals(1, resultado.size());
            assertEquals("Corte de cabello", resultado.get(0).getNombreServicio());
            verify(servicioRepository).findByIdEstado(1L);
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay servicios activos")
        void obtenerTodos_sinServiciosActivos_retornaListaVacia() {
            // Arrange
            when(servicioRepository.findByIdEstado(1L)).thenReturn(Collections.emptyList());

            // Act
            List<ServicioDTO> resultado = servicioService.obtenerTodos();

            // Assert
            assertTrue(resultado.isEmpty());
        }
    }

    // ============================================================
    // obtenerPorId
    // ============================================================
    @Nested
    @DisplayName("obtenerPorId")
    class ObtenerPorId {

        @Test
        @DisplayName("retorna ServicioDTO cuando existe")
        void obtenerPorId_conIdValido_retornaServicioDTO() {
            // Arrange
            when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

            // Act
            ServicioDTO resultado = servicioService.obtenerPorId(1L);

            // Assert
            assertNotNull(resultado);
            assertEquals(1L, resultado.getIdServicio());
            assertEquals("Corte de cabello", resultado.getNombreServicio());
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando el id no existe")
        void obtenerPorId_conIdInexistente_lanzaResourceNotFound() {
            // Arrange
            when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> servicioService.obtenerPorId(99L));
            assertTrue(ex.getMessage().contains("99"));
        }
    }

    // ============================================================
    // crear
    // ============================================================
    @Nested
    @DisplayName("crear")
    class Crear {

        @Test
        @DisplayName("guarda el servicio y retorna el DTO creado")
        void crear_conDatosValidos_retornaServicioCreado() {
            // Arrange
            ServicioDTO nuevo = ServicioDTO.builder()
                    .nombreServicio("Tinte")
                    .descripcion("Tinte de barba")
                    .duracion(45)
                    .costo(new BigDecimal("30000"))
                    .build();
            Servicio guardado = Servicio.builder()
                    .idServicio(2L)
                    .nombreServicio("Tinte")
                    .descripcion("Tinte de barba")
                    .duracion(45)
                    .costo(new BigDecimal("30000"))
                    .idEstado(1L)
                    .build();
            when(servicioRepository.save(any(Servicio.class))).thenReturn(guardado);

            // Act
            ServicioDTO resultado = servicioService.crear(nuevo);

            // Assert
            assertEquals(2L, resultado.getIdServicio());
            assertEquals("Tinte", resultado.getNombreServicio());
            assertEquals(1L, resultado.getIdEstado());
            verify(servicioRepository).save(any(Servicio.class));
        }
    }

    // ============================================================
    // actualizar
    // ============================================================
    @Nested
    @DisplayName("actualizar")
    class Actualizar {

        @Test
        @DisplayName("actualiza todos los campos cuando son válidos")
        void actualizar_conTodosLosCampos_actualizaServicio() {
            // Arrange
            ServicioDTO dto = ServicioDTO.builder()
                    .nombreServicio("Nuevo nombre")
                    .descripcion("Nueva descripción")
                    .duracion(60)
                    .costo(new BigDecimal("25000"))
                    .build();
            when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
            when(servicioRepository.save(any(Servicio.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            ServicioDTO resultado = servicioService.actualizar(1L, dto);

            // Assert
            assertEquals("Nuevo nombre", resultado.getNombreServicio());
            assertEquals("Nueva descripción", resultado.getDescripcion());
            assertEquals(60, resultado.getDuracion());
            assertEquals(new BigDecimal("25000"), resultado.getCosto());
        }

        @Test
        @DisplayName("no actualiza la duración cuando es 0 o negativa")
        void actualizar_conDuracionInvalida_noActualizaDuracion() {
            // Arrange
            ServicioDTO dto = ServicioDTO.builder().duracion(0).build();
            when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
            when(servicioRepository.save(any(Servicio.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            ServicioDTO resultado = servicioService.actualizar(1L, dto);

            // Assert
            assertEquals(30, resultado.getDuracion()); // valor original
        }

        @Test
        @DisplayName("conserva campos originales cuando el DTO trae nulls")
        void actualizar_conCamposNulos_conservaValoresOriginales() {
            // Arrange
            ServicioDTO dto = ServicioDTO.builder().build(); // todo null
            when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
            when(servicioRepository.save(any(Servicio.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            ServicioDTO resultado = servicioService.actualizar(1L, dto);

            // Assert
            assertEquals("Corte de cabello", resultado.getNombreServicio());
            assertEquals("Corte clásico", resultado.getDescripcion());
            assertEquals(30, resultado.getDuracion());
            assertEquals(new BigDecimal("20000"), resultado.getCosto());
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando el id no existe")
        void actualizar_conIdInexistente_lanzaResourceNotFound() {
            // Arrange
            when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> servicioService.actualizar(99L, servicioDTO));
            verify(servicioRepository, never()).save(any());
        }
    }

    // ============================================================
    // eliminar
    // ============================================================
    @Nested
    @DisplayName("eliminar")
    class Eliminar {

        @Test
        @DisplayName("elimina el servicio cuando no tiene citas activas")
        void eliminar_sinCitasActivas_eliminaServicio() {
            // Arrange
            when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
            when(citaRepository.existsByIdServicioAndIdEstado(1L, 1L)).thenReturn(false);

            // Act
            servicioService.eliminar(1L);

            // Assert
            verify(servicioRepository).delete(servicio);
        }

        @Test
        @DisplayName("lanza IllegalStateException cuando el servicio tiene citas activas")
        void eliminar_conCitasActivas_lanzaIllegalStateException() {
            // Arrange
            when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
            when(citaRepository.existsByIdServicioAndIdEstado(1L, 1L)).thenReturn(true);

            // Act + Assert
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> servicioService.eliminar(1L));
            assertTrue(ex.getMessage().contains("citas activas"));
            verify(servicioRepository, never()).delete(any(Servicio.class));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando el servicio no existe")
        void eliminar_conIdInexistente_lanzaResourceNotFound() {
            // Arrange
            when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> servicioService.eliminar(99L));
            verify(servicioRepository, never()).delete(any(Servicio.class));
        }
    }

    // ============================================================
    // deshabilitar
    // ============================================================
    @Nested
    @DisplayName("deshabilitar")
    class Deshabilitar {

        @Test
        @DisplayName("cambia el idEstado a 2 cuando el servicio existe")
        void deshabilitar_conServicioExistente_cambiaEstadoA2() {
            // Arrange
            when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
            when(servicioRepository.save(any(Servicio.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            ServicioDTO resultado = servicioService.deshabilitar(1L);

            // Assert
            assertEquals(2L, resultado.getIdEstado());
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando el servicio no existe")
        void deshabilitar_conIdInexistente_lanzaResourceNotFound() {
            // Arrange
            when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> servicioService.deshabilitar(99L));
            verify(servicioRepository, never()).save(any());
        }
    }
}
