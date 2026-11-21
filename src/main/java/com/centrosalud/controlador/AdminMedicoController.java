package com.centrosalud.controlador;

import com.centrosalud.dto.Peticiones.MedicoReq;
import com.centrosalud.dto.Respuestas.MedicoRes;
import com.centrosalud.servicio.MedicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/medicos")
public class AdminMedicoController {

    private final MedicoService service;

    public AdminMedicoController(MedicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<MedicoRes> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public MedicoRes obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicoRes crear(@Valid @RequestBody MedicoReq req) { return service.crear(req); }

    @PutMapping("/{id}")
    public MedicoRes actualizar(@PathVariable Long id, @Valid @RequestBody MedicoReq req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
