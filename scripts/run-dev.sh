#!/usr/bin/env bash
# =============================================================================
# SIGER — run-dev.sh
# Arranca el stack de desarrollo en local (MySQL + backend + frontend) sin Docker.
# Asume MySQL ya corriendo en localhost:3306 y el script database/siger.sql ejecutado.
# =============================================================================
set -euo pipefail

# Colores para output
C_BLUE='\033[0;34m'
C_GREEN='\033[0;32m'
C_YELLOW='\033[1;33m'
C_RED='\033[0;31m'
C_RESET='\033[0m'

log()    { echo -e "${C_BLUE}[INFO]${C_RESET}  $*"; }
ok()     { echo -e "${C_GREEN}[OK]${C_RESET}    $*"; }
warn()   { echo -e "${C_YELLOW}[WARN]${C_RESET}  $*"; }
error()  { echo -e "${C_RED}[ERROR]${C_RESET} $*" >&2; }

# Directorio raíz del repo (asumiendo que el script vive en scripts/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${ROOT_DIR}"

# -----------------------------------------------------------------------------
# 1. Verificar dependencias
# -----------------------------------------------------------------------------
command -v java   >/dev/null || { error "Java no encontrado. Instala JDK 21."; exit 1; }
command -v mvn    >/dev/null || { error "Maven no encontrado. Instala Maven 3.9+ o usa ./backend/mvnw."; exit 1; }
command -v node   >/dev/null || { error "Node no encontrado. Instala Node 20."; exit 1; }
command -v npm    >/dev/null || { error "npm no encontrado."; exit 1; }
command -v mysqladmin >/dev/null || { error "mysqladmin no encontrado. Instala cliente MySQL."; exit 1; }

# -----------------------------------------------------------------------------
# 2. Valores por defecto de variables de entorno (no sobrescribe las ya puestas)
# -----------------------------------------------------------------------------
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"
export DB_URL="${DB_URL:-jdbc:mysql://localhost:3306/siger?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}"
export DB_USERNAME="${DB_USERNAME:-siger_app}"
export DB_PASSWORD="${DB_PASSWORD:-cambia_esta_password}"
export JWT_SECRET="${JWT_SECRET:-dev_jwt_secret_inseguro_no_usar_en_produccion_xxxxxxxxxxxxxxxxxxxxxx}"
export JWT_EXPIRATION_MS="${JWT_EXPIRATION_MS:-86400000}"
export SERVER_PORT="${SERVER_PORT:-8080}"
export VITE_API_BASE_URL="${VITE_API_BASE_URL:-http://localhost:8080}"
export VITE_APP_NAME="${VITE_APP_NAME:-SIGER-dev}"

warn "Variables de entorno (sobrescribe antes de llamar al script si necesitas):"
warn "  DB_USERNAME=${DB_USERNAME}"
warn "  DB_URL=${DB_URL}"
warn "  JWT_SECRET=${JWT_SECRET:0:8}... (truncado)"
warn ""

# -----------------------------------------------------------------------------
# 3. Comprobar que MySQL está disponible
# -----------------------------------------------------------------------------
log "Comprobando MySQL en localhost:3306..."
for i in {1..30}; do
    if mysqladmin ping -h localhost -u "${DB_USERNAME}" -p"${DB_PASSWORD}" --silent 2>/dev/null; then
        ok "MySQL responde al ping (intento ${i})."
        break
    fi
    if [[ ${i} -eq 30 ]]; then
        error "No se pudo conectar a MySQL con usuario '${DB_USERNAME}'."
        error "Verifica que el servicio esté corriendo y que DB_PASSWORD sea correcto."
        exit 1
    fi
    sleep 2
done

# -----------------------------------------------------------------------------
# 4. Arrancar backend en background
# -----------------------------------------------------------------------------
log "Arrancando backend Spring Boot (perfil ${SPRING_PROFILES_ACTIVE})..."
mkdir -p .logs
( cd backend && mvn -q spring-boot:run ) > .logs/backend.log 2>&1 &
BACKEND_PID=$!
ok "Backend PID=${BACKEND_PID}. Logs en .logs/backend.log"

# Limpieza al salir
cleanup() {
    log "Deteniendo procesos hijos..."
    [[ -n "${BACKEND_PID:-}" ]]  && kill "${BACKEND_PID}" 2>/dev/null || true
    [[ -n "${FRONTEND_PID:-}" ]] && kill "${FRONTEND_PID}" 2>/dev/null || true
    wait 2>/dev/null || true
    ok "Procesos detenidos. Adiós."
}
trap cleanup EXIT INT TERM

# -----------------------------------------------------------------------------
# 5. Esperar a que backend responda
# -----------------------------------------------------------------------------
log "Esperando a que el backend responda en :${SERVER_PORT}..."
for i in {1..60}; do
    if curl -fsS "http://localhost:${SERVER_PORT}/actuator/health" >/dev/null 2>&1; then
        ok "Backend UP en http://localhost:${SERVER_PORT}"
        break
    fi
    if [[ ${i} -eq 60 ]]; then
        error "Backend no respondió en 60s. Revisa .logs/backend.log"
        tail -n 50 .logs/backend.log
        exit 1
    fi
    sleep 2
done

# -----------------------------------------------------------------------------
# 6. Arrancar frontend en background
# -----------------------------------------------------------------------------
log "Arrancando frontend Vite..."
( cd frontend && npm run dev ) > .logs/frontend.log 2>&1 &
FRONTEND_PID=$!
ok "Frontend PID=${FRONTEND_PID}. Logs en .logs/frontend.log"

# Esperar 3s y mostrar URLs
sleep 3
echo ""
ok "==== Stack de desarrollo SIGER listo ===="
ok "  Frontend:  http://localhost:5173"
ok "  Backend:   http://localhost:${SERVER_PORT}"
ok "  API health: http://localhost:${SERVER_PORT}/actuator/health"
ok "  MySQL:     localhost:3306 (BD: siger)"
echo ""
warn "Ctrl-C para detener ambos procesos."
echo ""

# Mantener script vivo y reenviar logs
wait
