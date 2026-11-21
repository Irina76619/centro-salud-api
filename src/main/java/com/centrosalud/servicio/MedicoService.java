package com.centrosalud.servicio;

import com.centrosalud.dto.Peticiones.MedicoReq;
import com.centrosalud.dto.Respuestas.MedicoRes;
import com.centrosalud.excepcion.ApiException;
import com.centrosalud.modelo.Especialidad;
import com.centrosalud.modelo.Medico;
import com.centrosalud.repositorio.CitaRepository;
import com.centrosalud.repositorio.EspecialidadRepository;
import com.centrosalud.repositorio.MedicoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MedicoService {

    private final MedicoRepository medicos;
    private final EspecialidadRepository especialidades;
    private final CitaRepository citas;
    private final PasswordEncoder encoder;

    public MedicoService(MedicoRepository medicos, EspecialidadRepository especialidades,
                         CitaRepository citas, PasswordEncoder encoder) {
        this.medicos = medicos;
        this.especialidades = especialidades;
        this.citas = citas;
        this.encoder = encoder;
    }

    public List<MedicoRes> listar() {
        return medicos.findAll(Sort.by("id")).stream().map(Mapeador::medico).toList();
    }

    public MedicoRes obtener(Long id) {
        return Mapeador.medico(buscar(id));
    }

    @Transactional
    public MedicoRes crear(MedicoReq req) {
        if (req.clave() == null || req.clave().isBlank()) {
            throw ApiException.reglaNegocio("La clave es obligatoria al registrar un medico");
        }
        String codigo = req.codigoMedico().trim();
        if (medicos.existsByCodigoMedico(codigo)) {
            throw ApiException.conflicto("Ya existe un medico con el codigo " + codigo);
        }
        if (medicos.existsByDni(req.dni())) {
            throw ApiException.conflicto("Ya existe un medico con el DNI " + req.dni());
        }
        Especialidad esp = buscarEspecialidad(req.especialidadId());
        Medico m = new Medico(req.dni(), req.nombre().trim(), req.apellidos().trim(), req.edad(),
                codigo, encoder.encode(req.clave()), esp);
        return Mapeador.medico(medicos.save(m));
    }

    @Transactional
    public MedicoRes actualizar(Long id, MedicoReq req) {
        Medico m = buscar(id);
        String codigo = req.codigoMedico().trim();
        if (medicos.existsByCodigoMedicoAndIdNot(codigo, id)) {
            throw ApiException.conflicto("Ya existe otro medico con el codigo " + codigo);
        }
        if (medicos.existsByDniAndIdNot(req.dni(), id)) {
            throw ApiException.conflicto("Ya existe otro medico con el DNI " + req.dni());
        }
        m.setCodigoMedico(codigo);
        m.setDni(req.dni());
        m.setNombre(req.nombre().trim());
        m.setApellidos(req.apellidos().trim());
        m.setEdad(req.edad());
        m.setEspecialidad(buscarEspecialidad(req.especialidadId()));
        if (req.clave() != null && !req.clave().isBlank()) {   // clave opcional al editar
            m.setClave(encoder.encode(req.clave()));
        }
        return Mapeador.medico(m);
    }

    @Transactional
    public void eliminar(Long id) {
        Medico m = buscar(id);
        if (citas.existsByMedicoId(id)) {
            throw ApiException.conflicto("No se puede eliminar: el medico tiene citas registradas");
        }
        medicos.delete(m);   // sus horarios se borran solos (ON DELETE CASCADE en la BD)
    }

    private Medico buscar(Long id) {
        return medicos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Medico", id));
    }

    private Especialidad buscarEspecialidad(Long id) {
        return especialidades.findById(id).orElseThrow(() -> ApiException.noEncontrado("Especialidad", id));
    }
}
