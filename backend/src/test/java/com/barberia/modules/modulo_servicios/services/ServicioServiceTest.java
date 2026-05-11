package com.barberia.modules.modulo_servicios.services;

import com.barberia.modules.modulo_servicios.models.dtos.ServicioDTO;
import com.barberia.modules.modulo_servicios.models.entities.Servicio;
import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private ServicioService servicioService;

    @Test
    void obtenerTodos_DeberiaRetornarServiciosActivos() {
        Servicio servicioUno = crearServicio(
                1L,
                "Corte clásico",
                "Corte de cabello tradicional",
                1L
        );

        Servicio servicioDos = crearServicio(
                2L,
                "Barba",
                "Arreglo de barba",
                1L
        );

        when(servicioRepository.findByIdEstado(1L)).thenReturn(List.of(servicioUno, servicioDos));

        List<ServicioDTO> resultado = servicioService.obtenerTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Corte clásico", resultado.get(0).getNombreServicio());
        assertEquals("Barba", resultado.get(1).getNombreServicio());

        verify(servicioRepository).findByIdEstado(1L);
    }

    @Test
    void obtenerTodos_DeberiaRetornarListaVaciaCuandoNoHayServiciosActivos() {
        when(servicioRepository.findByIdEstado(1L)).thenReturn(List.of());

        List<ServicioDTO> resultado = servicioService.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(servicioRepository).findByIdEstado(1L);
    }

    @Test
    void obtenerPorId_DeberiaRetornarServicioCuandoExiste() {
        Servicio servicio = crearServicio(
                1L,
                "Corte clásico",
                "Corte de cabello tradicional",
                1L
        );

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        ServicioDTO resultado = servicioService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdServicio());
        assertEquals("Corte clásico", resultado.getNombreServicio());
        assertEquals("Corte de cabello tradicional", resultado.getDescripcion());

        verify(servicioRepository).findById(1L);
    }

    @Test
    void obtenerPorId_DeberiaLanzarErrorCuandoServicioNoExiste() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> servicioService.obtenerPorId(99L)
        );

        assertEquals("Servicio no encontrado con id: 99", exception.getMessage());

        verify(servicioRepository).findById(99L);
    }

    @Test
    void crear_DeberiaGuardarServicioCorrectamente() {
        ServicioDTO servicioDTO = ServicioDTO.builder()
                .nombreServicio("Corte clásico")
                .descripcion("Corte de cabello tradicional")
                .build();

        when(servicioRepository.save(any(Servicio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServicioDTO resultado = servicioService.crear(servicioDTO);

        assertNotNull(resultado);
        assertEquals("Corte clásico", resultado.getNombreServicio());
        assertEquals("Corte de cabello tradicional", resultado.getDescripcion());

        ArgumentCaptor<Servicio> servicioCaptor = ArgumentCaptor.forClass(Servicio.class);

        verify(servicioRepository).save(servicioCaptor.capture());

        Servicio servicioGuardado = servicioCaptor.getValue();

        assertEquals("Corte clásico", servicioGuardado.getNombreServicio());
        assertEquals("Corte de cabello tradicional", servicioGuardado.getDescripcion());
    }

    private Servicio crearServicio(
            Long idServicio,
            String nombreServicio,
            String descripcion,
            Long idEstado
    ) {
        return Servicio.builder()
                .idServicio(idServicio)
                .nombreServicio(nombreServicio)
                .descripcion(descripcion)
                .idEstado(idEstado)
                .build();
    }
}