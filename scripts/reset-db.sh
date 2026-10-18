#!/usr/bin/env bash
# =============================================================================
# SIGER — reset-db.sh
# Reinicia la base de datos desde cero ejecutando database/siger.sql
# DESTRUCTIVO: borra y recrea la BD `siger`. No usar en producción.
# =============================================================================
set -euo pipefail

C_BLUE='\033[0;34m'
C_GREEN='\033[0;32m'
C_YELLOW='\033[1;33m'
C_RED='\033[0;31m'
C_RESET='\033[0m'

log()   { echo -e "${C_BLUE}[INFO]${C_RESET}  $*"; }
ok()    { echo -e "${C_GREEN}[OK]${C_RESET}    $*"; }
warn()  { echo -e "${C_YELLOW}[WARN]${C_RESET}  $*"; }
error() { echo -e "${C_RED}[ERROR]${C_RESET} $*" >&2; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
SQL_FILE="${ROOT_DIR}/database/siger.sql"

# -----------------------------------------------------------------------------
# Validar existencia del SQL
# -----------------------------------------------------------------------------
if [[ ! -f "${SQL_FILE}" ]]; then
    error "No se encontró ${SQL_FILE}"
    exit 1
fi

# -----------------------------------------------------------------------------
# Pedir credenciales de root si no fueron pasadas
# -----------------------------------------------------------------------------
MYSQL_ROOT_USER="${MYSQL_ROOT_USER:-root}"
if [[ -z "${MYSQL_ROOT_PASSWORD:-}" ]]; then
    warn "MYSQL_ROOT_PASSWORD no definido. Se pedirá interactivamente."
    warn "(Para no interactivo: MYSQL_ROOT_PASSWORD=xxx $0)"
fi

# -----------------------------------------------------------------------------
# Confirmar (a menos que se pase -y)
# -----------------------------------------------------------------------------
FORCE_YES="${1:-}"
if [[ "${FORCE_YES}" != "-y" && "${FORCE_YES}" != "--yes" ]]; then
    warn "Esto va a BORRAR y recrear la base de datos 'siger'."
    warn "Todas las reservas, usuarios y eventos de prueba se perderán."
    read -r -p "¿Continuar? [y/N] " confirm
    if [[ "${confirm,,}" != "y" && "${confirm,,}" != "si" && "${confirm,,}" != "sí" ]]; then
        log "Operación cancelada. No se hicieron cambios."
        exit 0
    fi
fi

# -----------------------------------------------------------------------------
# Ejecutar script SQL
# -----------------------------------------------------------------------------
log "Ejecutando ${SQL_FILE} como ${MYSQL_ROOT_USER}..."
if [[ -n "${MYSQL_ROOT_PASSWORD:-}" ]]; then
    MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysql -u "${MYSQL_ROOT_USER}" -h localhost < "${SQL_FILE}"
else
    mysql -u "${MYSQL_ROOT_USER}" -h localhost -p < "${SQL_FILE}"
fi

ok "Base de datos 'siger' reinicializada."
ok "Tablas y datos semilla cargados desde siger.sql."
log "Recuerda recrear el usuario 'siger_app' si lo necesitas:"
log "  CREATE USER 'siger_app'@'localhost' IDENTIFIED BY '<password>';"
log "  GRANT ALL PRIVILEGES ON siger.* TO 'siger_app'@'localhost'; FLUSH PRIVILEGES;"
