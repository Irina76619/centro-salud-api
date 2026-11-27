-- =====================================================================
-- Sistema de Gestión de Centro de Salud - Script MySQL
-- Ejecutar completo (MySQL Workbench: Ctrl+Shift+Enter).
-- Se puede volver a ejecutar: borra y recrea la base de datos.
-- =====================================================================

DROP DATABASE IF EXISTS centro_salud;
CREATE DATABASE centro_salud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE centro_salud;

-- ---------------------------------------------------------------------
-- TABLAS
-- ---------------------------------------------------------------------

CREATE TABLE especialidad (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE medico (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    codigo_medico    VARCHAR(10) NOT NULL UNIQUE,
    clave            VARCHAR(100) NOT NULL,
    dni              VARCHAR(8)  NOT NULL UNIQUE,
    nombre           VARCHAR(50) NOT NULL,
    apellidos        VARCHAR(80) NOT NULL,
    edad             INT NOT NULL CHECK (edad BETWEEN 18 AND 100),
    id_especialidad  INT NOT NULL,
    FOREIGN KEY (id_especialidad) REFERENCES especialidad(id)
);

CREATE TABLE paciente (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    dni        VARCHAR(8)  NOT NULL UNIQUE,
    nombre     VARCHAR(50) NOT NULL,
    apellidos  VARCHAR(80) NOT NULL,
    edad       INT NOT NULL CHECK (edad BETWEEN 0 AND 120),
    telefono   VARCHAR(15)
);

CREATE TABLE administrativo (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    usuario    VARCHAR(30) NOT NULL UNIQUE,
    clave      VARCHAR(100) NOT NULL,
    dni        VARCHAR(8)  NOT NULL UNIQUE,
    nombre     VARCHAR(50) NOT NULL,
    apellidos  VARCHAR(80) NOT NULL,
    edad       INT NOT NULL CHECK (edad BETWEEN 18 AND 100)
);

CREATE TABLE horario (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    dia_semana   VARCHAR(12) NOT NULL,
    hora_inicio  TIME NOT NULL,
    hora_fin     TIME NOT NULL,
    id_medico    INT NOT NULL,
    FOREIGN KEY (id_medico) REFERENCES medico(id) ON DELETE CASCADE
);

CREATE TABLE cita (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora   DATETIME NOT NULL,
    motivo       VARCHAR(150) NOT NULL,
    estado       ENUM('PENDIENTE','ATENDIDA','CANCELADA', 'NO_ASISTIO') NOT NULL DEFAULT 'PENDIENTE',
    id_paciente  INT NOT NULL,
    id_medico    INT NOT NULL,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id),
    FOREIGN KEY (id_medico)   REFERENCES medico(id)
);

CREATE TABLE receta (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    medicamento   VARCHAR(100) NOT NULL,
    indicaciones  VARCHAR(200) NOT NULL,
    fecha         DATE NOT NULL,
    id_cita       INT NOT NULL,
    FOREIGN KEY (id_cita) REFERENCES cita(id) ON DELETE CASCADE
);

CREATE TABLE historial_clinico (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    diagnostico  VARCHAR(200) NOT NULL,
    tratamiento  VARCHAR(200) NOT NULL,
    fecha        DATE NOT NULL,
    id_paciente  INT NOT NULL,
    id_cita      INT NULL,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id) ON DELETE CASCADE,
    FOREIGN KEY (id_cita)     REFERENCES cita(id)     ON DELETE SET NULL
);

-- ---------------------------------------------------------------------
-- DATOS DE PRUEBA
-- ---------------------------------------------------------------------

INSERT INTO especialidad (nombre) VALUES
    ('Medicina General'),
    ('Pediatría'),
    ('Cardiología'),
    ('Dermatología');

-- Cuenta administrativa: admin / admin123   (la clave se guarda como hash BCrypt)
INSERT INTO administrativo (usuario, clave, dni, nombre, apellidos, edad) VALUES
    ('admin', 'admin123', '70000001', 'Rosa', 'Medina Torres', 35);

-- Médicos: entran con su código (MED001...) y la clave 1234   (hash BCrypt)
INSERT INTO medico (codigo_medico, clave, dni, nombre, apellidos, edad, id_especialidad) VALUES
    ('MED001', '1234', '40000001', 'Carlos', 'Ramírez Soto',  45, 1),
    ('MED002', '1234', '40000002', 'Lucía',  'Fernández Paz', 38, 2),
    ('MED003', '1234', '40000003', 'Jorge',  'Salazar Vega',  50, 3);

