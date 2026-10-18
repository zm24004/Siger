import http from './http.js'

/**
 * GET /api/usuarios (ADMINISTRADOR).
 */
export function getUsuarios() {
  return http.get('/usuarios').then((r) => r.data)
}

/**
 * PATCH /api/usuarios/{id}/estado (ADMINISTRADOR).
 * Body: { estado }  — 'ACTIVO' | 'INACTIVO'
 */
export function cambiarEstado(id, estado) {
  return http.patch(`/usuarios/${id}/estado`, { estado }).then((r) => r.data)
}
