# Centro de Salud — API REST (Spring Boot)

Backend del sistema de gestión de un centro de salud: pacientes, médicos, especialidades, horarios, citas,
historial clínico, recetas y reportes. Evolución de la versión de consola en Java puro a una API REST
con autenticación JWT por roles (**Administrativo** y **Médico**).

**Stack:** Java 21 · Spring Boot 3.4 · Spring Data JPA (Hibernate) · Spring Security + JWT · MySQL 8 · Swagger UI

## Ejecutar en local (IntelliJ IDEA)

1. **Base de datos:** en MySQL Workbench ejecuta `database/centro_salud.sql` completo (borra y recrea `centro_salud`).
2. **Abrir:** *File → Open →* carpeta del proyecto (IntelliJ detecta el `pom.xml` y descarga las dependencias).
3. **Credenciales de BD:** por defecto usa `root` / `admin` en `localhost:3306`. Si las tuyas son otras, crea una
   *Run Configuration* con variables de entorno (ver tabla abajo).
4. **Ejecutar** la clase `CentroSaludApplication`. Arranca en `http://localhost:8080`.
5. **Probar:** abre `http://localhost:8080/swagger-ui.html`, haz login y pega el token en **Authorize**.

### Usuarios de prueba

| Rol | Tipo (`tipo`) | Usuario | Clave |
|---|---|---|---|
| Administrativo | `ADMIN` | `admin` | `admin123` |
| Médico | `MEDICO` | `MED001` / `MED002` / `MED003` | `1234` |

### Variables de entorno

| Variable | Por defecto | Para qué |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/centro_salud?useSSL=false&allowPublicKeyRetrieval=true` | URL JDBC (en la nube la da el proveedor) |
| `DB_USER` / `DB_PASSWORD` | `root` / `admin` | Credenciales de BD |
| `DB_POOL_SIZE` | `3` | Conexiones simultáneas (los planes gratis las limitan) |
| `JWT_SECRET` | clave de desarrollo | **Cámbiala en producción** (mínimo 32 caracteres) |
| `JWT_EXPIRATION_MINUTES` | `480` | Duración del token |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Dominios del frontend, separados por coma. Admite `https://*.vercel.app` |
| `PORT` | `8080` | Puerto |

## Arquitectura

```
Controlador  →  Servicio  →  Repositorio  →  MySQL
(HTTP/JSON)     (reglas)     (JPA, sin SQL)
```

| Paquete | Qué contiene | Equivale en la versión consola |
|---|---|---|
| `modelo` | Entidades JPA (`Persona` abstracta → `Paciente`, `Medico`, `Administrativo`, …) | `modelo` |
| `repositorio` | Interfaces JPA | `datos` (DAO) |
| `servicio` | Lógica de negocio y validaciones | `servicio` + la lógica que estaba en `MenuConsola` |
| `controlador` | Endpoints REST | `vista` (menús) |
| `dto` | Objetos de entrada/salida (nunca se exponen entidades ni claves) | — |
| `seguridad` / `config` | JWT, roles, CORS, bloqueo de login | `ServicioLogin` |
| `excepcion` | Errores JSON uniformes | `System.out.println("error...")` |

**Conceptos POO presentes:** clase abstracta y herencia (`Persona`), polimorfismo (`descripcion()` en cada
subclase), encapsulamiento (atributos privados), sobrecarga de métodos (`PacienteService.buscar(dni)` /
`buscar(nombre, apellidos)`) y de constructores (vacío + completo en cada entidad).

## Reglas de negocio implementadas

- **Login:** 3 intentos fallidos → bloqueo de 5 minutos (versión web de "se cierra el programa").
- **Claves:** guardadas con BCrypt (no en texto plano).
- **Médico solo ve lo suyo:** el id del médico sale del token, no de la petición. Ve únicamente sus citas, sus pacientes y edita solo registros de sus citas.
- **Citas:** fecha futura · el médico debe atender ese día y hora según su `Horario` · sin choques de horario · solo se editan/cancelan citas `PENDIENTE`.
- **Horarios:** no se cruzan bloques del mismo médico en un día.
- **Borrados protegidos:** no se elimina un paciente/médico con citas ni una especialidad en uso (responde 409 con mensaje claro).

## Endpoints

Todos devuelven JSON. Excepto `/api/auth/login`, requieren `Authorization: Bearer <token>`.
Errores: `{ timestamp, status, mensaje, errores? }` (`errores` trae el detalle por campo en validaciones).

### Auth
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | `{tipo: "ADMIN"\|"MEDICO", usuario, clave}` → `{token, rol, id, nombre, detalle}` |

### Administrativo — `/api/admin/**` (rol ADMIN)
| Recurso | Rutas |
|---|---|
| Pacientes | `GET /pacientes` (`?dni=` o `?nombre=&apellidos=`) · `GET /pacientes/{id}` · `GET /pacientes/{id}/citas` · `POST` · `PUT /{id}` · `DELETE /{id}` |
| Médicos | `GET /medicos` · `GET /{id}` · `POST` · `PUT /{id}` (clave opcional) · `DELETE /{id}` |
| Especialidades | `GET /especialidades` · `GET /{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` |
| Horarios | `GET /horarios` (`?medicoId=`) · `GET /{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` |
| Citas | `GET /citas` (`?medicoId=&pacienteId=&estado=`) · `GET /{id}` · `POST` · `PUT /{id}` · `PATCH /{id}/cancelar` · `DELETE /{id}` |
| Dashboard | `GET /dashboard` |
| Reportes | `GET /reportes/citas-por-medico` · `/citas-por-especialidad` · `/citas-por-dia?fecha=2026-10-28` · `/citas-por-mes?mes=10&anio=2026` · `/resumen-anual?anio=2026` · `/historial/{pacienteId}` · `/recetas-tratamientos` |

### Médico — `/api/medico/**` (rol MEDICO)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/perfil` | Mis datos |
| GET | `/citas` (`?estado=`) · `/citas/hoy` | Mis citas |
| GET | `/horario` | Mi horario |
| GET | `/pacientes` | Pacientes que he atendido o tengo agendados |
| GET | `/pacientes/{id}/historial` | Historial completo del paciente (con recetas) |
| POST | `/citas/{id}/atender` | `{diagnostico, tratamiento, recetas:[{medicamento, indicaciones}]}` → cierra la cita |
| PUT | `/historial/{id}` | Editar diagnóstico/tratamiento de mi registro |
| GET/POST | `/citas/{id}/recetas` | Ver / agregar recetas |
| PUT/DELETE | `/recetas/{id}` | Editar / eliminar receta |

### Formatos de ejemplo
```jsonc
// Cita (POST /api/admin/citas)
{ "fechaHora": "2026-12-07T09:00:00", "motivo": "Control", "pacienteId": 1, "medicoId": 1 }
// Horario
{ "diaSemana": "Lunes", "horaInicio": "08:00", "horaFin": "13:00", "medicoId": 1 }
```

## Despliegue gratuito (al final del desarrollo)

1. **BD:** crea un MySQL gratuito (p. ej. el plan DEV de Clever Cloud o TiDB Cloud Starter) y ejecuta `database/centro_salud.sql`.
   Ojo: el plan DEV es solo para pruebas y limita las conexiones.
2. **Backend:** en Render, *New → Web Service* desde tu repo de GitHub con **Docker** (usa el `Dockerfile`).
   Define `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` y `CORS_ORIGINS` (tu dominio de Vercel).
   El plan gratis se duerme tras 15 min sin tráfico: la primera petición tarda ~1 minuto.
3. **Frontend:** Vercel, con la variable `VITE_API_URL` apuntando al backend.
