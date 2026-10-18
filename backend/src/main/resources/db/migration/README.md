# Migraciones de base de datos — SIGER

## Origen de la verdad

La **fuente de la verdad** del esquema de base de datos de SIGER es el script único:

```
/database/siger.sql
```

Este script:
- Hace `DROP DATABASE` + `CREATE DATABASE` cada vez que se ejecuta (idempotente destructivo).
- Crea las 12 tablas + 1 vista + datos semilda.
- Se ejecuta automáticamente en Docker Compose al montarse en
  `/docker-entrypoint-initdb.d/siger.sql` (solo la **primera vez** que se inicializa
  el volumen de datos).

## ¿Por qué sin Flyway/Liquibase (por ahora)?

En la **fase inicial del proyecto** se optó por un script SQL único por:

1. **Simplicidad:** los estudiantes/miembros del equipo entienden SQL estándar sin
   curva de aprendizaje de una herramienta de migraciones.
2. **Reinicio limpio:** para desarrollo es útil poder regenerar la BD completa en
   un solo comando.
3. **Equivalencia con la guía original:** el entregable académico trabajaba con
   un único script SQL.

## Esta carpeta (reservada)

Esta carpeta `db/migration/` queda **reservada** para adoptar Flyway o Liquibase
en el **sprint S5** (Hardening), cuando:

- El esquema esté estable.
- Se necesite evolución de la BD sin pérdida de datos en producción.
- Se añadan tests de integración con Testcontainers.

Cuando eso ocurra, los archivos de esta carpeta seguirán la convención:

- **Flyway:** `V{version}__{descripcion}.sql` (p. ej. `V1__schema_inicial.sql`,
  `V2__añadir_indice_reserva_email.sql`).
- **Liquibase:** `db.changelog-master.yaml` + `db.changelog-{X}.yaml`.

Y `application-prod.properties` deberá activar `spring.flyway.enabled=true`
(ahora está en `false`) o `spring.liquibase.enabled=true`.

## Cómo regenerar la BD ahora

```bash
# Local (requiere MySQL en localhost)
./scripts/reset-db.sh

# Con Docker (destruye el volumen y recrea)
docker compose down -v
docker compose up --build
```

> **Importante:** `reset-db.sh` y el script `siger.sql` son destructivos. **NO
> usar en producción** sobre una base con datos reales sin backup previo.
