import { useState } from 'react'
import { registrarAsistencia } from '../api/asistencias.js'
import { useToast } from '../components/Toast.jsx'

export default function RegistrarAsistencia() {
  const toast = useToast()
  const [codigo, setCodigo] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [resultado, setResultado] = useState(null)
  const [error, setError] = useState(null)

  async function handleSubmit(e) {
    e.preventDefault()
    const cod = codigo.trim()
    if (!cod) {
      setError('Ingresa el código de reserva.')
      return
    }
    setError(null)
    setResultado(null)
    setEnviando(true)
    try {
      const data = await registrarAsistencia(cod)
      setResultado(data)
      toast.exito('Asistencia registrada correctamente.')
      setCodigo('')
    } catch (err) {
      if (err.status === 404) {
        setError('Código de reserva no encontrado.')
        toast.error('Código de reserva no encontrado.')
      } else if (err.status === 409) {
        setError('La asistencia ya fue registrada o la reserva no está confirmada.')
        toast.error(err.message || 'No se puede registrar la asistencia.')
      } else {
        setError(err.message || 'No se pudo registrar la asistencia.')
        toast.error(err.message || 'No se pudo registrar la asistencia.')
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="panel-wrapper">
      <header className="panel-header">
        <h1>Registrar asistencia</h1>
      </header>

      <form className="auth-card form-asistencia" onSubmit={handleSubmit} noValidate>
        <div className="form-group">
          <label htmlFor="codigoReservacion">Código de reserva</label>
          <input
            id="codigoReservacion"
            name="codigoReservacion"
            type="text"
            className={`form-control ${error ? 'is-invalid' : ''}`}
            value={codigo}
            onChange={(e) => {
              setCodigo(e.target.value)
              setError(null)
              setResultado(null)
            }}
            placeholder="Ej: SIG-AB12CD34"
            autoComplete="off"
            aria-invalid={!!error}
            aria-describedby={error ? 'err-codigo' : undefined}
          />
          {error && <p id="err-codigo" className="form-error">{error}</p>}
        </div>

        <button type="submit" className="btn btn-primary btn-block" disabled={enviando}>
          {enviando ? 'Registrando…' : 'Registrar asistencia'}
        </button>
      </form>

      {resultado && (
        <div className="resultado-exito" role="status">
          <h2>Asistencia confirmada</h2>
          <p><strong>Reserva:</strong> <code>{resultado.codigoReservacion || codigo}</code></p>
          {resultado.eventoNombre && <p><strong>Evento:</strong> {resultado.eventoNombre}</p>}
          {resultado.eventoFecha && <p><strong>Fecha:</strong> {resultado.eventoFecha}</p>}
        </div>
      )}
    </div>
  )
}
