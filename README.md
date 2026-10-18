# Parche SIGER — Gestión de reservas + QR + panel de admin

Este zip contiene SOLO los archivos modificados en la última tanda. Extrae el zip
sobre tu proyecto `siger/` local (reemplazando los archivos existentes).

## Contenido

### Backend (4 archivos Java)
- `backend/src/main/java/com/siger/dto/ReservacionResponse.java` — + campo `participanteNombre`
- `backend/src/main/java/com/siger/service/ReservacionService.java` — + métodos `pausar`, `confirmar`, `cancelar`, `listarTodas`, `cancelarComoAdmin`
- `backend/src/main/java/com/siger/controller/ReservacionController.java` — + endpoints PATCH /{id}/pausar, /confirmar, /cancelar; GET / (admin); PATCH /admin/{id}/cancelar
- `backend/src/main/java/com/siger/repository/ReservacionRepository.java` — `contarActivas` ahora incluye 'Pausada'

### Frontend (4 archivos + 1 CSS)
- `frontend/src/api/reservaciones.js` — + funciones `pausarReservacion`, `confirmarReservacion`, `cancelarReservacion`, `getTodasReservaciones`, `cancelarComoAdmin`
- `frontend/src/pages/Comprobante.jsx` — QR real (librería `qrcode`) reemplaza el placeholder
- `frontend/src/pages/MisReservaciones.jsx` — botones Pausar/Confirmar/Cancelar según estado
- `frontend/src/components/AdminReservas.jsx` — NUEVO: tabla de admin con QR modal + cancelar
- `frontend/src/styles/panel-additions.css` — estilos nuevos (tabla, badges de estado, QR modal). **Append** al final de tu `panel.css` existente.

### SQL (1 archivo)
- `database/view-cupos-pausada.sql` — actualiza la vista `vw_eventos_disponibles` para contar 'Pausada' como activa

## Cómo aplicar

1. **Instala la librería de QR** (si no la tienes):
   ```powershell
   cd siger\frontend
   npm install qrcode
   ```

2. **Copia los archivos del parche** sobre tu proyecto:
   - Descomprime este zip y copia las carpetas `backend/`, `frontend/`, `database/` sobre tu `siger/` local (reemplaza los archivos existentes).

3. **Append de CSS**: abre `frontend/src/styles/panel.css` y al FINAL del archivo, pega el contenido de `panel-additions.css` (o únelos).

4. **Renderiza AdminReservas en PanelAdmin**: abre `frontend/src/pages/PanelAdmin.jsx`:
   - Arriba añade: `import AdminReservas from '../components/AdminReservas.jsx'`
   - En el JSX, después de la tabla de usuarios, añade: `<AdminReservas />`

5. **Aplica el SQL de la vista** (en Workbench o terminal):
   ```powershell
   Get-Content database\view-cupos-pausada.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
   ```

6. **Recompila + arranca el backend**:
   ```powershell
   cd siger\backend
   mvn clean compile "-Dmaven.test.skip=true"
   $env:DB_PASSWORD = "CambiaEstaClave123!"
   $env:JWT_SECRET = "clave-secreta-de-desarrollo-de-al-menos-32-caracteres-siger"
   mvn spring-boot:run "-Dmaven.test.skip=true"
   ```

7. **Reinicia el frontend** (Ctrl+C en la terminal del frontend + `npm run dev`).

## Estados de reserva

| Estado | Botones visibles (participante) | ¿Cuenta para cupo? |
|---|---|---|
| Confirmada | ⏸ Pausar, ✕ Cancelar | Sí |
| Pausada | ✓ Confirmar, ✕ Cancelar | Sí |
| Pendiente | ✓ Confirmar, ✕ Cancelar | Sí |
| Cancelada | (sin botones) | No (libera cupo) |

El admin puede ver TODAS las reservas (tabla con QR + cancelar cualquiera).
