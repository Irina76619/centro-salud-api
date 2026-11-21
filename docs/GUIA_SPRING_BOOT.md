# Guía para explicar este proyecto (Spring Boot sin miedo)

## La idea en una frase
> "Pasé mi sistema de consola a una API REST: el frontend en React pide datos por HTTP y el backend en Spring Boot
> aplica las reglas de negocio, guarda en MySQL y protege cada ruta según el rol."

## Qué cambió respecto a la versión de consola
| Antes (consola) | Ahora (Spring Boot) | Por qué es mejor |
|---|---|---|
| `PacienteDAO` con SQL a mano (122 líneas) | `PacienteRepository` (interfaz de ~10 líneas) | Spring genera el SQL a partir del nombre del método |
| `MenuConsola` mezclaba menú y reglas | `Controlador` (HTTP) + `Servicio` (reglas) | Cada clase hace una sola cosa |
| `System.out.println("Ese DNI ya existe")` | `ApiException` → JSON con código 409 | El frontend puede mostrar el error |
| Clave en texto plano | BCrypt | Si roban la BD no ven las claves |
| "3 intentos y se cierra" | Bloqueo 5 min tras 3 fallos | Un servidor web no se "cierra" |
| Filtrar por médico en el menú | El id sale del **token**, validado en el servidor | Aunque manipulen el frontend, no ven datos ajenos |

## Las anotaciones que realmente usas (aprende solo estas)
| Anotación | Significa | Dónde |
|---|---|---|
| `@SpringBootApplication` | "Aquí arranca todo" | `CentroSaludApplication` |
| `@Entity` / `@Table` | "Esta clase es una tabla" | `modelo` |
| `@Id` + `@GeneratedValue` | Clave primaria autoincremental | `modelo` |
| `@Column`, `@ManyToOne`, `@JoinColumn` | Columna normal, relación N→1 y su llave foránea | `modelo` |
| `@MappedSuperclass` | La clase padre aporta columnas pero no tiene tabla propia | `Persona` |
| `@Service` | Clase de lógica de negocio | `servicio` |
| `@Transactional` | Todo el método es una transacción: o se guarda todo o nada | `servicio` |
| `@RestController` + `@RequestMapping` | Clase que responde peticiones HTTP | `controlador` |
| `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` | Verbo HTTP de cada método | `controlador` |
| `@RequestBody` + `@Valid` | Convierte el JSON a objeto y lo valida | `controlador` |
| `@PathVariable` / `@RequestParam` | Dato en la ruta (`/pacientes/5`) / en la query (`?dni=...`) | `controlador` |
| `@RestControllerAdvice` | Un solo lugar que convierte excepciones en respuestas JSON | `ManejadorErrores` |

## Conceptos clave (con sus palabras)
- **Inyección de dependencias:** no hago `new PacienteRepository()`; lo pido en el constructor y Spring me lo entrega. Facilita probar y cambiar piezas.
- **JPA/Hibernate (ORM):** mapea clases ↔ tablas. Trabajo con objetos y no escribo SQL.
- **DTO:** objeto especial para entrar/salir de la API. No expongo las entidades porque traerían la clave hash y acoplarían el frontend a la BD.
- **JWT:** texto firmado que el servidor da al hacer login. El frontend lo envía en cada petición; nadie puede alterarlo sin la clave secreta. Al ser *stateless*, el servidor no guarda sesiones.
- **LAZY:** las relaciones (médico de una cita) se cargan solo cuando se usan. Por eso los métodos de servicio son `@Transactional`, y `@EntityGraph` evita consultas repetidas (problema N+1).
- **CORS:** el navegador bloquea llamadas entre dominios distintos (Vercel → Render) salvo que el backend las autorice (`CORS_ORIGINS`).

## Preguntas típicas de entrevista (y cómo responderlas con ESTE proyecto)
1. **¿Por qué separar Controller, Service y Repository?** → Controller: HTTP. Service: reglas (ej. "la cita debe caer en el horario del médico"). Repository: acceso a datos. Así cada capa se cambia sin romper las otras.
2. **¿Dónde está el polimorfismo?** → `Persona.descripcion()` es abstracto; `Medico`, `Paciente` y `Administrativo` lo implementan distinto. En `AuthService.login` trato a ambos como `Persona`.
3. **¿Cómo evitas que un médico vea pacientes de otro?** → El id del médico viene del token, no de la petición, y `AtencionService` verifica la pertenencia antes de devolver o editar algo.
4. **¿Cómo guardas las claves?** → BCrypt: hash con sal, irreversible. Se compara con `matches`, nunca se descifra.
5. **¿Qué hace `@Transactional`?** → Si algo falla a mitad (ej. al atender una cita: crear historial, recetas y cambiar estado), se deshace todo.
6. **¿Qué es el problema N+1?** → Listar 100 citas y hacer 1 consulta extra por cada paciente/médico. Se resuelve con `@EntityGraph` (un join).
7. **¿Por qué `ddl-auto=none`?** → El esquema lo controla `centro_salud.sql`; que Hibernate no modifique tablas es más seguro.
8. **¿Qué mejorarías?** → Tests automatizados, paginación en listados, refresh tokens, migraciones con Flyway y bloqueo de login persistente (hoy es en memoria).
