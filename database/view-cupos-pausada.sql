-- =============================================================================
-- Actualizar la vista vw_eventos_disponibles para contar 'Pausada' como activa
-- (una reserva pausada sigue ocupando cupo; solo 'Cancelada' libera).
-- Ejecutar en MySQL Workbench o:  mysql -u root -p siger < database/view-cupos-pausada.sql
-- =============================================================================

USE siger;

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
