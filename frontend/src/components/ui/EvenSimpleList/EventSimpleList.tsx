import { Link } from "react-router-dom";
import { useAuth } from "../../../context/AuthContext";
import type { EventoDetalle } from "../../../interfaces/api-interface/eventos.interface";
import * as dateUtils from '../../../util/date-utils';

interface EventSimpleListProps {
    eventList: EventoDetalle[]
}

export const EventSimpleList = ({ eventList }: EventSimpleListProps) => {
    const { isAuthenticated, rol } = useAuth();
    return (
        <>
            <ul className="catalogo-grid" aria-label="Listado de eventos">
                {eventList.map((ev) => {
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
                                    <p className="evento-meta"><span>📅</span>{dateUtils.formatDate(ev.fecha!.toString())} · {ev.horaInicio}</p>
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
        </>
    )
}
