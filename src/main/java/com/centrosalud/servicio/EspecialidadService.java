package com.centrosalud.servicio;

import com.centrosalud.dto.Peticiones.EspecialidadReq;
import com.centrosalud.dto.Respuestas.EspecialidadRes;
import com.centrosalud.excepcion.ApiException;
import com.centrosalud.modelo.Especialidad;
import com.centrosalud.repositorio.EspecialidadRepository;
import com.centrosalud.repositorio.MedicoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Logica de negocio de Especialidades (CRUD). @Transactional = todo el metodo es una sola transaccion. */
@Service
@Transactional(readOnly = true)
public class EspecialidadService {

    private final EspecialidadRepository especialidades;
    private final MedicoRepository medicos;

    public EspecialidadService(EspecialidadRepository especialidades, MedicoRepository medicos) {
        this.especialidades = especialidades;
        this.medicos = medicos;
    }

    public List<EspecialidadRes> listar() {
        return especialidades.findAll(Sort.by("nombre")).stream().map(Mapeador::especialidad).toList();
    }

    public EspecialidadRes obtener(Long id) {
        return Mapeador.especialidad(buscar(id));
    }

    @Transactional
    public EspecialidadRes crear(EspecialidadReq req) {
        String nombre = req.nombre().trim();
        if (especialidades.existsByNombreIgnoreCase(nombre)) {
            throw ApiException.conflicto("Ya existe la especialidad '" + nombre + "'");
        }
        return Mapeador.especialidad(especialidades.save(new Especialidad(nombre)));
    }

    @Transactional
    public EspecialidadRes actualizar(Long id, EspecialidadReq req) {
        Especialidad e = buscar(id);
        String nombre = req.nombre().trim();
        if (especialidades.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw ApiException.conflicto("Ya existe la especialidad '" + nombre + "'");
        }
        e.setNombre(nombre);
        return Mapeador.especialidad(e);
    }

    @Transactional
    public void eliminar(Long id) {
        Especialidad e = buscar(id);
        if (medicos.existsByEspecialidadId(id)) {
            throw ApiException.conflicto("No se puede eliminar: hay medicos con esta especialidad");
        }
        especialidades.delete(e);
    }

    private Especialidad buscar(Long id) {
        return especialidades.findById(id).orElseThrow(() -> ApiException.noEncontrado("Especialidad", id));
    }
}
