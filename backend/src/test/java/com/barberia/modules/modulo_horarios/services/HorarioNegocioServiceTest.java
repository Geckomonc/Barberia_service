package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_horarios.models.dtos.HorarioNegocioDTO;
import com.barberia.modules.modulo_horarios.models.dtos.HorarioUpdateDTO;
import com.barberia.modules.modulo_horarios.models.entities.HorarioNegocio;
import com.barberia.modules.modulo_horarios.repositories.HorarioNegocioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioNegocioServiceTest {

    @Mock
    private HorarioNegocioRepository horarioNegocioRepository;

    @InjectMocks
    private HorarioNegocioService horarioNegocioService;

    @Test
    void obtenerTodos_DeberiaRetornarTodosLosHorariosOrdenadosPorDia() {
        HorarioNegocio horarioUno = crearHorario(
                1L,
                1L,
                LocalTime.of(7, 0),
                LocalTime.of(18, 0),
                true
        );

        HorarioNegocio horarioDos = crearHorario(
                2L,
                2L,
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                true
        );

        when(horarioNegocioRepository.findAllByOrderByIdDiaAsc()).thenReturn(List.of(horarioUno, horarioDos));

        List<HorarioNegocioDTO> resultado = horarioNegocioService.obtenerTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getIdDia());
        assertEquals(2L, resultado.get(1).getIdDia());
        assertEquals(LocalTime.of(7, 0), resultado.get(0).getHoraApertura());
        assertEquals(LocalTime.of(17, 0), resultado.get(1).getHoraCierre());

        verify(horarioNegocioRepository).findAllByOrderByIdDiaAsc();
    }

    @Test
    void obtenerTodos_DeberiaRetornarListaVaciaCuandoNoHayHorarios() {
        when(horarioNegocioRepository.findAllByOrderByIdDiaAsc()).thenReturn(List.of());

        List<HorarioNegocioDTO> resultado = horarioNegocioService.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(horarioNegocioRepository).findAllByOrderByIdDiaAsc();
    }

    @Test
    void obtenerPorDia_DeberiaRetornarHorarioCuandoExiste() {
        HorarioNegocio horario = crearHorario(
                1L,
                1L,
                LocalTime.of(7, 0),
                LocalTime.of(18, 0),
                true
        );

        when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));

        HorarioNegocioDTO resultado = horarioNegocioService.obtenerPorDia(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdHorarioNegocio());
        assertEquals(1L, resultado.getIdDia());
        assertEquals(LocalTime.of(7, 0), resultado.getHoraApertura());
        assertEquals(LocalTime.of(18, 0), resultado.getHoraCierre());
        assertTrue(resultado.getLocalAbierto());

        verify(horarioNegocioRepository).findByIdDia(1L);
    }

    @Test
    void obtenerPorDia_DeberiaLanzarErrorCuandoNoExiste() {
        when(horarioNegocioRepository.findByIdDia(9L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> horarioNegocioService.obtenerPorDia(9L)
        );

        assertEquals("Horario no encontrado para el día: 9", exception.getMessage());

        verify(horarioNegocioRepository).findByIdDia(9L);
    }

    @Test
    void actualizarHorario_DeberiaActualizarHorarioCorrectamente() {
        HorarioNegocio horario = crearHorario(
                1L,
                1L,
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                false
        );

        HorarioUpdateDTO dto = mock(HorarioUpdateDTO.class);

        when(dto.getHoraApertura()).thenReturn("07:00:00");
        when(dto.getHoraCierre()).thenReturn("18:00:00");
        when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));
        when(horarioNegocioRepository.save(any(HorarioNegocio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HorarioNegocioDTO resultado = horarioNegocioService.actualizarHorario(1L, dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdDia());
        assertEquals(LocalTime.of(7, 0), resultado.getHoraApertura());
        assertEquals(LocalTime.of(18, 0), resultado.getHoraCierre());
        assertTrue(resultado.getLocalAbierto());

        verify(horarioNegocioRepository).save(horario);
    }

    @Test
    void actualizarHorario_DeberiaLanzarErrorCuandoHorarioNoExiste() {
        HorarioUpdateDTO dto = mock(HorarioUpdateDTO.class);

        when(horarioNegocioRepository.findByIdDia(9L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> horarioNegocioService.actualizarHorario(9L, dto)
        );

        assertEquals("Horario no encontrado para el día: 9", exception.getMessage());

        verify(horarioNegocioRepository, never()).save(any(HorarioNegocio.class));
    }

    @Test
    void actualizarHorario_DeberiaLanzarErrorCuandoHoraAperturaEsNula() {
        HorarioNegocio horario = crearHorario(
                1L,
                1L,
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                true
        );

        HorarioUpdateDTO dto = mock(HorarioUpdateDTO.class);

        when(dto.getHoraApertura()).thenReturn(null);
        when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> horarioNegocioService.actualizarHorario(1L, dto)
        );

        assertEquals("Error: Debe especificar hora de apertura y cierre", exception.getMessage());

        verify(horarioNegocioRepository, never()).save(any(HorarioNegocio.class));
    }

    @Test
    void actualizarHorario_DeberiaLanzarErrorCuandoHoraCierreEsNula() {
        HorarioNegocio horario = crearHorario(
                1L,
                1L,
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                true
        );

        HorarioUpdateDTO dto = mock(HorarioUpdateDTO.class);

        when(dto.getHoraApertura()).thenReturn("07:00:00");
        when(dto.getHoraCierre()).thenReturn(null);
        when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> horarioNegocioService.actualizarHorario(1L, dto)
        );

        assertEquals("Error: Debe especificar hora de apertura y cierre", exception.getMessage());

        verify(horarioNegocioRepository, never()).save(any(HorarioNegocio.class));
    }

    @Test
    void actualizarHorario_DeberiaLanzarErrorCuandoFormatoDeHoraEsInvalido() {
        HorarioNegocio horario = crearHorario(
                1L,
                1L,
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                true
        );

        HorarioUpdateDTO dto = mock(HorarioUpdateDTO.class);

        when(dto.getHoraApertura()).thenReturn("hora-invalida");
        when(dto.getHoraCierre()).thenReturn("18:00:00");
        when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> horarioNegocioService.actualizarHorario(1L, dto)
        );

        assertEquals("Error: Formato de hora inválido. Use HH:mm:ss (ej: 07:00:00)", exception.getMessage());

        verify(horarioNegocioRepository, never()).save(any(HorarioNegocio.class));
    }

    @Test
    void actualizarHorario_DeberiaLanzarErrorCuandoCierreEsAnteriorAApertura() {
        HorarioNegocio horario = crearHorario(
                1L,
                1L,
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                true
        );

        HorarioUpdateDTO dto = mock(HorarioUpdateDTO.class);

        when(dto.getHoraApertura()).thenReturn("18:00:00");
        when(dto.getHoraCierre()).thenReturn("07:00:00");
        when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> horarioNegocioService.actualizarHorario(1L, dto)
        );

        assertEquals("Error: La hora de cierre no puede ser anterior a la de apertura", exception.getMessage());

        verify(horarioNegocioRepository, never()).save(any(HorarioNegocio.class));
    }

    @Test
    void cerrarDia_DeberiaCerrarElDiaCorrectamente() {
        HorarioNegocio horario = crearHorario(
                1L,
                1L,
                LocalTime.of(7, 0),
                LocalTime.of(18, 0),
                true
        );

        when(horarioNegocioRepository.findByIdDia(1L)).thenReturn(Optional.of(horario));
        when(horarioNegocioRepository.save(any(HorarioNegocio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HorarioNegocioDTO resultado = horarioNegocioService.cerrarDia(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdDia());
        assertFalse(resultado.getLocalAbierto());

        verify(horarioNegocioRepository).save(horario);
    }

    @Test
    void cerrarDia_DeberiaLanzarErrorCuandoHorarioNoExiste() {
        when(horarioNegocioRepository.findByIdDia(9L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> horarioNegocioService.cerrarDia(9L)
        );

        assertEquals("Horario no encontrado para el día: 9", exception.getMessage());

        verify(horarioNegocioRepository, never()).save(any(HorarioNegocio.class));
    }

    private HorarioNegocio crearHorario(
            Long idHorarioNegocio,
            Long idDia,
            LocalTime horaApertura,
            LocalTime horaCierre,
            Boolean localAbierto
    ) {
        return HorarioNegocio.builder()
                .idHorarioNegocio(idHorarioNegocio)
                .idDia(idDia)
                .horaApertura(horaApertura)
                .horaCierre(horaCierre)
                .localAbierto(localAbierto)
                .build();
    }
}