-- =============================================================================
-- SIGER — Script de base de datos MySQL 8.0.16+
-- 12 tablas (3 de subtipo con herencia JOINED), índices, 1 vista y datos semilla.
-- Ejecutar desde la raíz del repo:  mysql -u root -p < database/siger.sql
-- Convención: columnas en camelCase (coincide con las entidades JPA).
-- =============================================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET collation_connection = utf8mb4_unicode_ci;

DROP DATABASE IF EXISTS siger;
CREATE DATABASE siger
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE siger;

-- -----------------------------------------------------------------------------
-- 1. usuario (tabla base, herencia JOINED)
-- -----------------------------------------------------------------------------
CREATE TABLE usuario (
  idUsuario      BIGINT       NOT NULL AUTO_INCREMENT,
  nombre         VARCHAR(100) NOT NULL,
  apellido       VARCHAR(100) NOT NULL,
  email          VARCHAR(150) NOT NULL,
  telefono       VARCHAR(20)  NOT NULL,
  codigoPais     VARCHAR(10)  NOT NULL,
  contrasena     VARCHAR(255) NOT NULL,
  estado         BOOLEAN      NOT NULL DEFAULT TRUE,
  fechaRegistro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idUsuario),
  UNIQUE KEY uq_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 2. administrador (subtipo)
