-- =============================================================================
-- SIGER — Script de base de datos MySQL 8.0.16+
-- 12 tablas (3 de subtipo con herencia JOINED), índices, 1 vista y datos semilla.
-- Ejecutar desde la raíz del repo:  mysql -u root -p < database/siger.sql
-- Convención: columnas en camelCase (coincide con las entidades JPA).
-- =============================================================================

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
  email          VARCHAR(150) NOT NULL,
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
         AND r.estado IN ('Pendiente','Confirmada'))) AS cuposDisponibles
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
-- Genera uno desde la app (POST /api/auth/registro) o con un generador BCrypt,
-- y reemplaza los placeholders antes de cargar en producción.
-- Hash BCrypt para "Admin123$" : $2a$10$... (pon aquí tu hash real)
-- =============================================================================
INSERT INTO usuario (nombre, email, contrasena, estado) VALUES
  ('Admin SIGER',     'admin@siger.test',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('Org Demo',        'org@siger.test',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE),
  ('Part Demo',       'part@siger.test',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe', TRUE);

INSERT INTO administrador (idUsuario) VALUES (1);
INSERT INTO organizador   (idUsuario) VALUES (2);
INSERT INTO participante  (idUsuario) VALUES (3);

INSERT INTO ubicacion (nombre, direccion, tipo) VALUES
  ('Auditorio central', 'Campus FMO, San Salvador', 'Presencial'),
  ('Sala virtual A',    NULL, 'Virtual');

INSERT INTO evento (nombre, descripcion, fecha, horaInicio, horaFin,
                    capacidadMaxima, estado, idUbicacion, idOrganizador) VALUES
  ('Congreso de Investigación 2026',
   'Jornada académica con ponentes nacionales e internacionales.',
   '2026-10-20', '09:00:00', '13:00:00', 100, 'Activo', 1, 2),
  ('Taller de Metodología',
   'Sesión práctica sobre métodos cualitativos.',
   '2026-11-05', '15:00:00', '17:00:00', 25, 'Activo', 2, 2);

INSERT INTO recurso (nombre, descripcion, cantidad, tipo, estado) VALUES
  ('Proyector',     'Proyector Full HD',    5, 'Audiovisual', 'Disponible'),
  ('Laptop',        'Laptop i7 16GB',        10, 'Cómputo',   'Disponible'),
  ('Aula 101',      'Aula con 30 pupitres', 1, 'Infraestructura', 'Disponible');

INSERT INTO evento_recurso (idEvento, idRecurso, cantidadAsignada) VALUES
  (1, 1, 2),
  (1, 3, 1),
  (2, 1, 1);

INSERT INTO notificacion (mensaje, tipo, leido, idUsuario) VALUES
  ('Bienvenido al sistema SIGER.', 'Info', FALSE, 3);
