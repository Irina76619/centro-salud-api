package com.centrosalud.dto;

import com.centrosalud.modelo.EstadoCita;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** DTOs de SALIDA (lo que recibe el frontend). Nunca incluyen claves. */
public final class Respuestas {
    private Respuestas() { }

    public record LoginRes(String token, String rol, Long id, String nombre, String detalle) { }

    public record EspecialidadRes(Long id, String nombre) { }

    public record PacienteRes(Long id, String dni, String nombre, String apellidos, Integer edad, String telefono) { }

    public record MedicoRes(Long id, String codigoMedico, String dni, String nombre, String apellidos,
                            Integer edad, Long especialidadId, String especialidad) { }

    public record HorarioRes(Long id, String diaSemana, LocalTime horaInicio, LocalTime horaFin,
                             Long medicoId, String medico) { }

    public record CitaRes(Long id, LocalDateTime fechaHora, String motivo, EstadoCita estado,
                          Long pacienteId, String paciente, String pacienteDni,
                          Long medicoId, String medico, String especialidad) { }

    public record RecetaRes(Long id, String medicamento, String indicaciones, LocalDate fecha, Long citaId) { }

    /** Registro del historial con su medico y las recetas de la cita asociada. "medico" y "citaId" pueden ser null. */
    public record HistorialDetalleRes(Long id, LocalDate fecha, String diagnostico, String tratamiento,
                                      Long pacienteId, String paciente, Long citaId, String medico,
                                      List<RecetaRes> recetas) { }

    /** Un grupo de citas (por medico o por especialidad) con su total. */
    public record GrupoCitasRes(String grupo, long total, List<CitaRes> citas) { }

    /** Reporte simple de citas (por dia o por mes). */
    public record ReporteCitasRes(String titulo, long total, List<CitaRes> citas) { }

    /** Par etiqueta/total, pensado para graficos. */
    public record ConteoRes(String etiqueta, long total) { }

    public record DashboardRes(long pacientes, long medicos, long especialidades,
                               long citasHoy, long pendientes, long atendidas, long canceladas) { }
}
