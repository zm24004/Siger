-- =============================================================================
-- MIGRACIÓN 001 — Extender la tabla usuario con apellido, teléfono y país
-- =============================================================================
-- Aplicar con:  cmd /c "mysql -u root -p siger < database\migration-001-usuarios-extendidos.sql"
-- (usar cmd para que los acentos no se re-codifiquen; o cargar desde Workbench)
-- =============================================================================

USE siger;

ALTER TABLE usuario
  ADD COLUMN apellido   VARCHAR(100) NULL AFTER nombre,
  ADD COLUMN telefono   VARCHAR(30)  NULL AFTER email,
  ADD COLUMN codigoPais VARCHAR(10)  NULL AFTER telefono;

-- Verificación
DESCRIBE usuario;
