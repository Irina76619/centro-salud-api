package com.centrosalud.modelo;

import jakarta.persistence.*;

import java.time.LocalDate;

/** Registro del historial clinico de un paciente (diagnostico + tratamiento). La cita es opcional. */
@Entity
@Table(name = "historial_clinico")
public class HistorialClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String diagnostico;

    @Column(nullable = false, length = 200)
    private String tratamiento;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    /** Puede ser null: si se borra la cita, el historial se conserva (ON DELETE SET NULL). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita")
    private Cita cita;

    public HistorialClinico() { }

    public HistorialClinico(String diagnostico, String tratamiento, LocalDate fecha, Paciente paciente, Cita cita) {
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.fecha = fecha;
        this.paciente = paciente;
        this.cita = cita;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }
}
