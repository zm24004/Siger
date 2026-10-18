import { useEffect, useState } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { getEvento } from '../api/eventos.js'
import { reservar } from '../api/reservaciones.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useToast } from '../components/Toast.jsx'
import Loader from '../components/Loader.jsx'
import Modal from '../components/Modal.jsx'

function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleDateString('es-ES', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' })
}

export default function EventoDetalle() {
  const { id } = useParams()
  const { isAuthenticated, rol } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()

  const [evento, setEvento] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [reservando, setReservando] = useState(false)
  const [codigoReserva, setCodigoReserva] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)

  useEffect(() => {
    let alive = true
    setCargando(true)
    getEvento(id)
      .then((data) => alive && setEvento(data))
      .catch((err) => {
        if (!alive) return
        toast.error(err.message || 'No se pudo cargar el evento.')
      })
      .finally(() => alive && setCargando(false))
    return () => { alive = false }
  }, [id, toast])

  async function handleReservar() {
    if (!isAuthenticated) {
      toast.info('Inicia sesión para reservar.')
      navigate('/login', { state: { from: `/evento/${id}` } })
      return
    }
    if (rol !== 'PARTICIPANTE') {
      toast.info('Sólo los participantes pueden reservar.')
      return
    }
    setReservando(true)
    try {
      const data = await reservar(id)
      setCodigoReserva(data.codigoReservacion)
      setModalOpen(true)
      toast.exito('Reserva confirmada. Revisa tu comprobante.')
      // Refresca cupos
      getEvento(id).then(setEvento).catch(() => {})
    } catch (err) {
      if (err.status === 409) {
        toast.error('Ya tienes una reserva activa para este evento.')
      } else if (err.status === 400) {
        toast.error(err.message || 'No se pudo reservar: el evento está lleno o cancelado.')
      } else {
        toast.error(err.message || 'No se pudo completar la reserva.')
      }
    } finally {
      setReservando(false)
    }
  }

  if (cargando) {
    return (
      <div className="centrado">
        <Loader label="Cargando evento…" />
      </div>
    )
  }

  if (!evento) {
    return (
      <div className="estado-vacio">
        <h1>Evento no encontrado</h1>
        <p>El evento solicitado no existe o fue cancelado.</p>
        <Link to="/eventos" className="btn btn-primary">Volver al catálogo</Link>
      </div>
    )
  }

  const cupos = Number(evento.cuposDisponibles ?? 0)
  const cancelado = (evento.estado || '').toUpperCase() === 'CANCELADO'

  return (
    <article className="evento-detalle">
      <nav className="breadcrumb" aria-label="Migas de pan">
        <Link to="/eventos">← Volver al catálogo</Link>
      </nav>

      <header className="evento-detalle-header">
        <span className={`evento-estado evento-estado-${(evento.estado || 'ACTIVO').toLowerCase()}`}>
          {evento.estado || 'ACTIVO'}
        </span>
        <h1 className="evento-titulo evento-titulo-lg">{evento.nombre}</h1>
        <p className="evento-meta-lg">
          {formatDate(evento.fecha)} · {evento.horaInicio} – {evento.horaFin}
        </p>
        <p className="evento-meta-lg">📍 {evento.ubicacionNombre || 'Por confirmar'}</p>
        <p className="evento-meta-lg">👤 Organiza: {evento.organizadorNombre || '—'}</p>
      </header>

      <section className="evento-detalle-cuerpo">
        <h2 className="section-titulo">Descripción</h2>
        <p className="evento-descripcion">{evento.descripcion || 'Sin descripción.'}</p>

        <div className="evento-resumen">
          <div className="resumen-item">
            <span className="resumen-label">Capacidad máxima</span>
            <span className="resumen-valor">{evento.capacidadMaxima}</span>
          </div>
          <div className="resumen-item">
            <span className="resumen-label">Cupos disponibles</span>
            <span className={`resumen-valor ${cupos <= 0 ? 'texto-secundario' : ''}`}>
              {cupos <= 0 ? 'Agotado' : cupos}
            </span>
          </div>
        </div>
      </section>

      <section className="evento-detalle-acciones">
        {!isAuthenticated ? (
          <>
            <Link to="/login" className="btn btn-primary">Inicia sesión para reservar</Link>
            <Link to="/registro" className="btn btn-ghost">Crear cuenta</Link>
          </>
        ) : rol === 'PARTICIPANTE' ? (
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleReservar}
            disabled={reservando || cancelado || cupos <= 0}
            aria-label="Reservar mi lugar en este evento"
          >
            {reservando ? 'Reservando…' : cancelado ? 'Evento cancelado' : cupos <= 0 ? 'Sin cupos' : 'Reservar mi lugar'}
          </button>
        ) : (
          <p className="texto-secundario">Los organizadores no pueden reservar como participantes.</p>
        )}
      </section>

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        titulo="¡Reserva confirmada!"
        ariaLabel="Comprobante de reserva"
        footer={
          <>
            <button type="button" className="btn btn-ghost" onClick={() => setModalOpen(false)}>Cerrar</button>
            <Link
              to={`/comprobante/${codigoReserva}`}
              className="btn btn-primary"
              onClick={() => setModalOpen(false)}
            >
              Ver comprobante
            </Link>
          </>
        }
      >
        <p>Tu código de reserva es:</p>
        <p className="comprobante-codigo comprobante-codigo-lg">{codigoReserva}</p>
        <p className="texto-secundario">Guárdalo para registrar tu asistencia el día del evento.</p>
      </Modal>
    </article>
  )
}
