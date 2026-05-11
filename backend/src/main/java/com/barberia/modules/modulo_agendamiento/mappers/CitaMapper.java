package com.barberia.modules.modulo_agendamiento.mappers;

import com.barberia.modules.modulo_agendamiento.models.dtos.*;
import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CitaMapper {

    private static final Long ESTADO_ACTIVO = 1L;

    public Cita toEntity(CitaCreateDTO request, String numeroDocCliente) {
        return Cita.builder()
                .numeroDocumentoCliente(numeroDocCliente)
                .numeroDocumentoPeluquero(request.getNumeroDocumentoPeluquero())
                .idServicio(request.getIdServicio())
                .fechaCita(request.getFechaCita())
                .horaInicioCita(request.getHoraInicioCita())
                .horaFinCita(request.getHoraFinCita())
                .idEstado(ESTADO_ACTIVO)
                .citaConfirmada(false)
                .fechaCreacion(Instant.now())
                .build();
    }

    public CitaDTO toDto(Cita cita) {
        return CitaDTO.builder()
                .noCita(cita.getNoCita())
                .numeroDocumentoCliente(cita.getNumeroDocumentoCliente())
                .numeroDocumentoPeluquero(cita.getNumeroDocumentoPeluquero())
                .idServicio(cita.getIdServicio())
                .fechaCita(cita.getFechaCita())
                .horaInicioCita(cita.getHoraInicioCita())
                .horaFinCita(cita.getHoraFinCita())
                .idEstado(cita.getIdEstado())
                .citaConfirmada(cita.getCitaConfirmada())
                .fechaCreacion(cita.getFechaCreacion())
                .build();
    }

    public CitaDisponibilidadResponseDTO toDisponibilidadResponse(
            CitaDisponibilidadRequestDTO request, boolean disponible) {
        String mensaje = disponible ? "Disponible" : "No disponible: existe una cita solapada";
        return CitaDisponibilidadResponseDTO.builder()
                .disponible(disponible)
                .mensaje(mensaje)
                .numeroDocumentoPeluquero(request.getNumeroDocumentoPeluquero())
                .idServicio(request.getIdServicio())
                .fechaCita(request.getFechaCita())
                .horaInicioCita(request.getHoraInicioCita())
                .horaFinCita(request.getHoraFinCita())
                .build();
    }
}