-- -----------------------------------------------------------------------------
CREATE TABLE administrador (
  idUsuario  BIGINT NOT NULL,
  PRIMARY KEY (idUsuario),
  CONSTRAINT fk_admin_usuario FOREIGN KEY (idUsuario)
    REFERENCES usuario(idUsuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 3. organizador (subtipo)
-- -----------------------------------------------------------------------------
CREATE TABLE organizador (
  idUsuario  BIGINT NOT NULL,
  PRIMARY KEY (idUsuario),
  CONSTRAINT fk_org_usuario FOREIGN KEY (idUsuario)
    REFERENCES usuario(idUsuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 4. participante (subtipo)
-- -----------------------------------------------------------------------------
CREATE TABLE participante (
  idUsuario  BIGINT NOT NULL,
  PRIMARY KEY (idUsuario),
  CONSTRAINT fk_part_usuario FOREIGN KEY (idUsuario)
    REFERENCES usuario(idUsuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 5. ubicacion
-- -----------------------------------------------------------------------------
CREATE TABLE ubicacion (
  idUbicacion   BIGINT       NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(150) NOT NULL,
  direccion     VARCHAR(255),
  tipo          VARCHAR(50)  NOT NULL DEFAULT 'Presencial',
  enlaceVirtual VARCHAR(500),
  PRIMARY KEY (idUbicacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 6. evento
-- -----------------------------------------------------------------------------
CREATE TABLE evento (
  idEvento        BIGINT       NOT NULL AUTO_INCREMENT,
  nombre          VARCHAR(200) NOT NULL,
  descripcion     TEXT,
  fecha           DATE         NOT NULL,
  horaInicio      TIME         NOT NULL,
  horaFin         TIME,
  capacidadMaxima INT          NOT NULL,
  estado          VARCHAR(20)  NOT NULL DEFAULT 'Activo',
  fechaCreacion   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  idUbicacion     BIGINT       NOT NULL,
  idOrganizador   BIGINT       NOT NULL,
  PRIMARY KEY (idEvento),
  CONSTRAINT fk_evento_ubicacion FOREIGN KEY (idUbicacion)
    REFERENCES ubicacion(idUbicacion),
  CONSTRAINT fk_evento_organizador FOREIGN KEY (idOrganizador)
    REFERENCES organizador(idUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 7. reservacion
-- -----------------------------------------------------------------------------
CREATE TABLE reservacion (
  idReservacion     BIGINT      NOT NULL AUTO_INCREMENT,
  fechaReservacion  DATE        NOT NULL DEFAULT (CURRENT_DATE),
  codigoReservacion VARCHAR(20) NOT NULL,
  estado            VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
  idEvento          BIGINT      NOT NULL,
  idParticipante    BIGINT      NOT NULL,
  PRIMARY KEY (idReservacion),
  UNIQUE KEY uq_reservacion_codigo (codigoReservacion),
  UNIQUE KEY uq_evento_participante (idEvento, idParticipante),
  CONSTRAINT fk_res_evento FOREIGN KEY (idEvento)
    REFERENCES evento(idEvento),
  CONSTRAINT fk_res_participante FOREIGN KEY (idParticipante)
    REFERENCES participante(idUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 8. asistencia
-- -----------------------------------------------------------------------------
CREATE TABLE asistencia (
  idAsistencia   BIGINT      NOT NULL AUTO_INCREMENT,
  fechaAsistencia DATE       NOT NULL DEFAULT (CURRENT_DATE),
  horaAsistencia TIME        NOT NULL DEFAULT (CURRENT_TIME),
  estado         VARCHAR(20) NOT NULL DEFAULT 'Presente',
  idReservacion  BIGINT      NOT NULL,
  PRIMARY KEY (idAsistencia),
  UNIQUE KEY uq_asistencia_reservacion (idReservacion),
  CONSTRAINT fk_asis_reservacion FOREIGN KEY (idReservacion)
    REFERENCES reservacion(idReservacion) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 9. recurso
-- -----------------------------------------------------------------------------
CREATE TABLE recurso (
  idRecurso   BIGINT       NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(150) NOT NULL,
  descripcion TEXT,
  cantidad    INT          NOT NULL DEFAULT 1,
  tipo        VARCHAR(50)  NOT NULL DEFAULT 'General',
  estado      VARCHAR(20)  NOT NULL DEFAULT 'Disponible',
  PRIMARY KEY (idRecurso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 10. evento_recurso (N:M con atributo cantidadAsignada)
-- -----------------------------------------------------------------------------
CREATE TABLE evento_recurso (
  idEvento          BIGINT NOT NULL,
  idRecurso         BIGINT NOT NULL,
  cantidadAsignada  INT    NOT NULL DEFAULT 1,
  PRIMARY KEY (idEvento, idRecurso),
  CONSTRAINT fk_er_evento FOREIGN KEY (idEvento)
    REFERENCES evento(idEvento) ON DELETE CASCADE,
  CONSTRAINT fk_er_recurso FOREIGN KEY (idRecurso)
    REFERENCES recurso(idRecurso) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 11. reporte
-- -----------------------------------------------------------------------------
CREATE TABLE reporte (
  idReporte       BIGINT       NOT NULL AUTO_INCREMENT,
  tipo            VARCHAR(50)  NOT NULL,
  parametro       VARCHAR(255),
  contenido       TEXT,
  fechaGeneracion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  idAdministrador BIGINT       NOT NULL,
  PRIMARY KEY (idReporte),
  CONSTRAINT fk_rep_admin FOREIGN KEY (idAdministrador)
    REFERENCES administrador(idUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 12. notificacion
-- -----------------------------------------------------------------------------
CREATE TABLE notificacion (
  idNotificacion BIGINT      NOT NULL AUTO_INCREMENT,
  mensaje        TEXT        NOT NULL,
  tipo           VARCHAR(50) NOT NULL DEFAULT 'Info',
  leido          BOOLEAN     NOT NULL DEFAULT FALSE,
  fechaEnvio     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  idUsuario      BIGINT      NOT NULL,
  PRIMARY KEY (idNotificacion),
  CONSTRAINT fk_not_usuario FOREIGN KEY (idUsuario)
    REFERENCES usuario(idUsuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- ÍNDICES SECUNDARIOS
-- =============================================================================
CREATE INDEX idx_evento_fecha        ON evento(fecha);
CREATE INDEX idx_evento_estado       ON evento(estado);
CREATE INDEX idx_evento_organizador   ON evento(idOrganizador);
CREATE INDEX idx_reservacion_part    ON reservacion(idParticipante);
CREATE INDEX idx_reservacion_evento  ON reservacion(idEvento);
CREATE INDEX idx_reservacion_estado  ON reservacion(estado);
CREATE INDEX idx_asistencia_fecha    ON asistencia(fechaAsistencia);
CREATE INDEX idx_notificacion_usuario ON notificacion(idUsuario);
CREATE INDEX idx_notificacion_leido  ON notificacion(leido);

-- =============================================================================
-- VISTA: eventos disponibles con cupo calculado
-- =============================================================================
CREATE OR REPLACE VIEW vw_eventos_disponibles AS
SELECT
  e.idEvento,
  e.nombre,
  e.descripcion,
  e.fecha,
  e.horaInicio,
  e.horaFin,
  e.capacidadMaxima,
  e.estado,
  u.nombre  AS organizador,
  ub.nombre AS ubicacion,
  ub.direccion,
  ub.tipo   AS modalidad,
  (e.capacidadMaxima -
     (SELECT COUNT(*) FROM reservacion r
       WHERE r.idEvento = e.idEvento
         AND r.estado IN ('Pendiente','Confirmada','Pausada'))) AS cuposDisponibles
FROM evento e
JOIN organizador o ON e.idOrganizador = o.idUsuario
JOIN usuario    u ON o.idUsuario = u.idUsuario
JOIN ubicacion  ub ON e.idUbicacion = ub.idUbicacion;

-- =============================================================================
-- USUARIO DE APLICACIÓN (crearlo con root; NO usar root desde Spring)
-- =============================================================================
-- CREATE USER 'siger_app'@'localhost' IDENTIFIED BY 'CambiaEstaClave123!';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON siger.* TO 'siger_app'@'localhost';
-- FLUSH PRIVILEGES;

-- =============================================================================
-- DATOS SEMILLA
-- IMPORTANTE: las contraseñas deben ser hashes BCrypt.
-- Hash BCrypt para "Admin123$" : $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe
-- (Mismo hash usado en los 3 usuarios existentes; se mantiene sin cambios)
-- =============================================================================

-- Usuarios base (mantener los tres existentes tal como están)
INSERT INTO usuario (nombre, apellido, email, telefono, codigoPais, contrasena, estado) VALUES
  ('Administrador',     'General',   'admin@siger.com', '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('Org Demo',        'Eventos',   'org@siger.test',    '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('Part Demo',       'Prueba',    'part@siger.test',   '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE);

INSERT INTO administrador (idUsuario) VALUES (1);
INSERT INTO organizador   (idUsuario) VALUES (2);
INSERT INTO participante  (idUsuario) VALUES (3);

-- Participantes adicionales (mantener usuarios existentes sin cambios)
INSERT INTO usuario (nombre, apellido, email, telefono, codigoPais, contrasena, estado) VALUES
  ('Ana',             'Martínez',  'ana.martinez@siger.test',  '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('Carlos',          'Ramírez',   'carlos.ramirez@siger.test', '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('María',           'López',     'maria.lopez@siger.test',    '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('José',            'Hernández', 'jose.hernandez@siger.test','+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('Laura',           'González',  'laura.gonzalez@siger.test', '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('Daniel',          'Pérez',     'daniel.perez@siger.test',   '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('Sofía',           'Torres',    'sofia.torres@siger.test',   '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('Miguel',          'Castro',    'miguel.castro@siger.test',  '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('Valeria',         'Flores',    'valeria.flores@siger.test', '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('Andrés',          'Morales',   'andres.morales@siger.test', '+503', 'SV', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE);

INSERT INTO participante (idUsuario) VALUES (4),(5),(6),(7),(8),(9),(10),(11),(12),(13);

-- Ubicaciones
INSERT INTO ubicacion (nombre, direccion, tipo, enlaceVirtual) VALUES
  ('Auditorio central', 'Campus FMO, San Salvador', 'Presencial', NULL),
  ('Sala virtual A',    NULL, 'Virtual', 'https://meet.siger.test/sala-a'),
  ('Aula Magna',        'Edificio A, Planta Baja, San Salvador', 'Presencial', NULL),
  ('Sala de Conferencias B', 'Edificio B, Piso 2, San Salvador', 'Presencial', NULL),
  ('Auditorio Satélite', 'Sede Norte, Santa Tecla', 'Presencial', NULL),
  ('Sala Virtual Internacional', NULL, 'Virtual', 'https://meet.siger.test/sala-int');

-- Eventos
INSERT INTO evento (nombre, descripcion, fecha, horaInicio, horaFin,
                    capacidadMaxima, estado, idUbicacion, idOrganizador) VALUES
  ('Congreso de Investigación 2026',
   'Jornada académica con ponentes nacionales e internacionales.',
   '2026-10-20', '09:00:00', '13:00:00', 100, 'Activo', 1, 2),
  ('Taller de Metodología',
   'Sesión práctica sobre métodos cualitativos.',
   '2026-11-05', '15:00:00', '17:00:00', 25, 'Activo', 2, 2),
  ('Seminario de Inteligencia Artificial',
   'Introducción a IA, Machine Learning y casos aplicados.',
   '2026-11-12', '09:00:00', '12:00:00', 80, 'Activo', 3, 2),
  ('Workshop de Desarrollo Web',
   'Taller práctico de desarrollo frontend/backend con enfoque a proyectos.',
   '2026-11-18', '13:00:00', '17:00:00', 40, 'Activo', 4, 2),
  ('Foro de Emprendimiento Universitario',
   'Panel con emprendedores, mentorías y networking.',
   '2026-11-25', '08:30:00', '11:30:00', 150, 'Activo', 1, 2),
  ('Charla: Ciberseguridad para Todos',
   'Buenas prácticas, phishing y protección de datos personales.',
   '2026-12-03', '18:00:00', '20:00:00', 60, 'Activo', 6, 2),
  ('Hackathon SIGER 2026',
   'Competencia de 24 horas con retos reales y premiación.',
   '2026-12-10', '08:00:00', '08:00:00', 70, 'Activo', 5, 2),
  ('Taller de Datos y Visualización',
   'Uso práctico de herramientas para análisis y visualización de datos.',
   '2026-12-17', '14:00:00', '17:00:00', 35, 'Activo', 2, 2);

-- Recursos
INSERT INTO recurso (nombre, descripcion, cantidad, tipo, estado) VALUES
  ('Proyector',     'Proyector Full HD',        10, 'Audiovisual',      'Disponible'),
  ('Micrófono',     'Micrófono inalámbrico',     8, 'Audiovisual',      'Disponible'),
  ('Laptop',        'Laptop i7 16GB',           15, 'Cómputo',          'Disponible'),
  ('Extensión',     'Regleta eléctrica 6 tomas', 12, 'Eléctrico',        'Disponible'),
  ('Aula 101',      'Aula con 30 pupitres',      1, 'Infraestructura',  'Disponible'),
  ('Mesas Redondas','Mesas para networking',     6, 'Mobiliario',       'Disponible'),
  ('Router',        'Router WiFi empresarial',   3, 'Redes',            'Disponible'),
  ('Kit de Premios','Kit para ganadores',        5, 'Premiación',       'Disponible');

-- Asignación de recursos a eventos
INSERT INTO evento_recurso (idEvento, idRecurso, cantidadAsignada) VALUES
  (1, 1, 2),
  (1, 2, 4),
  (1, 5, 1),
  (2, 1, 1),
  (3, 1, 2),
  (3, 2, 2),
  (3, 3, 5),
  (4, 1, 1),
  (4, 3, 10),
  (4, 4, 4),
  (5, 1, 3),
  (5, 2, 6),
  (5, 6, 6),
  (6, 1, 1),
  (6, 2, 1),
  (7, 1, 4),
  (7, 2, 8),
  (7, 3, 15),
  (7, 7, 2),
  (7, 8, 2),
  (8, 1, 1),
  (8, 3, 8),
  (8, 4, 3);

-- Reservaciones (incluye al participante demo y otros)
INSERT INTO reservacion (fechaReservacion, codigoReservacion, estado, idEvento, idParticipante) VALUES
  (CURRENT_DATE, 'RES-000001', 'Confirmada', 1, 3),
  (CURRENT_DATE, 'RES-000002', 'Confirmada', 1, 4),
  (CURRENT_DATE, 'RES-000003', 'Confirmada', 1, 5),
  (CURRENT_DATE, 'RES-000004', 'Pendiente',   1, 6),
  (CURRENT_DATE, 'RES-000005', 'Confirmada', 2, 3),
  (CURRENT_DATE, 'RES-000006', 'Confirmada', 2, 7),
  (CURRENT_DATE, 'RES-000007', 'Confirmada', 3, 4),
  (CURRENT_DATE, 'RES-000008', 'Confirmada', 3, 8),
  (CURRENT_DATE, 'RES-000009', 'Pendiente',   3, 9),
  (CURRENT_DATE, 'RES-000010', 'Confirmada', 4, 5),
  (CURRENT_DATE, 'RES-000011', 'Confirmada', 4, 6),
  (CURRENT_DATE, 'RES-000012', 'Pendiente',   4, 10),
  (CURRENT_DATE, 'RES-000013', 'Confirmada', 5, 3),
  (CURRENT_DATE, 'RES-000014', 'Confirmada', 5, 7),
  (CURRENT_DATE, 'RES-000015', 'Confirmada', 5, 11),
  (CURRENT_DATE, 'RES-000016', 'Confirmada', 6, 4),
  (CURRENT_DATE, 'RES-000017', 'Pendiente',   6, 12),
  (CURRENT_DATE, 'RES-000018', 'Confirmada', 7, 8),
  (CURRENT_DATE, 'RES-000019', 'Confirmada', 7, 9),
  (CURRENT_DATE, 'RES-000020', 'Pendiente',   7, 13),
  (CURRENT_DATE, 'RES-000021', 'Confirmada', 8, 5),
  (CURRENT_DATE, 'RES-000022', 'Confirmada', 8, 6);

-- Asistencias (para reservaciones confirmadas)
INSERT INTO asistencia (fechaAsistencia, horaAsistencia, estado, idReservacion) VALUES
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 1),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 2),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 3),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 5),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 7),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 8),
  (CURRENT_DATE, CURRENT_TIME, 'Ausente',   10),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 11),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 13),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 14),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 15),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 16),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 18),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 19),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 21),
  (CURRENT_DATE, CURRENT_TIME, 'Presente', 22);

-- Notificaciones
INSERT INTO notificacion (mensaje, tipo, leido, idUsuario) VALUES
  ('Bienvenido al sistema SIGER.', 'Info', FALSE, 3),
  ('Tu reservación para Congreso de Investigación 2026 ha sido confirmada.', 'Info', FALSE, 3),
  ('Tu reservación para Taller de Metodología ha sido confirmada.', 'Info', FALSE, 3),
  ('Nuevo evento disponible: Seminario de Inteligencia Artificial.', 'Info', FALSE, 4),
  ('Recordatorio: Foro de Emprendimiento Universitario - 2026-11-25 08:30.', 'Recordatorio', FALSE, 3),
  ('Recordatorio: Hackathon SIGER 2026 - 2026-12-10 08:00.', 'Recordatorio', FALSE, 8),
  ('Tu asistencia ha sido registrada correctamente.', 'Info', TRUE, 4),
  ('Hay cupos disponibles para Taller de Datos y Visualización.', 'Info', FALSE, 5);
