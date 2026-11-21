package com.centrosalud.modelo;

import jakarta.persistence.*;

/** POO - HERENCIA y POLIMORFISMO: Medico hereda de Persona y redefine descripcion(). */
@Entity
@Table(name = "medico")
public class Medico extends Persona {

    @Column(name = "codigo_medico", nullable = false, unique = true, length = 10)
    private String codigoMedico;

    /** Siempre guarda el HASH BCrypt, nunca la clave en texto plano. */
    @Column(nullable = false, length = 100)
    private String clave;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_especialidad", nullable = false)
    private Especialidad especialidad;

    public Medico() { }

    public Medico(String dni, String nombre, String apellidos, Integer edad,
                  String codigoMedico, String clave, Especialidad especialidad) {
        super(dni, nombre, apellidos, edad);
        this.codigoMedico = codigoMedico;
        this.clave = clave;
        this.especialidad = especialidad;
    }

    @Override
    public String descripcion() {
        return "Dr(a). " + getNombreCompleto() + " - " + especialidad.getNombre() + " (" + codigoMedico + ")";
    }

    public String getCodigoMedico() { return codigoMedico; }
    public void setCodigoMedico(String codigoMedico) { this.codigoMedico = codigoMedico; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }
}
