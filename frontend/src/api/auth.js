import http from './http.js'

/**
 * POST /api/auth/login
 * Body: { email, contrasena }
 * Response: { token, nombre, rol, idUsuario }
 */
export function loginPost(data) {
  return http.post('/auth/login', data).then((r) => r.data)
}

/**
 * POST /api/auth/registro
 * Body: { nombre, apellido, email, telefono, codigoPais, contrasena, rol }
 * Response: { token, nombre, apellido, rol, idUsuario }
 */
export function registroPost(data) {
  return http.post('/auth/registro', data).then((r) => r.data)
}

/**
 * POST /api/auth/recuperar-contrasena
 * Body: { email, nombreOAlias }
 * Response: { message, protocolo }
 * El backend SIEMPRE devuelve el mismo mensaje (protocolo de seguridad):
 * no revela si el email existe o no.
 */
export async function recuperarContrasenaPost(data) {
  const res = await http.post('/auth/recuperar-contrasena', data)
  return res.data
}
