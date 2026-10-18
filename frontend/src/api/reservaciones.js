import http from './http.js'

export async function reservar(idEvento) {
  const res = await http.post(`/reservaciones/evento/${idEvento}`)
  return res.data
}

export async function getMisReservaciones() {
  const res = await http.get('/reservaciones/mis-reservaciones')
  return res.data
}

export async function getReservacion(codigo) {
  const res = await http.get(`/reservaciones/${codigo}`)
  return res.data
}

// Modificar estado de la reserva (participante)
export async function pausarReservacion(id) {
  const res = await http.patch(`/reservaciones/${id}/pausar`)
  return res.data
}

export async function confirmarReservacion(id) {
  const res = await http.patch(`/reservaciones/${id}/confirmar`)
  return res.data
}

export async function cancelarReservacion(id) {
  const res = await http.patch(`/reservaciones/${id}/cancelar`)
  return res.data
}

// Admin: listar todas + cancelar cualquiera
export async function getTodasReservaciones() {
  const res = await http.get('/reservaciones')
  return res.data
}

export async function cancelarComoAdmin(id) {
  const res = await http.patch(`/reservaciones/admin/${id}/cancelar`)
  return res.data
}
