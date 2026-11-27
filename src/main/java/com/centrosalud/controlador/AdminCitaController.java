package com.centrosalud.controlador;

import com.centrosalud.dto.Peticiones.CitaReq;
import com.centrosalud.dto.Respuestas.CitaRes;
import com.centrosalud.modelo.EstadoCita;
import com.centrosalud.servicio.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/citas")
public class AdminCitaController {

    private final CitaService service;

    public AdminCitaController(CitaService service) {
        this.service = service;
    }

    /** GET /api/admin/citas?medicoId=&pacienteId=&estado=PENDIENTE  (todos los filtros son opcionales) */
    @GetMapping
    public List<CitaRes> listar(@RequestParam(required = false) Long medicoId,
                                @RequestParam(required = false) Long pacienteId,
                                @RequestParam(required = false) EstadoCita estado) {
        return service.listar(medicoId, pacienteId, estado);
    }

    @GetMapping("/{id}")
    public CitaRes obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaRes crear(@Valid @RequestBody CitaReq req) { return service.crear(req); }

    /** Editar o reprogramar una cita pendiente. */
    @PutMapping("/{id}")
    public CitaRes actualizar(@PathVariable Long id, @Valid @RequestBody CitaReq req) {
        return service.actualizar(id, req);
    }

    @PatchMapping("/{id}/cancelar")
    public CitaRes cancelar(@PathVariable Long id) { return service.cancelar(id); }

    /** Marca una cita pendiente como "no asistio". */
    @PatchMapping("/{id}/no-asistio")
    public CitaRes noAsistio(@PathVariable Long id) { return service.marcarNoAsistio(id); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
