package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_agendamiento.repositories.CitaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class ConflictoCitaChecker {

    private static final Long ESTADO_ACTIVO = 1L;

    private final CitaRepository citaRepository;

    public ConflictoCitaChecker(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public boolean hayConflicto(String numeroDocPeluquero, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        return !citaRepository.findConflicts(numeroDocPeluquero, fecha, ESTADO_ACTIVO, inicio, fin).isEmpty();
    }

    public void validarSinConflictosParaAgendar(String numeroDocPeluquero, LocalDate fecha,
                                                LocalTime inicio, LocalTime fin) {
        List<Cita> conflictos = citaRepository.findConflictsForUpdate(
                numeroDocPeluquero, fecha, ESTADO_ACTIVO, inicio, fin);
        if (!conflictos.isEmpty()) {
            throw new IllegalArgumentException("No se puede agendar: existe una cita solapada");
        }
    }

    public void validarSinConflictosParaReprogramar(Cita cita, LocalDate fecha,
                                                    LocalTime inicio, LocalTime fin) {
        List<Cita> conflictos = citaRepository.findConflictsForUpdateExcludingNoCita(
                cita.getNumeroDocumentoPeluquero(), cita.getNoCita(), fecha, ESTADO_ACTIVO, inicio, fin);
        if (!conflictos.isEmpty()) {
            throw new IllegalArgumentException("No se puede reprogramar: existe una cita solapada");
        }
    }
}