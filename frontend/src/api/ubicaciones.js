import http from './http.js'

/**
 * GET /api/ubicaciones → listado público de ubicaciones.
 */
export function getUbicaciones() {
  return http.get('/ubicaciones').then((r) => r.data)
}

/**
 * POST /api/ubicaciones (ORGANIZADOR).
 * Body: { nombre, direccion, capacidad }
 */
export function crearUbicacion(data) {
  return http.post('/ubicaciones', data).then((r) => r.data)
}
