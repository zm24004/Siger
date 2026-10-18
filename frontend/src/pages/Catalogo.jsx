import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getCatalogo } from '../api/eventos.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useToast } from '../components/Toast.jsx'
import Loader from '../components/Loader.jsx'

function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' })
}

export default function Catalogo() {
  const { isAuthenticated, rol } = useAuth()
  const toast = useToast()
  const [eventos, setEventos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [filtro, setFiltro] = useState('')

  useEffect(() => {
    let alive = true
    setCargando(true)
    getCatalogo()
      .then((data) => {
        if (!alive) return
        setEventos(Array.isArray(data) ? data : [])
      })
      .catch((err) => {
        if (!alive) return
        toast.error(err.message || 'No se pudieron cargar los eventos.')
      })
      .finally(() => alive && setCargando(false))
    return () => { alive = false }
  }, [toast])

  const filtrados = eventos.filter((e) => {
    const q = filtro.trim().toLowerCase()
    if (!q) return true
    return (
      (e.nombre || '').toLowerCase().includes(q) ||
      (e.ubicacionNombre || '').toLowerCase().includes(q)
    )
  })

  return (
    <div className="catalogo-wrapper">
      <header className="catalogo-header">
        <div>
          <h1 className="catalogo-titulo">Eventos disponibles</h1>
          <p className="catalogo-subtitulo">Reserva tu lugar en los próximos eventos del campus.</p>
        </div>
        <div className="catalogo-buscador">
          <label htmlFor="filtro" className="sr-only">Buscar eventos</label>
          <input
            id="filtro"
            type="search"
            className="form-control"
            placeholder="Buscar por nombre o lugar…"
            value={filtro}
            onChange={(e) => setFiltro(e.target.value)}
          />
        </div>
      </header>

      {cargando ? (
        <Loader label="Cargando eventos…" />
      ) : filtrados.length === 0 ? (
        <div className="estado-vacio">
          <h2>No hay eventos para mostrar</h2>
          <p>{filtro ? 'Prueba con otros términos de búsqueda.' : 'Vuelve más tarde, los organizadores publicarán eventos pronto.'}</p>
        </div>
      ) : (
        <ul className="catalogo-grid" aria-label="Listado de eventos">
          {filtrados.map((ev) => {
            const cupos = Number(ev.cuposDisponibles ?? 0)
            const sinCupos = cupos <= 0
            const cancelado = (ev.estado || '').toUpperCase() === 'CANCELADO'
            return (
              <li key={ev.idEvento} className="evento-card">
                <Link to={`/evento/${ev.idEvento}`} className="evento-card-link" aria-label={`Ver detalle del evento ${ev.nombre}`}>
                  <div className="evento-card-cuerpo">
                    <span className={`evento-estado evento-estado-${(ev.estado || 'ACTIVO').toLowerCase()}`}>
                      {ev.estado || 'ACTIVO'}
                    </span>
                    <h3 className="evento-titulo">{ev.nombre}</h3>
                    <p className="evento-meta"><span>📅</span>{formatDate(ev.fecha)} · {ev.horaInicio}</p>
                    <p className="evento-meta"><span>📍</span>{ev.ubicacionNombre || 'Por confirmar'}</p>
                    <p className="evento-meta"><span>👤</span>Organiza: {ev.organizadorNombre || '—'}</p>
                  </div>
                  <div className="evento-acciones">
                    <span className={`evento-cupos ${sinCupos ? 'agotado' : ''}`} aria-label={`Cupos disponibles: ${cupos}`}>
                      {sinCupos ? 'Agotado' : `${cupos} cupos`}
                    </span>
                    {isAuthenticated && rol === 'PARTICIPANTE' && !cancelado && !sinCupos && (
                      <span className="btn btn-primary btn-sm" aria-hidden="true">Reservar →</span>
                    )}
                  </div>
                </Link>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}
