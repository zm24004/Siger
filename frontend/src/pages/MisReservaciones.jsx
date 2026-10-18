import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMisReservaciones, pausarReservacion, confirmarReservacion, cancelarReservacion } from '../api/reservaciones.js'
import { useToast } from '../components/Toast.jsx'
import Loader from '../components/Loader.jsx'

export default function MisReservaciones() {
  const [reservas, setReservas] = useState(null)
  const [cargando, setCargando] = useState(false)
  const toast = useToast()

  const cargar = () => {
    setCargando(true)
    getMisReservaciones()
      .then(setReservas)
      .catch((e) => toast.error(e.message || 'Error al cargar'))
      .finally(() => setCargando(false))
  }

  useEffect(() => { cargar() }, [])

  async function accion(fn, id, msgExito) {
    try {
      await fn(id)
      toast.exito(msgExito)
      cargar()
    } catch (e) {
      toast.error(e.message || 'Error')
    }
  }

  if (!reservas) return <Loader />

  return (
    <div className="panel-section">
      <div className="panel-header-greeting">
        <h2>Mis reservas</h2>
        <Link to="/eventos" className="btn btn-ghost">← Catálogo</Link>
      </div>

      {reservas.length === 0 ? (
        <div className="estado-vacio">
          <p>No tienes reservas aún.</p>
          <Link to="/eventos" className="btn btn-primary">Ver eventos</Link>
        </div>
      ) : (
        <div className="panel-lista">
          {reservas.map((r) => (
            <div key={r.idReservacion} className="reserva-item">
              <div className="reserva-info">
                <h3>{r.eventoNombre}</h3>
                <p className="reserva-meta">
                  📅 {r.eventoFecha} · ⏰ {r.eventoHoraInicio}
                </p>
                <p className="reserva-codigo">Código: <strong>{r.codigoReservacion}</strong></p>
              </div>
              <div className="reserva-estado">
                <span className={`badge estado-${(r.estado || '').toLowerCase()}`}>{r.estado}</span>
              </div>
              <div className="reserva-acciones">
                <Link to={`/comprobante/${r.codigoReservacion}`} className="btn btn-ghost btn-sm">Ver comprobante</Link>
                {r.estado === 'Confirmada' && (
                  <>
                    <button className="btn btn-ghost btn-sm" disabled={cargando}
                      onClick={() => accion(pausarReservacion, r.idReservacion, 'Reserva pausada')}>⏸ Pausar</button>
                    <button className="btn btn-danger btn-sm" disabled={cargando}
                      onClick={() => { if (confirm('¿Cancelar esta reserva?')) accion(cancelarReservacion, r.idReservacion, 'Reserva cancelada') }}>✕ Cancelar</button>
                  </>
                )}
                {r.estado === 'Pausada' && (
                  <>
                    <button className="btn btn-primary btn-sm" disabled={cargando}
                      onClick={() => accion(confirmarReservacion, r.idReservacion, 'Reserva confirmada')}>✓ Confirmar</button>
                    <button className="btn btn-danger btn-sm" disabled={cargando}
                      onClick={() => { if (confirm('¿Cancelar esta reserva?')) accion(cancelarReservacion, r.idReservacion, 'Reserva cancelada') }}>✕ Cancelar</button>
                  </>
                )}
                {r.estado === 'Pendiente' && (
                  <>
                    <button className="btn btn-primary btn-sm" disabled={cargando}
                      onClick={() => accion(confirmarReservacion, r.idReservacion, 'Reserva confirmada')}>✓ Confirmar</button>
                    <button className="btn btn-danger btn-sm" disabled={cargando}
                      onClick={() => { if (confirm('¿Cancelar esta reserva?')) accion(cancelarReservacion, r.idReservacion, 'Reserva cancelada') }}>✕ Cancelar</button>
                  </>
                )}
                {r.estado === 'Cancelada' && (
                  <span className="reserva-cancelada-nota">Reserva cancelada</span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
