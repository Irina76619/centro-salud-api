package com.centrosalud.servicio;

import com.centrosalud.dto.Respuestas.*;
import com.centrosalud.excepcion.ApiException;
import com.centrosalud.modelo.Cita;
import com.centrosalud.modelo.EstadoCita;
import com.centrosalud.repositorio.*;
import com.centrosalud.util.Tiempo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reportes obligatorios. Antes imprimian en consola; ahora devuelven datos (JSON)
 * para que el frontend los muestre en tablas y graficos.
 */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private static final String[] MESES = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

    private final CitaRepository citas;
    private final MedicoRepository medicos;
    private final EspecialidadRepository especialidades;
    private final PacienteRepository pacientes;
    private final HistorialService historialService;

    public ReporteService(CitaRepository citas, MedicoRepository medicos, EspecialidadRepository especialidades,
                          PacienteRepository pacientes, HistorialService historialService) {
        this.citas = citas;
        this.medicos = medicos;
        this.especialidades = especialidades;
        this.pacientes = pacientes;
        this.historialService = historialService;
    }

    /** Reporte 1: citas por medico (incluye medicos sin citas, con total 0). */
    public List<GrupoCitasRes> citasPorMedico() {
        Map<Long, List<Cita>> porMedico = citas.findAllByOrderByFechaHoraAsc().stream()
                .collect(Collectors.groupingBy(c -> c.getMedico().getId()));
        return medicos.findAll(Sort.by("id")).stream().map(m -> {
            List<CitaRes> lista = porMedico.getOrDefault(m.getId(), List.of()).stream().map(Mapeador::cita).toList();
            return new GrupoCitasRes(m.getNombreCompleto() + " (" + m.getCodigoMedico() + ")", lista.size(), lista);
        }).toList();
    }

    /** Reporte 2: citas por especialidad. */
    public List<GrupoCitasRes> citasPorEspecialidad() {
        Map<Long, List<Cita>> porEsp = citas.findAllByOrderByFechaHoraAsc().stream()
                .collect(Collectors.groupingBy(c -> c.getMedico().getEspecialidad().getId()));
        return especialidades.findAll(Sort.by("nombre")).stream().map(e -> {
            List<CitaRes> lista = porEsp.getOrDefault(e.getId(), List.of()).stream().map(Mapeador::cita).toList();
            return new GrupoCitasRes(e.getNombre(), lista.size(), lista);
        }).toList();
    }

    /** Reporte 3a: citas de un dia. */
    public ReporteCitasRes citasPorDia(LocalDate fecha) {
        List<CitaRes> lista = entre(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
        return new ReporteCitasRes("Citas del " + fecha, lista.size(), lista);
    }

    /** Reporte 3b: citas de un mes. */
    public ReporteCitasRes citasPorMes(int mes, int anio) {
        YearMonth ym = periodo(mes, anio);
        List<CitaRes> lista = entre(ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay());
        return new ReporteCitasRes("Citas de " + MESES[mes - 1] + " " + anio, lista.size(), lista);
    }

    /** Extra para graficos: total de citas de cada mes de un anio. */
    public List<ConteoRes> resumenAnual(int anio) {
        List<ConteoRes> resultado = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            YearMonth ym = periodo(mes, anio);
            long total = citas.countByFechaHoraGreaterThanEqualAndFechaHoraLessThan(
                    ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay());
            resultado.add(new ConteoRes(MESES[mes - 1], total));
        }
        return resultado;
    }

    /** Reporte 4: historial clinico completo de un paciente. */
    public List<HistorialDetalleRes> historialPorPaciente(Long pacienteId) {
        if (!pacientes.existsById(pacienteId)) {
            throw ApiException.noEncontrado("Paciente", pacienteId);
        }
        return historialService.porPaciente(pacienteId);
    }

    /** Reporte 5: recetas y tratamientos asignados (todos los pacientes). */
    public List<HistorialDetalleRes> recetasYTratamientos() {
        return historialService.todos();
    }

    /** Tarjetas del panel principal del administrador. */
    public DashboardRes dashboard() {
        LocalDate hoy = Tiempo.hoy();
        return new DashboardRes(
                pacientes.count(),
                medicos.count(),
                especialidades.count(),
                citas.countByFechaHoraGreaterThanEqualAndFechaHoraLessThan(hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay()),
                citas.countByEstado(EstadoCita.PENDIENTE),
                citas.countByEstado(EstadoCita.ATENDIDA),
                citas.countByEstado(EstadoCita.CANCELADA));
    }

    private List<CitaRes> entre(LocalDateTime desde, LocalDateTime hasta) {
        return citas.findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAsc(desde, hasta)
                .stream().map(Mapeador::cita).toList();
    }

    private YearMonth periodo(int mes, int anio) {
        if (mes < 1 || mes > 12) throw ApiException.reglaNegocio("El mes debe estar entre 1 y 12");
        if (anio < 2000 || anio > 2100) throw ApiException.reglaNegocio("El anio debe estar entre 2000 y 2100");
        return YearMonth.of(anio, mes);
    }
}
