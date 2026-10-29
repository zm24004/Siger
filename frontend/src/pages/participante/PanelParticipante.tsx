import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { eventoService } from '../../api/eventos';
import { reservacionService } from '../../api/reservaciones';
import { notificacionService } from '../../api/notificaciones';
import { useAuth } from '../../context/AuthContext';
import { useNotificaciones } from '../../context/NotificacionContext';
import { Loader } from '../../components/Loader';
import { useToast } from '../../context/ToastContext';
import type { EventoDetalle } from '../../interfaces/api-interface/eventos.interface';
import type { Notificacion } from '../../interfaces/api-interface/notificaciones.interface';
import type { Reservacion } from '../../interfaces/api-interface/reservacion.interface';
import type { Paginado } from '../../interfaces/common';

function formatDate(value: string) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' })
}

function formatDateTime(value: string) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleString('es-ES', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export default function PanelParticipante() {
  const { usuario } = useAuth()
  const { noLeidas, conectado } = useNotificaciones()
  const toast = useToast()

  const [catalogo, setCatalogo] = useState<EventoDetalle[] | null>(null)
  const [reservas, setReservas] = useState<Reservacion[] | null>(null)
  const [notifs, setNotifs] = useState<Notificacion[] | null>(null)
  const [cargando, setCargando] = useState(true)

  const cargarNotifs = useCallback(() => {
    notificacionService.getMis()
      .then((data) => setNotifs(Array.isArray(data) ? data : []))
      .catch((err) => toast.error(err.message || 'No se pudieron cargar las notificaciones.'))
  }, [toast])

  useEffect(() => {
    let alive = true
    setCargando(true)
    Promise.all([
      eventoService.getPublicCatalogo().catch(() => []),
      reservacionService.getMisReservaciones().catch(() => []),
      notificacionService.getMis().catch(() => [])
    ])
      .then(([cats, res, notis]) => {
        if (!alive) return
        // const eventosContent = (cats as Paginado<EventoDetalle>).content || [];
        setCatalogo(cats as EventoDetalle[] )
        setReservas(Array.isArray(res) ? res : [])
        setNotifs(Array.isArray(notis) ? notis : [])
      })
      .finally(() => alive && setCargando(false))
    return () => { alive = false }
  }, [])

  const proximasReservas = reservas?.filter((r) => {
      const est = (r.estado || '').toUpperCase()
      return est === 'CONFIRMADA' || est === 'PENDIENTE'
    })?.slice(0, 5)

  const notifsRecientes = notifs?.slice(0, 5)

  async function handleMarcarLeida(id: number) {
    try {
      await notificacionService.marcarLeida(id)
      setNotifs((list) =>
        list!.map((n) => (n.idNotificacion === id ? { ...n, leido: true } : n))
      )
      toast.exito('Notificación marcada como leída.')
    } catch (err: any) {
      toast.error(err.message || 'No se pudo actualizar la notificación.')
    }
  }

  if (cargando) {
    return (
      <div className="centrado">
        <Loader label="Cargando tu panel…" />
      </div>
    )
  }

  const nombreCompleto = [usuario?.nombre, usuario?.apellido].filter(Boolean).join(' ')

  return (
    <div className="panel-wrapper">
      <header className="panel-header">
        <div className="panel-header-greeting">
          <h1>Hola, {nombreCompleto || 'participante'} 👋</h1>
          <div className="panel-header-meta">
            <span className="badge badge-rol badge-rol-participante">PARTICIPANTE</span>
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
        <Link to="/eventos" className="btn btn-primary btn-sm">Ver catálogo</Link>
      </header>

      <section className="panel-stats" aria-label="Resumen">
        <div className="stat-card">
          <span className="stat-label">Próximos eventos</span>
          <span className="stat-valor">{catalogo?.length}</span>
          <Link to="/eventos" className="stat-link">Explorar →</Link>
        </div>
        <div className="stat-card stat-card-exito">
          <span className="stat-label">Mis reservas</span>
          <span className="stat-valor">{reservas?.length}</span>
          <Link to="/mis-reservaciones" className="stat-link">Ver todas →</Link>
        </div>
        <div className="stat-card stat-card-peligro">
          <span className="stat-label">Notificaciones sin leer</span>
          <span className="stat-valor">{noLeidas}</span>
          <span className="texto-secundario stat-sub">En tiempo real</span>
        </div>
      </section>

      <section className="panel-section" aria-label="Mis próximas reservas">
        <div className="panel-section-head">
          <h2>Mis próximas reservas</h2>
          <Link to="/mis-reservaciones" className="link-primario">Ver todas</Link>
        </div>
        {proximasReservas?.length === 0 ? (
          <div className="estado-vacio estado-vacio-sm">
            <h3>Aún no tienes reservas activas</h3>
            <p>Cuando reserves un evento aparecerá aquí con su código y fecha.</p>
            <Link to="/eventos" className="btn btn-primary btn-sm">Ver eventos disponibles</Link>
          </div>
        ) : (
          <ul className="panel-lista">
            {proximasReservas?.map((r) => (
              <li key={r.idReservacion} className="panel-lista-item">
                <div className="panel-lista-main">
                  <span className="panel-lista-titulo">{r.eventoNombre || 'Evento eliminado'}</span>
                  <span className="panel-lista-meta">
                    📅 {formatDate(r.eventoFecha.toString())} · 🕐 {r.eventoHoraInicio || '—'}
                  </span>
                </div>
                <code className="codigo-chip">{r.codigoReservacion}</code>
                <span className={`badge badge-${(r.estado || 'pendiente').toLowerCase()}`}>
                  {r.estado || 'PENDIENTE'}
                </span>
                <Link
                  to={`/comprobante/${r.codigoReservacion}`}
                  className="btn btn-ghost btn-sm"
                >
                  Comprobante
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="panel-section" aria-label="Notificaciones recientes">
        <div className="panel-section-head">
          <h2>Notificaciones recientes</h2>
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            onClick={cargarNotifs}
            aria-label="Recargar notificaciones"
          >
            Refrescar
          </button>
        </div>
        {notifsRecientes?.length === 0 ? (
          <div className="estado-vacio estado-vacio-sm">
            <h3>Sin notificaciones</h3>
            <p>Te avisaremos por aquí cuando confirms una reservación o un evento cambie.</p>
          </div>
        ) : (
          <ul className="panel-lista">
            {notifsRecientes?.map((n) => {
              const leida = n.leido === true
              return (
                <li
                  key={n.idNotificacion}
                  className={`panel-lista-item panel-lista-item-notif ${leida ? 'leida' : 'no-leida'}`}
                >
                  <div className="panel-lista-main">
                    <span className="panel-lista-titulo">{n.mensaje}</span>
                    <span className="panel-lista-meta">{formatDateTime(n.fechaEnvio.toString())}</span>
                  </div>
                  <span className={`badge badge-${(n.tipo || 'info').toLowerCase()}`}>
                    {n.tipo || 'INFO'}
                  </span>
                  {!leida && (
                    <button
                      type="button"
                      className="btn btn-ghost btn-sm"
                      onClick={() => handleMarcarLeida(n.idNotificacion)}
                      aria-label={`Marcar notificación como leída: ${n.mensaje}`}
                    >
                      Marcar leída
                    </button>
                  )}
                </li>
              )
            })}
          </ul>
        )}
      </section>
    </div>
  )
}
