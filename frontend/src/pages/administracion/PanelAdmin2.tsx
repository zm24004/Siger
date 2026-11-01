import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { eventoService } from "../../api/eventos";
import { usuarioService } from "../../api/usuarios";
import AdminReservas from "../../components/AdminReservas";
import { Loader } from "../../components/Loader";
import Modal from "../../components/Modal";
import { useAuth } from "../../context/AuthContext";
import { useNotificaciones } from "../../context/NotificacionContext";
import { useToast } from "../../context/ToastContext";
import type { Usuario } from "../../interfaces/api-interface/usuario.interface";
import type { Paginado } from "../../interfaces/common";
import type { EventoDetalle } from "../../interfaces/api-interface/eventos.interface";
import { SummaryUsers } from "./summary/SummaryUsers";
import { SummaryOrganizers } from "./summary/SummaryOrganizers";
import { SummaryParticipants } from "./summary/SummaryParticipants";
import { SummaryEvents } from "./summary/SummaryEvents";

/**
 * `estado` llega como Boolean desde el backend (UsuarioResponse.estado: Boolean),
 * pero por robustez aceptamos también los strings "ACTIVO"/"INACTIVO" y "true"/"false".
 */
function estadoActivo(estado: boolean | string | null | undefined): boolean {
  if (estado === true) return true;
  if (estado === false) return false;
  if (typeof estado === "string") {
    const v = estado.trim().toUpperCase();
    return v === "ACTIVO" || v === "TRUE" || v === "1";
  }
  return false;
}

function estadoTexto(estado: boolean | string | null | undefined): "ACTIVO" | "INACTIVO" {
  return estadoActivo(estado) ? "ACTIVO" : "INACTIVO";
}

function estadoCssKey(estado: boolean | string | null | undefined) {
  return estadoActivo(estado) ? "activo" : "inactivo";
}

