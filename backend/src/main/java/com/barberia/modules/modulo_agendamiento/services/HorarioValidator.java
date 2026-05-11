package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_horarios.models.entities.HorarioNegocio;
import com.barberia.modules.modulo_horarios.repositories.HorarioNegocioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class HorarioValidator {

    private final HorarioNegocioRepository horarioNegocioRepository;

    public HorarioValidator(HorarioNegocioRepository horarioNegocioRepository) {
        this.horarioNegocioRepository = horarioNegocioRepository;
    }

    public void validar(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        long idDia = fecha.getDayOfWeek().getValue();
        HorarioNegocio horario = horarioNegocioRepository.findByIdDia(idDia)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado para el día: " + idDia));

        if (!Boolean.TRUE.equals(horario.getLocalAbierto())) {
            throw new IllegalArgumentException("El local no está abierto para la fecha seleccionada");
        }
        if (horaInicio.isBefore(horario.getHoraApertura())) {
            throw new IllegalArgumentException("La hora de inicio está fuera del horario de apertura");
        }
        if (horaFin.isAfter(horario.getHoraCierre())) {
            throw new IllegalArgumentException("La cita excede el horario de cierre");
        }
    }
}