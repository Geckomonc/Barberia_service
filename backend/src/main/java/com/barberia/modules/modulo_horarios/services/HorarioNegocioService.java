package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_horarios.models.dtos.HorarioNegocioDTO;
import com.barberia.modules.modulo_horarios.models.dtos.HorarioUpdateDTO;
import com.barberia.modules.modulo_horarios.models.entities.HorarioNegocio;
import com.barberia.modules.modulo_horarios.repositories.HorarioNegocioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HorarioNegocioService {

    private static final String MENSAJE_HORARIO_NO_ENCONTRADO = "Horario no encontrado para el día: ";
    @Autowired
    private HorarioNegocioRepository horarioNegocioRepository;

    public List<HorarioNegocioDTO> obtenerTodos() {
        return horarioNegocioRepository.findAllByOrderByIdDiaAsc()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public HorarioNegocioDTO obtenerPorDia(Long idDia) {
        HorarioNegocio horario = horarioNegocioRepository.findByIdDia(idDia)
                .orElseThrow(() -> new ResourceNotFoundException(MENSAJE_HORARIO_NO_ENCONTRADO + idDia));
        return convertirADTO(horario);
    }

    public HorarioNegocioDTO actualizarHorario(Long idDia, HorarioUpdateDTO dto) {
        HorarioNegocio horario = horarioNegocioRepository.findByIdDia(idDia)
                .orElseThrow(() -> new ResourceNotFoundException(MENSAJE_HORARIO_NO_ENCONTRADO + idDia));

        if (dto.getHoraApertura() == null || dto.getHoraCierre() == null) {
            throw new IllegalArgumentException("Error: Debe especificar hora de apertura y cierre");
        }

        LocalTime apertura;
        LocalTime cierre;
        try {
            apertura = LocalTime.parse(dto.getHoraApertura());
            cierre = LocalTime.parse(dto.getHoraCierre());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Error: Formato de hora inválido. Use HH:mm:ss (ej: 07:00:00)");
        }

        if (cierre.isBefore(apertura)) {
            throw new IllegalArgumentException("Error: La hora de cierre no puede ser anterior a la de apertura");
        }

        horario.setHoraApertura(apertura);
        horario.setHoraCierre(cierre);
        horario.setLocalAbierto(true);

        return convertirADTO(horarioNegocioRepository.save(horario));
    }

    public HorarioNegocioDTO cerrarDia(Long idDia) {
        HorarioNegocio horario = horarioNegocioRepository.findByIdDia(idDia)
                .orElseThrow(() -> new ResourceNotFoundException(MENSAJE_HORARIO_NO_ENCONTRADO + idDia));

        horario.setLocalAbierto(false);

        return convertirADTO(horarioNegocioRepository.save(horario));
    }

    private HorarioNegocioDTO convertirADTO(HorarioNegocio horario) {
        return HorarioNegocioDTO.builder()
                .idHorarioNegocio(horario.getIdHorarioNegocio())
                .idDia(horario.getIdDia())
                .horaApertura(horario.getHoraApertura())
                .horaCierre(horario.getHoraCierre())
                .localAbierto(horario.getLocalAbierto())
                .build();
    }
}
