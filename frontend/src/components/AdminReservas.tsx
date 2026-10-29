import QRCode from "qrcode";
import { useEffect, useRef, useState } from "react";
import { reservacionService } from "../api/reservaciones";
import Modal from "./Modal";
import type { Reservacion } from "../interfaces/api-interface/reservacion.interface";
import { useToast } from "../context/ToastContext";

export default function AdminReservas() {
  const [reservas, setReservas] = useState<Reservacion[] | null>(null);
  const [qrReserva, setQrReserva] = useState<Reservacion | null>(null);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const toast = useToast();

  const cargar = () => {
    reservacionService.getTodasReservaciones()
      .then(setReservas)
      .catch((e) => toast.error(e.message || "Error al cargar reservas"));
  };

  useEffect(() => {
    cargar();
  }, []);

  useEffect(() => {
    if (qrReserva?.codigoReservacion && canvasRef.current) {
      QRCode.toCanvas(
        canvasRef.current,
        qrReserva.codigoReservacion,
        {
          width: 260,
          margin: 2,
          color: { dark: "#1f2937", light: "#ffffff" },
        },
        () => {},
      );
    }
  }, [qrReserva]);

  async function cancelar(id: number) {
    if (!confirm("¿Cancelar esta reserva como administrador?")) return;
    try {
      await reservacionService.cancelarComoAdmin(id);
      toast.exito("Reserva cancelada");
      cargar();
    } catch (e: any) {
      toast.error(e.message || "Error");
    }
  }

  if (!reservas) return <p>Cargando reservas…</p>;

  return (
    <div className="panel-section">
      <div className="panel-header-greeting">
        <h3>Gestión de reservas</h3>
        <button className="btn btn-ghost btn-sm" onClick={cargar}>
          ↻ Actualizar
        </button>
      </div>

      {reservas.length === 0 ? (
        <div className="estado-vacio-sm">
          <p>No hay reservas registradas.</p>
        </div>
      ) : (
        <div className="tabla-reservas">
          <table className="tabla">
            <thead>
              <tr>
                <th>Código</th>
                <th>Evento</th>
                <th>Participante</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {reservas.map((r) => (
                <tr key={r.idReservacion}>
                  <td>
                    <strong>{r.codigoReservacion}</strong>
                  </td>
                  <td>{r.eventoNombre}</td>
                  <td>{r.participanteNombre || "—"}</td>
                  <td>{r.eventoFecha.toString()}</td>
                  <td>
                    <span
                      className={`badge estado-${(r.estado || "").toLowerCase()}`}
                    >
                      {r.estado}
                    </span>
                  </td>
                  <td>
                    <button
                      className="btn btn-ghost btn-sm"
                      onClick={() => setQrReserva(r)}
                    >
                      📱 Ver QR
                    </button>
                    {r.estado !== "Cancelada" && (
                      <button
                        className="btn btn-danger btn-sm"
                        onClick={() => cancelar(r.idReservacion)}
                      >
                        ✕ Cancelar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {qrReserva && (
        <Modal
          open={Boolean(qrReserva)}
          onClose={() => setQrReserva(null)}
          titulo={`QR · ${qrReserva?.codigoReservacion}`}
        >
          <div className="admin-qr-modal">
            <canvas ref={canvasRef}></canvas>
            <p className="admin-qr-evento">{qrReserva.eventoNombre}</p>
            <p className="admin-qr-participante">
              {qrReserva.participanteNombre}
            </p>
            <p className="admin-qr-nota">
              Escanea este código para verificar la reserva. El organizador
              puede registrar la asistencia con el código{" "}
              <strong>{qrReserva.codigoReservacion}</strong>.
            </p>
            <button className="btn btn-primary" onClick={() => window.print()}>
              🖨 Imprimir
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}
