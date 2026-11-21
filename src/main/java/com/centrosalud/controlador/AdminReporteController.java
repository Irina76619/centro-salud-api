package com.centrosalud.controlador;

import com.centrosalud.dto.Respuestas.*;
import com.centrosalud.servicio.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminReporteController {

    private final ReporteService service;

    public AdminReporteController(ReporteService service) {
        this.service = service;
    }

    /** Tarjetas del panel principal. */
    @GetMapping("/dashboard")
    public DashboardRes dashboard() { return service.dashboard(); }

    @GetMapping("/reportes/citas-por-medico")
    public List<GrupoCitasRes> citasPorMedico() { return service.citasPorMedico(); }

    @GetMapping("/reportes/citas-por-especialidad")
    public List<GrupoCitasRes> citasPorEspecialidad() { return service.citasPorEspecialidad(); }

    /** GET /api/admin/reportes/citas-por-dia?fecha=2026-10-28 */
    @GetMapping("/reportes/citas-por-dia")
    public ReporteCitasRes citasPorDia(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.citasPorDia(fecha);
    }

    /** GET /api/admin/reportes/citas-por-mes?mes=10&anio=2026 */
    @GetMapping("/reportes/citas-por-mes")
    public ReporteCitasRes citasPorMes(@RequestParam int mes, @RequestParam int anio) {
        return service.citasPorMes(mes, anio);
    }

    /** Total de citas por mes de un anio (para grafico de barras). */
    @GetMapping("/reportes/resumen-anual")
    public List<ConteoRes> resumenAnual(@RequestParam int anio) { return service.resumenAnual(anio); }

    @GetMapping("/reportes/historial/{pacienteId}")
    public List<HistorialDetalleRes> historial(@PathVariable Long pacienteId) {
        return service.historialPorPaciente(pacienteId);
    }

    @GetMapping("/reportes/recetas-tratamientos")
    public List<HistorialDetalleRes> recetasYTratamientos() { return service.recetasYTratamientos(); }
}
