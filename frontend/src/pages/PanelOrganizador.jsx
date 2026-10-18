import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMisEventos, cancelarEvento } from '../api/eventos.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useNotificaciones } from '../context/NotificacionContext.jsx'
import { useToast } from '../components/Toast.jsx'
import Loader from '../components/Loader.jsx'
import Modal from '../components/Modal.jsx'

function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' })
}

export default function PanelOrganizador() {
  const { usuario } = useAuth()
  const { conectado } = useNotificaciones()
  const toast = useToast()
  const [eventos, setEventos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [cancelandoId, setCancelandoId] = useState(null)
  const [confirmOpen, setConfirmOpen] = useState(false)

  useEffect(() => {
    let alive = true
    setCargando(true)
    getMisEventos()
      .then((data) => {
        if (!alive) return
        setEventos(Array.isArray(data) ? data : [])
      })
      .catch((err) => {
        if (!alive) return
        toast.error(err.message || 'No se pudieron cargar tus eventos.')
      })
      .finally(() => alive && setCargando(false))
    return () => { alive = false }
  }, [toast])

  const stats = useMemo(() => {
    const totalEventos = eventos.length
    // "Reservas totales" = suma de (capacidad - cupos disponibles) en mis eventos.
    const reservasTotales = eventos.reduce(
      (acc, e) => acc + Math.max(0, (Number(e.capacidadMaxima) || 0) - (Number(e.cuposDisponibles) || 0)),
      0
    )
    const eventosActivos = eventos.filter((e) => {
      const est = (e.estado || '').toUpperCase()
      return est !== 'CANCELADO'
    }).length
    return { totalEventos, reservasTotales, eventosActivos }
  }, [eventos])

  async function confirmarCancelacion() {
    if (!cancelandoId) return
    try {
      await cancelarEvento(cancelandoId)
      toast.exito('Evento cancelado correctamente.')
      setEventos((list) =>
        list.map((e) => (e.idEvento === cancelandoId ? { ...e, estado: 'CANCELADO' } : e))
      )
    } catch (err) {
      toast.error(err.message || 'No se pudo cancelar el evento.')
    } finally {
      setConfirmOpen(false)
      setCancelandoId(null)
    }
  }

  const nombreCompleto = [usuario?.nombre, usuario?.apellido].filter(Boolean).join(' ')

  return (
    <div className="panel-wrapper">
      <header className="panel-header">
        <div className="panel-header-greeting">
          <h1>Hola, {nombreCompleto || 'organizador'} 🎯</h1>
          <div className="panel-header-meta">
            <span className="badge badge-rol badge-rol-organizador">ORGANIZADOR</span>
            <span
              className={`panel-conn-dot ${conectado ? 'on' : 'off'}`}
              title={conectado ? 'Notificaciones conectadas' : 'Sin conexión en tiempo real'}
              aria-label={conectado ? 'Notificaciones conectadas' : 'Sin conexión en tiempo real'}
            ></span>
            <span className="texto-secundario panel-conn-text">
              {conectado ? 'En línea' : 'Reconectando…'}
            </span>
          </div>
        </div>
        <div className="panel-actions">
          <Link to="/organizador/nuevo-evento" className="btn btn-primary btn-sm">+ Crear evento</Link>
          <Link to="/organizador/asistencia" className="btn btn-ghost btn-sm">Registrar asistencia</Link>
        </div>
      </header>

      <section className="panel-stats" aria-label="Resumen">
        <div className="stat-card">
          <span className="stat-label">Mis eventos</span>
          <span className="stat-valor">{stats.totalEventos}</span>
          <span className="texto-secundario stat-sub">Total publicados</span>
        </div>
        <div className="stat-card stat-card-exito">
          <span className="stat-label">Reservas totales</span>
          <span className="stat-valor">{stats.reservasTotales}</span>
          <span className="texto-secundario stat-sub">En todos tus eventos</span>
        </div>
        <div className="stat-card stat-card-rol">
          <span className="stat-label">Eventos activos</span>
          <span className="stat-valor">{stats.eventosActivos}</span>
          <span className="texto-secundario stat-sub">No cancelados</span>
        </div>
      </section>

      <section className="panel-section" aria-label="Mis eventos">
        <div className="panel-section-head">
          <h2>Mis eventos</h2>
          <Link to="/organizador/nuevo-evento" className="link-primario">+ Nuevo</Link>
        </div>

        {cargando ? (
          <Loader label="Cargando tus eventos…" />
        ) : eventos.length === 0 ? (
          <div className="estado-vacio estado-vacio-sm">
            <h3>No tienes eventos creados</h3>
            <p>Crea tu primer evento y empieza a recibir reservas.</p>
            <Link to="/organizador/nuevo-evento" className="btn btn-primary btn-sm">Crear evento</Link>
          </div>
        ) : (
          <div className="tabla-wrapper" role="region" aria-label="Tabla de eventos" tabIndex={0}>
            <table className="tabla">
              <thead>
                <tr>
                  <th scope="col">Evento</th>
                  <th scope="col">Fecha</th>
                  <th scope="col">Hora</th>
                  <th scope="col">Lugar</th>
                  <th scope="col">Cupos</th>
                  <th scope="col">Estado</th>
                  <th scope="col">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {eventos.map((ev) => {
                  const cancelado = (ev.estado || '').toUpperCase() === 'CANCELADO'
                  const reservados = Math.max(
                    0,
                    (Number(ev.capacidadMaxima) || 0) - (Number(ev.cuposDisponibles) || 0)
                  )
                  return (
                    <tr key={ev.idEvento}>
                      <td>
                        <Link to={`/evento/${ev.idEvento}`} className="link-primario">{ev.nombre}</Link>
                      </td>
                      <td>{formatDate(ev.fecha)}</td>
                      <td>{ev.horaInicio} – {ev.horaFin}</td>
                      <td>{ev.ubicacionNombre || '—'}</td>
                      <td>
                        <span className="tabla-cupos">
                          <strong>{reservados}</strong> / {ev.capacidadMaxima ?? '—'}
                        </span>
                        <span className="texto-secundario"> ({ev.cuposDisponibles ?? '—'} libres)</span>
                      </td>
                      <td>
                        <span className={`badge badge-${(ev.estado || 'activo').toLowerCase()}`}>
                          {ev.estado || 'ACTIVO'}
                        </span>
                      </td>
                      <td>
                        <div className="panel-acciones-fila">
                          <Link to={`/evento/${ev.idEvento}`} className="btn btn-ghost btn-sm">Ver</Link>
                          <button
                            type="button"
                            className="btn btn-danger btn-sm"
                            disabled={cancelado}
                            onClick={() => {
                              setCancelandoId(ev.idEvento)
                              setConfirmOpen(true)
                            }}
                            aria-label={`Cancelar el evento ${ev.nombre}`}
                          >
                            Cancelar
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* Mensaje informativo sobre el dato de reservas */}
      <p className="texto-secundario panel-nota">
        ℹ️ Las <strong>reservas totales</strong> se calculan a partir de la diferencia entre
        capacidad máxima y cupos disponibles en cada uno de tus eventos.
      </p>

      <Modal
        open={confirmOpen}
        onClose={() => { setConfirmOpen(false); setCancelandoId(null) }}
        titulo="Cancelar evento"
        ariaLabel="Confirmar cancelación de evento"
        footer={
          <>
            <button type="button" className="btn btn-ghost" onClick={() => { setConfirmOpen(false); setCancelandoId(null) }}>
              Volver
            </button>
            <button type="button" className="btn btn-danger" onClick={confirmarCancelacion}>
              Sí, cancelar
            </button>
          </>
        }
      >
        <p>¿Seguro que deseas cancelar este evento? Esta acción <strong>no se puede deshacer</strong> y notificará a los participantes con reserva.</p>
      </Modal>
    </div>
  )
}