INSERT INTO paciente (dni, nombre, apellidos, edad, telefono) VALUES
    ('60000001', 'Ana',    'Quispe Huamán',  29, '987654321'),
    ('60000002', 'Luis',   'Mendoza Rojas',  41, '987111222'),
    ('60000003', 'María',  'Castillo Díaz',   8, '987333444'),
    ('60000004', 'Pedro',  'Vargas León',    63, '987555666'),
    ('60000005', 'Sofía',  'Paredes Cruz',   33, '987777888');

INSERT INTO horario (dia_semana, hora_inicio, hora_fin, id_medico) VALUES
    ('Lunes',     '08:00:00', '13:00:00', 1),
    ('Miércoles', '08:00:00', '13:00:00', 1),
    ('Martes',    '09:00:00', '14:00:00', 2),
    ('Jueves',    '09:00:00', '14:00:00', 2),
    ('Viernes',   '14:00:00', '19:00:00', 3);

INSERT INTO cita (fecha_hora, motivo, estado, id_paciente, id_medico) VALUES
    ('2026-10-05 09:00:00', 'Dolor de garganta',       'ATENDIDA',  1, 1),
    ('2026-10-07 10:00:00', 'Control de crecimiento',  'ATENDIDA',  3, 2),
    ('2026-10-09 15:00:00', 'Dolor en el pecho',       'ATENDIDA',  4, 3),
    ('2026-10-12 08:30:00', 'Chequeo general',         'ATENDIDA',  2, 1),
    ('2026-10-15 11:00:00', 'Fiebre persistente',      'NO_ASISTIO', 3, 2),
    ('2026-10-21 09:30:00', 'Seguimiento de gripe',    'ATENDIDA',  1, 1),
    ('2026-10-28 08:00:00', 'Chequeo general',         'PENDIENTE', 5, 1),
    ('2026-10-29 10:00:00', 'Vacunación',              'PENDIENTE', 3, 2),
    ('2026-10-30 16:00:00', 'Control de presión',      'PENDIENTE', 4, 3),
    ('2026-11-04 09:00:00', 'Dolor de cabeza',         'PENDIENTE', 2, 1);

INSERT INTO receta (medicamento, indicaciones, fecha, id_cita) VALUES
    ('Paracetamol 500 mg', 'Una tableta cada 8 horas por 3 días',     '2026-10-05', 1),
    ('Amoxicilina 500 mg', 'Una cápsula cada 8 horas por 7 días',     '2026-10-05', 1),
    ('Vitamina D',         'Una gota diaria por 30 días',             '2026-10-07', 2),
    ('Enalapril 10 mg',    'Una tableta cada mañana',                 '2026-10-09', 3),
    ('Ibuprofeno 400 mg',  'Una tableta cada 8 horas si hay dolor',   '2026-10-21', 6);

INSERT INTO historial_clinico (diagnostico, tratamiento, fecha, id_paciente, id_cita) VALUES
    ('Faringitis aguda',        'Antibiótico y analgésico por 7 días',      '2026-10-05', 1, 1),
    ('Crecimiento normal',      'Suplemento de vitamina D',                 '2026-10-07', 3, 2),
    ('Hipertensión leve',       'Enalapril y control de dieta',             '2026-10-09', 4, 3),
    ('Paciente sano',           'Sin tratamiento, control en 6 meses',      '2026-10-12', 2, 4),
    ('Cuadro gripal resuelto',  'Ibuprofeno y reposo',                      '2026-10-21', 1, 6);

-- ---------------------------------------------------------------------
-- CONSULTAS DE EJEMPLO PARA LOS REPORTES (copiar a los métodos de ServicioReportes)
-- ---------------------------------------------------------------------

-- Citas por médico
-- SELECT m.nombre, m.apellidos, COUNT(*) AS total
-- FROM cita c JOIN medico m ON c.id_medico = m.id
-- GROUP BY m.id;

-- Citas por especialidad
-- SELECT e.nombre, COUNT(*) AS total
-- FROM cita c JOIN medico m ON c.id_medico = m.id
--             JOIN especialidad e ON m.id_especialidad = e.id
-- GROUP BY e.id;

-- Citas por día
-- SELECT * FROM cita WHERE DATE(fecha_hora) = '2026-10-28';

-- Citas por mes
-- SELECT * FROM cita WHERE MONTH(fecha_hora) = 10 AND YEAR(fecha_hora) = 2026;

-- Historial clínico completo de un paciente
-- SELECT h.fecha, h.diagnostico, h.tratamiento
-- FROM historial_clinico h WHERE h.id_paciente = 1 ORDER BY h.fecha;

-- Recetas y tratamientos asignados
-- SELECT p.nombre, r.medicamento, r.indicaciones, r.fecha
-- FROM receta r JOIN cita c ON r.id_cita = c.id
--               JOIN paciente p ON c.id_paciente = p.id;