function fechaCorta(value: string) {
  if (!value) return "—";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleDateString("es-ES", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

export default function PanelAdmin() {
  const { usuario } = useAuth();
  const { conectado } = useNotificaciones();
  const toast = useToast();
  const [usuarios, setUsuarios] = useState<Usuario[] | null>(null);
  const [eventos, setEventos] = useState<EventoDetalle[] | null>(null);
  const [cargando, setCargando] = useState(true);
  const [filtro, setFiltro] = useState("");
  const [pendiente, setPendiente] = useState<{ id: number; siguiente: "ACTIVO" | "INACTIVO", estadoActual: "ACTIVO" | "INACTIVO" } | null>(null);

  useEffect(() => {
    let alive = true;
    setCargando(true);
    Promise.all([
      usuarioService.getUsuarios().catch(() => []),
      eventoService.getEventosPaginado().catch(() => [])
    ])
      .then(([us, evs]) => {
        if (!alive) return;
        const usuariosData = (us as Paginado<Usuario>).content || [];
        const eventosData = (evs as Paginado<EventoDetalle>).content || [];
        setUsuarios(usuariosData);
        setEventos(eventosData);
      })
      .finally(() => alive && setCargando(false));
    return () => {
      alive = false;
    };
  }, []);

  const filtrados = useMemo(() => {
    const q = filtro.trim().toLowerCase();
    if (!q) return usuarios;
    return usuarios!.filter(
      (u) =>
        (u.nombre || "").toLowerCase().includes(q) ||
        (u.apellido || "").toLowerCase().includes(q) ||
        (u.email || "").toLowerCase().includes(q) ||
        (u.rol || "").toLowerCase().includes(q),
    );
  }, [usuarios, filtro]);

  const stats = useMemo(() => {
    const total = usuarios?.length || 0;
    const activos = usuarios?.filter((u) => estadoActivo(u.estado)).length || 0;
    const inactivos = total - activos;
    const porRol = usuarios?.reduce<Record<string, number>>((acc, u) => {
      const r = u.rol || "DESCONOCIDO";
      acc[r] = (acc[r] || 0) + 1;
      return acc;
    }, {}) || {};
    const organizadores = porRol.ORGANIZADOR || 0;
    const participantes = porRol.PARTICIPANTE || 0;
    const administradores = porRol.ADMINISTRADOR || 0;
    const totalEventos = eventos?.length || 0;
    return {
      total,
      activos,
      inactivos,
      organizadores,
      participantes,
      administradores,
      totalEventos,
    };
  }, [usuarios, eventos]);

  async function confirmarCambio() {
    if (!pendiente) return;
    const { id, siguiente } = pendiente;
    try {
      const actualizado = await usuarioService.cambiarEstado(id, siguiente);
      setUsuarios((list) =>
        list!.map((u) =>
            u.idUsuario  === Number(id)
            ? { ...u, estado: actualizado?.estado ?? siguiente }
            : u,
        ),
      );
      toast.exito(
        `Usuario ${siguiente === "ACTIVO" ? "activado" : "desactivado"} correctamente.`,
      );
    } catch (err: any) {
      toast.error(err.message || "No se pudo cambiar el estado del usuario.");
    } finally {
      setPendiente(null);
    }
  }

  const nombreCompleto = [usuario?.nombre, usuario?.apellido]
    .filter(Boolean)
    .join(" ");

  return (
    <div className="panel-wrapper">
      <div className="panel-header">
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

      <section className="panel-stats" aria-label="Resumen del sistema">
        <SummaryUsers/>
        <SummaryOrganizers/>
        <SummaryParticipants/>
        <SummaryEvents/>
      </section>


      <section className="panel-section" aria-label="Gestión de usuarios">

      <div className="grid grid-cols-6">
            <div className="bg-amber-700">
            </div>
            <div className="bg-purple-500">
            </div>
            <div className="col-span-12 bg-slate-600">

            </div>
      </div>
        <div className="panel-section-head">
          <h2>Gestión de usuarios</h2>
          {stats.administradores > 0 && (
            <span className="texto-secundario">
              {stats.administradores} administrador
              {stats.administradores === 1 ? "" : "es"}
            </span>
          )}
        </div>

        {cargando ? (
          <Loader label="Cargando usuarios…" />
        ) : filtrados!.length === 0 ? (
          <div className="estado-vacio estado-vacio-sm">
            <h3>Sin resultados</h3>
            <p>
              {filtro
                ? "No hay usuarios que coincidan con la búsqueda."
                : "Aún no hay usuarios registrados en el sistema."}
            </p>
          </div>
        ) : (
          <div
            className="tabla-wrapper"
            role="region"
            aria-label="Tabla de usuarios"
            tabIndex={0}
          >
            <table className="tabla">
              <thead>
                <tr>
                  <th scope="col">Nombre</th>
                  <th scope="col">Correo</th>
                  <th scope="col">Rol</th>
                  <th scope="col">Teléfono</th>
                  <th scope="col">Registro</th>
                  <th scope="col">Estado</th>
                  <th scope="col">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {filtrados!.map((u) => {
                  const id = u.idUsuario;
                  const activo = estadoActivo(u.estado);
                  const nombreCompletoFila =
                    [u.nombre, u.apellido].filter(Boolean).join(" ") || "—";
                  const telefonoFila =
                    [u.codigoPais, u.telefono].filter(Boolean).join(" ") || "—";
                  return (
                    <tr key={id}>
                      <td>{nombreCompletoFila}</td>
                      <td>
                        <a href={`mailto:${u.email}`} className="link-primario">
                          {u.email}
                        </a>
                      </td>
                      <td>
                        <span
                          className={`badge badge-rol badge-rol-${(u.rol || "").toLowerCase()}`}
                        >
                          {u.rol}
                        </span>
                      </td>
                      <td className="texto-secundario">{telefonoFila}</td>
                      <td className="texto-secundario">
                        {fechaCorta(u.fechaRegistro.toString())}
                      </td>
                      <td>
                        <span
                          className={`badge badge-${estadoCssKey(u.estado)}`}
                        >
                          {estadoTexto(u.estado)}
                        </span>
                      </td>
                      <td>
                        <button
                          type="button"
                          className={`btn btn-sm ${activo ? "btn-danger" : "btn-primary"}`}
                          onClick={() =>
                            setPendiente({
                              id,
                              estadoActual: u.estado  ? "INACTIVO" : "ACTIVO",
                              siguiente: activo ? "INACTIVO" : "ACTIVO",
                            })
                          }
                          aria-label={`${activo ? "Desactivar" : "Activar"} a ${nombreCompletoFila}`}
                        >
                          {activo ? "Desactivar" : "Activar"}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <AdminReservas />

      <section
        className="panel-section panel-section-placeholder"
        aria-label="Reportes"
      >
        <div className="panel-section-head">
          <h2>Reportes</h2>
          <span className="badge">Próximamente</span>
        </div>
        <div className="estado-vacio estado-vacio-sm">
          <h3>Reportes en construcción</h3>
          <p>
            Aquí verás gráficos de eventos más populares, tasa de asistencia y
            crecimiento mensual de usuarios.
          </p>
        </div>
      </section>

      <Modal
        open={!!pendiente}
        onClose={() => setPendiente(null)}
        titulo="Confirmar cambio de estado"
        ariaLabel="Confirmar cambio de estado del usuario"
        footer={
          <>
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => setPendiente(null)}
            >
              Cancelar
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={confirmarCambio}
            >
              Confirmar
            </button>
          </>
        }
      >
        {pendiente && (
          <p>
            ¿Deseas{" "}
            <strong>
              {pendiente.siguiente === "ACTIVO" ? "activar" : "desactivar"}
            </strong>{" "}
            este usuario?
            {pendiente.siguiente === "INACTIVO" &&
              " Ya no podrá iniciar sesión."}
          </p>
        )}
      </Modal>
    </div>
  );
}
