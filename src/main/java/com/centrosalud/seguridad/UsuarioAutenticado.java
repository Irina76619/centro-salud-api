package com.centrosalud.seguridad;

/**
 * Quien esta haciendo la peticion, sacado del token JWT.
 * rol = "ADMIN" o "MEDICO". Si rol es MEDICO, "id" es el id del medico: asi el servidor
 * sabe que citas/pacientes le corresponden sin confiar en lo que mande el frontend.
 */
public record UsuarioAutenticado(Long id, String usuario, String rol) {
}
