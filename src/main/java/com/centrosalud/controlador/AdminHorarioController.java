package com.centrosalud.controlador;

import com.centrosalud.dto.Peticiones.HorarioReq;
import com.centrosalud.dto.Respuestas.HorarioRes;
import com.centrosalud.servicio.HorarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/horarios")
public class AdminHorarioController {

    private final HorarioService service;

    public AdminHorarioController(HorarioService service) {
        this.service = service;
    }

    /** GET /api/admin/horarios?medicoId=1  (medicoId es opcional) */
    @GetMapping
    public List<HorarioRes> listar(@RequestParam(required = false) Long medicoId) {
        return service.listar(medicoId);
    }

    @GetMapping("/{id}")
    public HorarioRes obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HorarioRes crear(@Valid @RequestBody HorarioReq req) { return service.crear(req); }

    @PutMapping("/{id}")
    public HorarioRes actualizar(@PathVariable Long id, @Valid @RequestBody HorarioReq req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
