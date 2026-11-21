package com.centrosalud.controlador;

import com.centrosalud.dto.Peticiones.EspecialidadReq;
import com.centrosalud.dto.Respuestas.EspecialidadRes;
import com.centrosalud.servicio.EspecialidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/especialidades")
public class AdminEspecialidadController {

    private final EspecialidadService service;

    public AdminEspecialidadController(EspecialidadService service) {
        this.service = service;
    }

    @GetMapping
    public List<EspecialidadRes> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public EspecialidadRes obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EspecialidadRes crear(@Valid @RequestBody EspecialidadReq req) { return service.crear(req); }

    @PutMapping("/{id}")
    public EspecialidadRes actualizar(@PathVariable Long id, @Valid @RequestBody EspecialidadReq req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
