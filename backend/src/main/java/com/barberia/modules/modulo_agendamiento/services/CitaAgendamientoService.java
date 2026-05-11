package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_agendamiento.mappers.CitaMapper;
import com.barberia.modules.modulo_agendamiento.models.dtos.*;
import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_agendamiento.repositories.CitaRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CitaAgendamientoService {

    private static final Long ESTADO_ACTIVO = 1L;
    private static final Long ESTADO_CANCELADA = 3L;

    private final CitaRepository citaRepository;
    private final HorarioValidator horarioValidator;
    private final ConflictoCitaChecker conflictoChecker;
    private final CitaMapper citaMapper;

    public CitaAgendamientoService(CitaRepository citaRepository,
                                   HorarioValidator horarioValidator,
                                   ConflictoCitaChecker conflictoChecker,
                                   CitaMapper citaMapper) {
        this.citaRepository = citaRepository;
        this.horarioValidator = horarioValidator;
        this.conflictoChecker = conflictoChecker;
        this.citaMapper = citaMapper;
    }

    public CitaDisponibilidadResponseDTO consultarDisponibilidad(CitaDisponibilidadRequestDTO request) {
        horarioValidator.validar(request.getFechaCita(), request.getHoraInicioCita(), request.getHoraFinCita());
        boolean disponible = !conflictoChecker.hayConflicto(
                request.getNumeroDocumentoPeluquero(),
                request.getFechaCita(),
                request.getHoraInicioCita(),
                request.getHoraFinCita());
        return citaMapper.toDisponibilidadResponse(request, disponible);
    }

    @Retryable(retryFor = ConcurrencyFailureException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 50))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CitaDTO agendar(CitaCreateDTO request, String numeroDocumentoCliente) {
        if (numeroDocumentoCliente == null || numeroDocumentoCliente.isBlank()) {
            throw new IllegalArgumentException("numeroDocumentoCliente no encontrado en el token");
        }
        horarioValidator.validar(request.getFechaCita(), request.getHoraInicioCita(), request.getHoraFinCita());
        conflictoChecker.validarSinConflictosParaAgendar(
                request.getNumeroDocumentoPeluquero(),
                request.getFechaCita(),
                request.getHoraInicioCita(),
                request.getHoraFinCita());

        Cita cita = citaMapper.toEntity(request, numeroDocumentoCliente);
        return citaMapper.toDto(citaRepository.save(cita));
    }

    @Transactional
    public CitaDTO cancelar(Long noCita) {
        Cita cita = buscarCita(noCita);
        verificarActiva(cita, "La cita no está activa o ya fue gestionada");
        cita.setIdEstado(ESTADO_CANCELADA);
        return citaMapper.toDto(citaRepository.save(cita));
    }

    @Retryable(retryFor = ConcurrencyFailureException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 50))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CitaDTO reprogramar(Long noCita, CitaReprogramarDTO request) {
        Cita cita = buscarCita(noCita);
        verificarActiva(cita, "La cita no está activa o ya fue gestionada");
        horarioValidator.validar(request.getFechaCita(), request.getHoraInicioCita(), request.getHoraFinCita());
        conflictoChecker.validarSinConflictosParaReprogramar(
                cita, request.getFechaCita(), request.getHoraInicioCita(), request.getHoraFinCita());

        cita.setFechaCita(request.getFechaCita());
        cita.setHoraInicioCita(request.getHoraInicioCita());
        cita.setHoraFinCita(request.getHoraFinCita());
        cita.setIdEstado(ESTADO_ACTIVO);
        return citaMapper.toDto(citaRepository.save(cita));
    }

    @Transactional
    public CitaDTO confirmar(Long noCita, String numeroDocumentoPeluquero) {
        if (numeroDocumentoPeluquero == null || numeroDocumentoPeluquero.isBlank()) {
            throw new IllegalArgumentException("numeroDocumentoPeluquero es requerido");
        }
        Cita cita = buscarCita(noCita);
        if (!numeroDocumentoPeluquero.equals(cita.getNumeroDocumentoPeluquero())) {
            throw new IllegalArgumentException("No puedes confirmar una cita que no te pertenece");
        }
        verificarActiva(cita, "La cita no está activa");
        cita.setCitaConfirmada(true);
        return citaMapper.toDto(citaRepository.save(cita));
    }

    private Cita buscarCita(Long noCita) {
        return citaRepository.findById(noCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada para noCita: " + noCita));
    }

    private void verificarActiva(Cita cita, String mensaje) {
        if (!ESTADO_ACTIVO.equals(cita.getIdEstado())) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}