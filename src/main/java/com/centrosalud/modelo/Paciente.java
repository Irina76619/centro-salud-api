package com.centrosalud.modelo;

import jakarta.persistence.*;

/** POO - HERENCIA: Paciente hereda dni, nombre, apellidos y edad de Persona. */
@Entity
@Table(name = "paciente")
public class Paciente extends Persona {

    @Column(length = 15)
    private String telefono;

    public Paciente() { }

    public Paciente(String dni, String nombre, String apellidos, Integer edad, String telefono) {
        super(dni, nombre, apellidos, edad);
        this.telefono = telefono;
    }

    @Override
    public String descripcion() {
        return "Paciente " + getNombreCompleto() + " (DNI " + getDni() + ")";
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
