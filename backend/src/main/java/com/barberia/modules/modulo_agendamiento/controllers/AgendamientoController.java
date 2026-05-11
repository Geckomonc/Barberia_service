package com.barberia.modules.modulo_agendamiento.controllers;

import com.barberia.modules.modulo_agendamiento.models.dtos.*;
import com.barberia.modules.modulo_agendamiento.services.CitaAgendamientoService;
import com.barberia.shared.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/agendamiento")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AgendamientoController {

    private final CitaAgendamientoService citaAgendamientoService;

    public AgendamientoController(CitaAgendamientoService citaAgendamientoService) {
        this.citaAgendamientoService = citaAgendamientoService;
    }

    @PostMapping("/disponibilidad")
    public ResponseEntity<ApiResponse<CitaDisponibilidadResponseDTO>> consultarDisponibilidad(
            @Valid @RequestBody CitaDisponibilidadRequestDTO request) {
        CitaDisponibilidadResponseDTO resp = citaAgendamientoService.consultarDisponibilidad(request);
        return ResponseEntity.ok(ApiResponse.success("Disponibilidad consultada", resp));
    }

    @PostMapping("/agendar")
    public ResponseEntity<ApiResponse<CitaDTO>> agendar(
            @Valid @RequestBody CitaCreateDTO request,
            Authentication authentication) {
        String numeroDocCliente = extraerNumeroDocumento(authentication);
        CitaDTO creado = citaAgendamientoService.agendar(request, numeroDocCliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Cita agendada", creado));
    }

    @PostMapping("/cancelar/{noCita}")
    public ResponseEntity<ApiResponse<CitaDTO>> cancelar(@PathVariable Long noCita) {
        CitaDTO cancelada = citaAgendamientoService.cancelar(noCita);
        return ResponseEntity.ok(ApiResponse.success("Cita cancelada", cancelada));
    }

    @PutMapping("/confirmar/{noCita}")
    public ResponseEntity<ApiResponse<CitaDTO>> confirmar(
            @PathVariable Long noCita,
            Authentication authentication) {
        String numeroDocPeluquero = extraerNumeroDocumento(authentication);
        CitaDTO confirmada = citaAgendamientoService.confirmar(noCita, numeroDocPeluquero);
        return ResponseEntity.ok(ApiResponse.success("Cita confirmada", confirmada));
    }

    @PutMapping("/reprogramar/{noCita}")
    public ResponseEntity<ApiResponse<CitaDTO>> reprogramar(
            @PathVariable Long noCita,
            @Valid @RequestBody CitaReprogramarDTO request) {
        CitaDTO reprogramada = citaAgendamientoService.reprogramar(noCita, request);
        return ResponseEntity.ok(ApiResponse.success("Cita reprogramada", reprogramada));
    }

    @SuppressWarnings("unchecked")
    private String extraerNumeroDocumento(Authentication authentication) {
        return (String) ((Map<String, Object>) authentication.getDetails()).get("numeroDocumento");
    }
}
