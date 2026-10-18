import http from './http.js'

/**
 * GET /api/notificaciones/mis (autenticado).
 */
export function getMis() {
  return http.get('/notificaciones/mis').then((r) => r.data)
}

/**
 * GET /api/notificaciones/mis/no-leidas (autenticado).
 */
export function getNoLeidas() {
  return http.get('/notificaciones/mis/no-leidas').then((r) => r.data)
}

/**
 * PATCH /api/notificaciones/{id}/leer
 */
export function marcarLeida(id) {
  return http.patch(`/notificaciones/${id}/leer`).then((r) => r.data)
}
