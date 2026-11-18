import { useEffect, useState } from 'react'
import { eventoService } from '../../api/eventos'
import { Loader } from '../../components/Loader'
import { useToast } from '../../context/ToastContext'
import type { EventoDetalle } from '../../interfaces/api-interface/eventos.interface'
import { EventSimpleList } from '../../components/ui/EvenSimpleList/EventSimpleList'


export default function Catalogo() {
  const toast = useToast();
  const [eventos, setEventos] = useState<EventoDetalle[] | null>([]);
  const [cargando, setCargando] = useState(true);
  const [filtro, setFiltro] = useState('');

  useEffect(() => {
    let alive = true
    setCargando(true)
    eventoService.getPublicCatalogo()
      .then((data) => {
        if (!alive) return
        setEventos(data)
      })
      .catch((err) => {
        if (!alive) return
        toast.error(err.message || 'No se pudieron cargar los eventos.')
      })
      .finally(() => alive && setCargando(false))
    return () => { alive = false }
  }, [])

  const filtrados = eventos!.filter((e) => {
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
        <EventSimpleList eventList={eventos!}/>
      )}
    </div>
  )
}
