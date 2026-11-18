import { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import QRCode from 'qrcode';
import { reservacionService } from '../../api/reservaciones';
import { Loader } from '../../components/Loader';
import type { Reservacion } from '../../interfaces/api-interface/reservacion.interface';
import type { EventoDetalle } from '../../interfaces/api-interface/eventos.interface';
import { eventoService } from '../../api/eventos';

export default function Comprobante() {
  const { codigo } = useParams();
  const navigate = useNavigate();
  const canvasRef = useRef(null);
  const [reservacion, setReservacion] = useState<Reservacion | null>(null);
  const [error, setError] = useState(null);
  const [evento, setEvento] = useState<EventoDetalle | null>(null);

  useEffect(() => {
    reservacionService.getReservacion(codigo as string)
      .then(setReservacion)
      .catch((err) => setError(err.message || 'No se pudo cargar el comprobante'))

      if (reservacion?.idEvento) {
        eventoService.getEvento(reservacion.idEvento)
          .then(setEvento)
          .catch((err) => setError(err.message || 'No se pudo cargar el evento'))
      }
  }, [codigo])

  useEffect(() => {
    if (reservacion?.codigoReservacion && canvasRef.current) {
      QRCode.toCanvas(canvasRef.current, reservacion.codigoReservacion, {
        width: 220,
        margin: 2,
        color: { dark: '#1f2937', light: '#ffffff' },
      }, (err) => { if (err) console.error('QR error', err) })
    }
  }, [reservacion])

  if (error) {
    return (
      <div className="comprobante-wrapper">
        <div className="comprobante-card">
          <p className="comprobante-error">❌ {error}</p>
          <button className="btn btn-ghost" onClick={() => navigate('/mis-reservaciones')}>← Volver</button>
        </div>
      </div>
    )
  }
  if (!reservacion) return <Loader />

  return (
    <div className="comprobante-wrapper">
      <div className="comprobante-card">
        <div className="comprobante-header">
          <span className="comprobante-logo">S</span>
          <div>
            <h1>SIGER</h1>
            <p>Sistema de Gestión de Reservas</p>
          </div>
          <span className={`comprobante-estado estado-${(reservacion.estado || '').toLowerCase()}`}>
            {reservacion.estado}
          </span>
        </div>

        <div className="comprobante-body">
          <p className="comprobante-emitido">Comprobante de Reserva</p>

          <div className="comprobante-codigo">
            <span className="label">CÓDIGO DE RESERVA</span>
            <strong>{reservacion.codigoReservacion}</strong>
          </div>

          <div className="comprobante-datos">
            <p><strong>EVENTO:</strong> {evento?.nombre || 'Evento no disponible'}</p>
            <p><strong>FECHA:</strong> {evento?.fecha.toString() || 'Fecha no disponible'}</p>
            <p><strong>HORA DE INICIO:</strong> {reservacion.eventoHoraInicio}</p>
          </div>

          <div className="comprobante-qr">
            <canvas ref={canvasRef}></canvas>
            <p className="comprobante-qr-texto">Presenta este código en el evento</p>
          </div>

          <p className="comprobante-nota">
            Conserva este comprobante. Deberás presentarlo (físico o digital) el día del evento para registrar tu asistencia.
          </p>
        </div>

        <div className="comprobante-footer">
          <p>© 2026 SIGER — Universidad</p>
        </div>
      </div>

      <button className="btn btn-primary comprobante-print" onClick={() => window.print()}>
        🖨 Imprimir
      </button>
    </div>
  )
}
