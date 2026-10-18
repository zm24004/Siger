import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { crearEvento } from '../api/eventos.js'
import { getUbicaciones } from '../api/ubicaciones.js'
import { useToast } from '../components/Toast.jsx'
import Loader from '../components/Loader.jsx'

const hoyStr = () => new Date().toISOString().slice(0, 10)

export default function FormularioEvento() {
  const { id } = useParams() // se reutiliza para edición futura (no implementada)
  const navigate = useNavigate()
  const toast = useToast()
  const esEdicion = Boolean(id)

  const [ubicaciones, setUbicaciones] = useState([])
  const [cargandoUbic, setCargandoUbic] = useState(true)
  const [enviando, setEnviando] = useState(false)

  const [form, setForm] = useState({
    nombre: '',
    descripcion: '',
    fecha: hoyStr(),
    horaInicio: '09:00',
    horaFin: '10:00',
    capacidadMaxima: 50,
    idUbicacion: ''
  })
  const [errores, setErrores] = useState({})

  useEffect(() => {
    let alive = true
    setCargandoUbic(true)
    getUbicaciones()
      .then((data) => {
        if (!alive) return
        const list = Array.isArray(data) ? data : []
        setUbicaciones(list)
        if (list.length > 0 && !form.idUbicacion) {
          setForm((f) => ({ ...f, idUbicacion: String(list[0].idUbicacion ?? list[0].id ?? '') }))
        }
      })
      .catch((err) => alive && toast.error(err.message || 'No se pudieron cargar las ubicaciones.'))
      .finally(() => alive && setCargandoUbic(false))
    return () => { alive = false }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function handleChange(e) {
    const { name, value, type } = e.target
    const val = type === 'number' ? (value === '' ? '' : Number(value)) : value
    setForm((f) => ({ ...f, [name]: val }))
    setErrores((er) => ({ ...er, [name]: undefined }))
  }

  function validar() {
    const e = {}
    if (!form.nombre.trim()) e.nombre = 'El nombre es obligatorio.'
    else if (form.nombre.trim().length < 5) e.nombre = 'Mínimo 5 caracteres.'
    if (form.descripcion && form.descripcion.length > 1000) e.descripcion = 'Máximo 1000 caracteres.'
    if (!form.fecha) e.fecha = 'La fecha es obligatoria.'
    else {
      const f = new Date(`${form.fecha}T00:00:00`)
      const hoy = new Date(`${hoyStr()}T00:00:00`)
      if (f < hoy && !esEdicion) e.fecha = 'La fecha no puede ser pasada.'
    }
    if (!form.horaInicio) e.horaInicio = 'Hora de inicio obligatoria.'
    if (!form.horaFin) e.horaFin = 'Hora de fin obligatoria.'
    if (form.horaInicio && form.horaFin && form.horaFin <= form.horaInicio) {
      e.horaFin = 'La hora de fin debe ser posterior a la de inicio.'
    }
    if (!form.capacidadMaxima || Number(form.capacidadMaxima) <= 0) {
      e.capacidadMaxima = 'Capacidad debe ser mayor a 0.'
    } else if (Number(form.capacidadMaxima) > 100000) {
      e.capacidadMaxima = 'Capacidad poco realista.'
    }
    if (!form.idUbicacion) e.idUbicacion = 'Selecciona una ubicación.'

    setErrores(e)
    return Object.keys(e).length === 0
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!validar()) {
      toast.error('Revisa los campos marcados.')
      return
    }
    setEnviando(true)
    const payload = {
      nombre: form.nombre.trim(),
      descripcion: form.descripcion?.trim() || '',
      fecha: form.fecha,
      horaInicio: form.horaInicio,
      horaFin: form.horaFin,
      capacidadMaxima: Number(form.capacidadMaxima),
      idUbicacion: Number(form.idUbicacion)
    }
    try {
      const creado = await crearEvento(payload)
      toast.exito('Evento creado correctamente.')
      navigate(`/evento/${creado.idEvento ?? creado.id ?? ''}`, { replace: true })
    } catch (err) {
      if (err.status === 400 && err.errors) {
        // Errores de validación del backend campo por campo
        const nuevos = {}
        Object.entries(err.errors).forEach(([k, v]) => {
          nuevos[k] = Array.isArray(v) ? v[0] : String(v)
        })
        setErrores((prev) => ({ ...prev, ...nuevos }))
        toast.error('El servidor rechazó algunos campos.')
      } else {
        toast.error(err.message || 'No se pudo crear el evento.')
      }
    } finally {
      setEnviando(false)
    }
  }

  if (cargandoUbic) {
    return (
      <div className="centrado">
        <Loader label="Cargando ubicaciones…" />
      </div>
    )
  }

  return (
    <div className="panel-wrapper">
      <header className="panel-header">
        <h1>{esEdicion ? 'Editar evento' : 'Nuevo evento'}</h1>
      </header>

      <form className="auth-card form-evento" onSubmit={handleSubmit} noValidate>
        <div className="form-group">
          <label htmlFor="nombre">Nombre del evento *</label>
          <input
            id="nombre"
            name="nombre"
            type="text"
            className={`form-control ${errores.nombre ? 'is-invalid' : ''}`}
            value={form.nombre}
            onChange={handleChange}
            maxLength={120}
            aria-invalid={!!errores.nombre}
            aria-describedby={errores.nombre ? 'err-nombre' : undefined}
          />
          {errores.nombre && <p id="err-nombre" className="form-error">{errores.nombre}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="descripcion">Descripción</label>
          <textarea
            id="descripcion"
            name="descripcion"
            className={`form-control ${errores.descripcion ? 'is-invalid' : ''}`}
            value={form.descripcion}
            onChange={handleChange}
            rows={4}
            maxLength={1000}
            aria-describedby="desc-count"
          />
          <small id="desc-count" className="form-ayuda">{form.descripcion?.length || 0}/1000</small>
          {errores.descripcion && <p className="form-error">{errores.descripcion}</p>}
        </div>

        <div className="form-grid-2">
          <div className="form-group">
            <label htmlFor="fecha">Fecha *</label>
            <input
              id="fecha"
              name="fecha"
              type="date"
              className={`form-control ${errores.fecha ? 'is-invalid' : ''}`}
              value={form.fecha}
              onChange={handleChange}
            />
            {errores.fecha && <p className="form-error">{errores.fecha}</p>}
          </div>
          <div className="form-group">
            <label htmlFor="capacidadMaxima">Capacidad máxima *</label>
            <input
              id="capacidadMaxima"
              name="capacidadMaxima"
              type="number"
              min={1}
              max={100000}
              className={`form-control ${errores.capacidadMaxima ? 'is-invalid' : ''}`}
              value={form.capacidadMaxima}
              onChange={handleChange}
            />
            {errores.capacidadMaxima && <p className="form-error">{errores.capacidadMaxima}</p>}
          </div>
        </div>

        <div className="form-grid-2">
          <div className="form-group">
            <label htmlFor="horaInicio">Hora de inicio *</label>
            <input
              id="horaInicio"
              name="horaInicio"
              type="time"
              className={`form-control ${errores.horaInicio ? 'is-invalid' : ''}`}
              value={form.horaInicio}
              onChange={handleChange}
            />
            {errores.horaInicio && <p className="form-error">{errores.horaInicio}</p>}
          </div>
          <div className="form-group">
            <label htmlFor="horaFin">Hora de fin *</label>
            <input
              id="horaFin"
              name="horaFin"
              type="time"
              className={`form-control ${errores.horaFin ? 'is-invalid' : ''}`}
              value={form.horaFin}
              onChange={handleChange}
            />
            {errores.horaFin && <p className="form-error">{errores.horaFin}</p>}
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="idUbicacion">Ubicación *</label>
          <select
            id="idUbicacion"
            name="idUbicacion"
            className={`form-control ${errores.idUbicacion ? 'is-invalid' : ''}`}
            value={form.idUbicacion}
            onChange={handleChange}
          >
            <option value="">Selecciona…</option>
            {ubicaciones.map((u) => (
              <option key={u.idUbicacion ?? u.id} value={u.idUbicacion ?? u.id}>
                {u.nombre}{u.direccion ? ` — ${u.direccion}` : ''}
              </option>
            ))}
          </select>
          {errores.idUbicacion && <p className="form-error">{errores.idUbicacion}</p>}
        </div>

        <div className="form-acciones">
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/organizador/eventos')}>
            Cancelar
          </button>
          <button type="submit" className="btn btn-primary" disabled={enviando}>
            {enviando ? 'Guardando…' : esEdicion ? 'Guardar cambios' : 'Crear evento'}
          </button>
        </div>
      </form>
    </div>
  )
}
