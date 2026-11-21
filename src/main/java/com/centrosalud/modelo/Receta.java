package com.centrosalud.modelo;

import jakarta.persistence.*;

import java.time.LocalDate;

/** Medicamento indicado durante una cita. */
@Entity
@Table(name = "receta")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String medicamento;

    @Column(nullable = false, length = 200)
    private String indicaciones;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cita", nullable = false)
    private Cita cita;

    public Receta() { }

    public Receta(String medicamento, String indicaciones, LocalDate fecha, Cita cita) {
        this.medicamento = medicamento;
        this.indicaciones = indicaciones;
        this.fecha = fecha;
        this.cita = cita;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMedicamento() { return medicamento; }
    public void setMedicamento(String medicamento) { this.medicamento = medicamento; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }
}
