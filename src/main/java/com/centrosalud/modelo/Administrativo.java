package com.centrosalud.modelo;

import jakarta.persistence.*;

/** Usuario administrativo del centro. Hereda de Persona igual que Medico y Paciente. */
@Entity
@Table(name = "administrativo")
public class Administrativo extends Persona {

    @Column(nullable = false, unique = true, length = 30)
    private String usuario;

    /** Hash BCrypt. */
    @Column(nullable = false, length = 100)
    private String clave;

    public Administrativo() { }

    public Administrativo(String dni, String nombre, String apellidos, Integer edad, String usuario, String clave) {
        super(dni, nombre, apellidos, edad);
        this.usuario = usuario;
        this.clave = clave;
    }

    @Override
    public String descripcion() {
        return "Administrativo " + getNombreCompleto();
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
