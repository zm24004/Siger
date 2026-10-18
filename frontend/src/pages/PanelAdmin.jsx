import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { getCatalogo } from "../api/eventos.js";
import { cambiarEstado, getUsuarios } from "../api/usuarios.js";
import AdminReservas from "../components/AdminReservas.jsx";
import Loader from "../components/Loader.jsx";
import Modal from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { useNotificaciones } from "../context/NotificacionContext.jsx";

/**
 * `estado` llega como Boolean desde el backend (UsuarioResponse.estado: Boolean),
 * pero por robustez aceptamos también los strings "ACTIVO"/"INACTIVO" y "true"/"false".
 */
function estadoActivo(estado) {
  if (estado === true) return true;
  if (estado === false) return false;
  if (typeof estado === "string") {
    const v = estado.trim().toUpperCase();
    return v === "ACTIVO" || v === "TRUE" || v === "1";
  }
  return false;
}

function estadoTexto(estado) {
  return estadoActivo(estado) ? "ACTIVO" : "INACTIVO";
}

function estadoCssKey(estado) {
  return estadoActivo(estado) ? "activo" : "inactivo";
}

function fechaCorta(value) {
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
  const [usuarios, setUsuarios] = useState([]);
  const [eventos, setEventos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [filtro, setFiltro] = useState("");
  const [pendiente, setPendiente] = useState(null);

  useEffect(() => {
    let alive = true;
    setCargando(true);
    Promise.all([getUsuarios().catch(() => []), getCatalogo().catch(() => [])])
      .then(([us, evs]) => {
        if (!alive) return;
        setUsuarios(Array.isArray(us) ? us : []);
        setEventos(Array.isArray(evs) ? evs : []);
      })
      .finally(() => alive && setCargando(false));
    return () => {
      alive = false;
    };
  }, []);

  const filtrados = useMemo(() => {
    const q = filtro.trim().toLowerCase();
    if (!q) return usuarios;
    return usuarios.filter(
      (u) =>
        (u.nombre || "").toLowerCase().includes(q) ||
        (u.apellido || "").toLowerCase().includes(q) ||
        (u.email || "").toLowerCase().includes(q) ||
        (u.rol || "").toLowerCase().includes(q),
    );
  }, [usuarios, filtro]);

  const stats = useMemo(() => {
    const total = usuarios.length;
    const activos = usuarios.filter((u) => estadoActivo(u.estado)).length;
    const inactivos = total - activos;
    const porRol = usuarios.reduce((acc, u) => {
      const r = u.rol || "DESCONOCIDO";
      acc[r] = (acc[r] || 0) + 1;
      return acc;
    }, {});
    const organizadores = porRol.ORGANIZADOR || 0;
    const participantes = porRol.PARTICIPANTE || 0;
    const administradores = porRol.ADMINISTRADOR || 0;
    const totalEventos = eventos.length;
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
      const actualizado = await cambiarEstado(id, siguiente);
      setUsuarios((list) =>
        list.map((u) =>
          Number(u.idUsuario ?? u.id) === Number(id)
            ? { ...u, estado: actualizado?.estado ?? siguiente }
            : u,
        ),
      );
      toast.exito(
        `Usuario ${siguiente === "ACTIVO" ? "activado" : "desactivado"} correctamente.`,
      );
    } catch (err) {
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
      <header className="panel-header">
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
        <input
          type="search"
          className="form-control form-control-sm"
          placeholder="Buscar usuario, correo o rol…"
          value={filtro}
          onChange={(e) => setFiltro(e.target.value)}
          aria-label="Filtrar usuarios"
        />
      </header>

      <section className="panel-stats" aria-label="Resumen del sistema">
        <div className="stat-card">
          <span className="stat-label">Usuarios totales</span>
          <span className="stat-valor">{stats.total}</span>
          <span className="texto-secundario stat-sub">
            {stats.activos} activos · {stats.inactivos} inactivos
          </span>
        </div>
        <div className="stat-card stat-card-rol">
          <span className="stat-label">Organizadores</span>
          <span className="stat-valor">{stats.organizadores}</span>
          <span className="texto-secundario stat-sub">Publican eventos</span>
        </div>
        <div className="stat-card stat-card-exito">
          <span className="stat-label">Participantes</span>
          <span className="stat-valor">{stats.participantes}</span>
          <span className="texto-secundario stat-sub">Reservan lugares</span>
        </div>
        <div className="stat-card stat-card-peligro">
          <span className="stat-label">Eventos</span>
          <span className="stat-valor">{stats.totalEventos}</span>
          <Link to="/eventos" className="stat-link">
            Ver catálogo →
          </Link>
        </div>
      </section>

      <section className="panel-section" aria-label="Gestión de usuarios">
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
        ) : filtrados.length === 0 ? (
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
                {filtrados.map((u) => {
                  const id = u.idUsuario ?? u.id;
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
                        {fechaCorta(u.fechaRegistro)}
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
                              estadoActual: u.estado,
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
