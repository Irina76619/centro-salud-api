package com.centrosalud.servicio;

import com.centrosalud.dto.Peticiones.PacienteReq;
import com.centrosalud.dto.Respuestas.CitaRes;
import com.centrosalud.dto.Respuestas.PacienteRes;
import com.centrosalud.excepcion.ApiException;
import com.centrosalud.modelo.Paciente;
import com.centrosalud.repositorio.CitaRepository;
import com.centrosalud.repositorio.PacienteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PacienteService {

    private final PacienteRepository pacientes;
    private final CitaRepository citas;

    public PacienteService(PacienteRepository pacientes, CitaRepository citas) {
        this.pacientes = pacientes;
        this.citas = citas;
    }

    public List<PacienteRes> listar() {
        return pacientes.findAll(Sort.by("id")).stream().map(Mapeador::paciente).toList();
    }

    /** POO - SOBRECARGA DE METODOS (1/2): buscar por DNI. */
    public List<PacienteRes> buscar(String dni) {
        return pacientes.findByDni(dni.trim()).stream().map(Mapeador::paciente).toList();
    }

    /** POO - SOBRECARGA DE METODOS (2/2): mismo nombre, otros parametros -> buscar por nombre y apellidos. */
    public List<PacienteRes> buscar(String nombre, String apellidos) {
        return pacientes.findByNombreIgnoreCaseAndApellidosIgnoreCase(nombre.trim(), apellidos.trim())
                .stream().map(Mapeador::paciente).toList();
    }

    public PacienteRes obtener(Long id) {
        return Mapeador.paciente(buscarPorId(id));
    }

    @Transactional
    public PacienteRes crear(PacienteReq req) {
        if (pacientes.existsByDni(req.dni())) {
            throw ApiException.conflicto("Ya existe un paciente con el DNI " + req.dni());
        }
        Paciente p = new Paciente(req.dni(), req.nombre().trim(), req.apellidos().trim(), req.edad(), vacioANull(req.telefono()));
        return Mapeador.paciente(pacientes.save(p));
    }

    @Transactional
    public PacienteRes actualizar(Long id, PacienteReq req) {
        Paciente p = buscarPorId(id);
        if (pacientes.existsByDniAndIdNot(req.dni(), id)) {
            throw ApiException.conflicto("Ya existe otro paciente con el DNI " + req.dni());
        }
        p.setDni(req.dni());
        p.setNombre(req.nombre().trim());
        p.setApellidos(req.apellidos().trim());
        p.setEdad(req.edad());
        p.setTelefono(vacioANull(req.telefono()));
        return Mapeador.paciente(p);
    }

    @Transactional
    public void eliminar(Long id) {
        Paciente p = buscarPorId(id);
        if (citas.existsByPacienteId(id)) {
            throw ApiException.conflicto("No se puede eliminar: el paciente tiene citas registradas");
        }
        pacientes.delete(p);
    }

    /** "Mostrar la cita del paciente con medico, especialidad, fecha y hora". */
    public List<CitaRes> citasDe(Long pacienteId) {
        buscarPorId(pacienteId);
        return citas.findByPacienteIdOrderByFechaHoraAsc(pacienteId).stream().map(Mapeador::cita).toList();
    }

    private Paciente buscarPorId(Long id) {
        return pacientes.findById(id).orElseThrow(() -> ApiException.noEncontrado("Paciente", id));
    }

    private static String vacioANull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
