package com.barberia.modules.modulo_agendamiento.repositories;

import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository("citaAgendamientoRepository")
public interface CitaRepository extends JpaRepository<Cita, Long> {

    @Query("SELECT c FROM CitaAgendamiento c WHERE c.numeroDocumentoPeluquero = :numeroDocumentoPeluquero " +
            "AND c.fechaCita = :fechaCita AND c.idEstado = :estadoActivo " +
            "AND c.horaInicioCita < :fin AND c.horaFinCita > :inicio")
    List<Cita> findConflicts(@Param("numeroDocumentoPeluquero") String numeroDocumentoPeluquero,
                             @Param("fechaCita") LocalDate fechaCita,
                             @Param("estadoActivo") long estadoActivo,
                             @Param("inicio") LocalTime inicio,
                             @Param("fin") LocalTime fin);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CitaAgendamiento c WHERE c.numeroDocumentoPeluquero = :numeroDocumentoPeluquero " +
            "AND c.fechaCita = :fechaCita AND c.idEstado = :estadoActivo " +
            "AND c.horaInicioCita < :fin AND c.horaFinCita > :inicio")
    List<Cita> findConflictsForUpdate(@Param("numeroDocumentoPeluquero") String numeroDocumentoPeluquero,
                                      @Param("fechaCita") LocalDate fechaCita,
                                      @Param("estadoActivo") long estadoActivo,
                                      @Param("inicio") LocalTime inicio,
                                      @Param("fin") LocalTime fin);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CitaAgendamiento c WHERE c.numeroDocumentoPeluquero = :numeroDocumentoPeluquero " +
            "AND c.noCita != :noCita AND c.fechaCita = :fechaCita AND c.idEstado = :estadoActivo " +
            "AND c.horaInicioCita < :fin AND c.horaFinCita > :inicio")
    List<Cita> findConflictsForUpdateExcludingNoCita(@Param("numeroDocumentoPeluquero") String numeroDocumentoPeluquero,
                                                     @Param("noCita") Long noCita,
                                                     @Param("fechaCita") LocalDate fechaCita,
                                                     @Param("estadoActivo") long estadoActivo,
                                                     @Param("inicio") LocalTime inicio,
                                                     @Param("fin") LocalTime fin);
}