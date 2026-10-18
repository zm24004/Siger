import http from './http.js'

/**
 * POST /api/asistencias/registrar (ORGANIZADOR).
 * Body: { codigoReservacion }
 */
export function registrarAsistencia(codigoReservacion) {
  return http.post('/asistencias/registrar', { codigoReservacion }).then((r) => r.data)
}
