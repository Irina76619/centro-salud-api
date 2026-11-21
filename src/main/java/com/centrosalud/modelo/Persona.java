package com.centrosalud.modelo;

import jakarta.persistence.*;

/**
 * POO - CLASE ABSTRACTA: no se puede instanciar; agrupa lo comun de Paciente, Medico y Administrativo.
 * POO - ENCAPSULAMIENTO: atributos privados con getters/setters.
 * JPA: @MappedSuperclass hace que las columnas se hereden en las tablas de cada hija (no hay tabla "persona").
 */
@MappedSuperclass
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String dni;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(nullable = false)
    private Integer edad;

    // POO - SOBRECARGA DE CONSTRUCTORES: uno vacio (lo exige JPA) y otro con todos los datos.
    protected Persona() { }

    protected Persona(String dni, String nombre, String apellidos, Integer edad) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.edad = edad;
    }

    /** POO - POLIMORFISMO: cada subclase describe a la persona a su manera. */
    public abstract String descripcion();

    public String getNombreCompleto() { return nombre + " " + apellidos; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
}
