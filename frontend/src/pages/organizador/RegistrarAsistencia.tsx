import { useState, type ChangeEvent } from 'react';
import { asistenciaService } from '../../api/asistencias';
import type { IAsistencia } from '../../interfaces/api-interface/asistencia.interface';
import { useToast } from '../../context/ToastContext';
import type { EventoDetalle } from '../../interfaces/api-interface/eventos.interface';

export default function RegistrarAsistencia() {
  const toast = useToast();
  const [codigo, setCodigo] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [resultado, setResultado] = useState<IAsistencia | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [evento] = useState<EventoDetalle | null>(null);

  async function handleSubmit(e: ChangeEvent<HTMLFormElement>) {
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
      const data = await asistenciaService.registrarAsistencia(cod)
      setResultado(data)
      toast.exito('Asistencia registrada correctamente.')
      setCodigo('')
    } catch (err: any) {
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
          {evento?.nombre && <p><strong>Evento:</strong> {evento.nombre}</p>}
          {evento?.fecha && <p><strong>Fecha:</strong> {evento.fecha.toString()}</p>}
        </div>
      )}
    </div>
  )
}
