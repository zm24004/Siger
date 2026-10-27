-- =============================================================================
-- SIGER — Datos semilla para MySQL 8.0.16+
-- Ejecutar después de database/siger.sql.
-- IMPORTANTE: las contraseñas deben ser hashes BCrypt.
-- Hash BCrypt para "Admin123$" : $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68u5jQ1Vu2Hfe
-- =============================================================================

USE siger;

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
