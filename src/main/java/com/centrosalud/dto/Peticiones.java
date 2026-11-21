package com.centrosalud.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * DTOs de ENTRADA (lo que envia el frontend). Son "records": clases inmutables y compactas.
 * Las anotaciones (@NotBlank, @Min...) se validan automaticamente por el @Valid del controlador.
 * Se usan DTOs (y no las entidades) para no exponer claves hash ni depender de la estructura de la BD.
 */
public final class Peticiones {
    private Peticiones() { }

    /** tipo = "ADMIN" o "MEDICO"; usuario = nombre de usuario o codigo de medico. */
    public record LoginReq(
            @NotBlank(message = "El tipo es obligatorio (ADMIN o MEDICO)") String tipo,
            @NotBlank(message = "El usuario es obligatorio") String usuario,
            @NotBlank(message = "La clave es obligatoria") String clave) { }

    public record EspecialidadReq(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 60, message = "Maximo 60 caracteres") String nombre) { }

    public record PacienteReq(
            @NotBlank(message = "El DNI es obligatorio")
            @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos") String dni,
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 50) String nombre,
            @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String apellidos,
            @NotNull(message = "La edad es obligatoria")
            @Min(value = 0, message = "Edad minima 0") @Max(value = 120, message = "Edad maxima 120") Integer edad,
            @Pattern(regexp = "^(\\d{6,15})?$", message = "El telefono debe tener entre 6 y 15 digitos") String telefono) { }

    /** En actualizacion, "clave" es opcional: si no viene, se conserva la actual. */
    public record MedicoReq(
            @NotBlank(message = "El codigo es obligatorio") @Size(max = 10) String codigoMedico,
            @Size(min = 4, max = 50, message = "La clave debe tener entre 4 y 50 caracteres") String clave,
            @NotBlank(message = "El DNI es obligatorio")
            @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos") String dni,
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 50) String nombre,
            @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String apellidos,
            @NotNull(message = "La edad es obligatoria")
            @Min(value = 18, message = "Edad minima 18") @Max(value = 100, message = "Edad maxima 100") Integer edad,
            @NotNull(message = "La especialidad es obligatoria") Long especialidadId) { }

    public record HorarioReq(
            @NotBlank(message = "El dia es obligatorio") String diaSemana,
            @NotNull(message = "La hora de inicio es obligatoria") LocalTime horaInicio,
            @NotNull(message = "La hora de fin es obligatoria") LocalTime horaFin,
            @NotNull(message = "El medico es obligatorio") Long medicoId) { }

    public record CitaReq(
            @NotNull(message = "La fecha y hora son obligatorias") LocalDateTime fechaHora,
            @NotBlank(message = "El motivo es obligatorio") @Size(max = 150) String motivo,
            @NotNull(message = "El paciente es obligatorio") Long pacienteId,
            @NotNull(message = "El medico es obligatorio") Long medicoId) { }

    public record RecetaReq(
            @NotBlank(message = "El medicamento es obligatorio") @Size(max = 100) String medicamento,
            @NotBlank(message = "Las indicaciones son obligatorias") @Size(max = 200) String indicaciones) { }

    public record HistorialReq(
            @NotBlank(message = "El diagnostico es obligatorio") @Size(max = 200) String diagnostico,
            @NotBlank(message = "El tratamiento es obligatorio") @Size(max = 200) String tratamiento) { }

    /** Lo que envia el medico al cerrar una cita: diagnostico + tratamiento + recetas (opcionales). */
    public record AtenderReq(
            @NotBlank(message = "El diagnostico es obligatorio") @Size(max = 200) String diagnostico,
            @NotBlank(message = "El tratamiento es obligatorio") @Size(max = 200) String tratamiento,
            @Valid List<RecetaReq> recetas) { }
}
