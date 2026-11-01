import { useAuth } from '../../context/AuthContext';
import { useNotificaciones } from '../../context/NotificacionContext';

export const HeaderAdmin = () => {
    const { usuario } = useAuth();
    const { conectado } = useNotificaciones();

    const nombreCompleto = [usuario?.nombre, usuario?.apellido]
        .filter(Boolean)
        .join(" ");

    return (
        <>
            <div className="panel-header mt-4 mb-8  ">
                <div className="panel-header-greeting">
                    <h1>Hola, {nombreCompleto || "administrador"} 🛡️</h1>
                    <div className="panel-header-meta">
                        <span className="badge badge-rol badge-rol-administrador">
                            ADMINISTRADOR
                        </span>
                        <span
                            className={`panel-conn-dot ${conectado ? "on" : "off"}`}
                            title={
                                conectado
                                    ? "Notificaciones conectadas"
                                    : "Sin conexión en tiempo real"
                            }
                            aria-label={
                                conectado
                                    ? "Notificaciones conectadas"
                                    : "Sin conexión en tiempo real"
                            }
                        ></span>
                        <span className="texto-secundario panel-conn-text">
                            {conectado ? "En línea" : "Reconectando…"}
                        </span>
                    </div>
                </div>
            </div>
        </>
    )
}
