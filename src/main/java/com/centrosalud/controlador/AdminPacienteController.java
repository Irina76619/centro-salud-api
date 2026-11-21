package com.centrosalud.controlador;

import com.centrosalud.dto.Peticiones.PacienteReq;
import com.centrosalud.dto.Respuestas.CitaRes;
import com.centrosalud.dto.Respuestas.PacienteRes;
import com.centrosalud.servicio.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pacientes")
public class AdminPacienteController {

    private final PacienteService service;

    public AdminPacienteController(PacienteService service) {
        this.service = service;
    }

    /** GET /api/admin/pacientes            -> todos
     *  GET /api/admin/pacientes?dni=...    -> busca por DNI
     *  GET /api/admin/pacientes?nombre=..&apellidos=.. -> busca por nombre y apellidos */
    @GetMapping
    public List<PacienteRes> listar(@RequestParam(required = false) String dni,
                                    @RequestParam(required = false) String nombre,
                                    @RequestParam(required = false) String apellidos) {
        if (dni != null && !dni.isBlank()) {
            return service.buscar(dni);
        }
        if (nombre != null && !nombre.isBlank() && apellidos != null && !apellidos.isBlank()) {
            return service.buscar(nombre, apellidos);
        }
        return service.listar();
    }

    @GetMapping("/{id}")
    public PacienteRes obtener(@PathVariable Long id) { return service.obtener(id); }

    /** Citas del paciente con medico, especialidad, fecha y hora. */
    @GetMapping("/{id}/citas")
    public List<CitaRes> citas(@PathVariable Long id) { return service.citasDe(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteRes crear(@Valid @RequestBody PacienteReq req) { return service.crear(req); }

    @PutMapping("/{id}")
    public PacienteRes actualizar(@PathVariable Long id, @Valid @RequestBody PacienteReq req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
