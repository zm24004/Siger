import http from './http.js'

/**
 * GET /api/eventos → listado público de eventos.
 */
export function getCatalogo() {
  return http.get('/eventos').then((r) => r.data)
}

/**
 * GET /api/eventos/mis-eventos → listado privado del organizador autenticado.
 */
export function getMisEventos() {
  return http.get('/eventos/mis-eventos').then((r) => r.data)
}

/**
 * GET /api/eventos/{id} → detalle público.
 */
export function getEvento(id) {
  return http.get(`/eventos/${id}`).then((r) => r.data)
}

/**
 * POST /api/eventos → crear evento (ORGANIZADOR).
 * Body: EventoRequest
 */
export function crearEvento(data) {
  return http.post('/eventos', data).then((r) => r.data)
}

/**
 * PATCH /api/eventos/{id}/cancelar (ORGANIZADOR).
 */
export function cancelarEvento(id) {
  return http.patch(`/eventos/${id}/cancelar`).then((r) => r.data)
}
