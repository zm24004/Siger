import { useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const ROL_HOME = {
  PARTICIPANTE: '/mi-panel',
  ORGANIZADOR: '/organizador',
  ADMINISTRADOR: '/admin'
}

const FEATURES = [
  {
    icono: '📅',
    titulo: 'Publica eventos con cupos limitados',
    descripcion: 'Como organizador, define capacidad máxima, fecha, hora y ubicación. SIGER controla la disponibilidad en tiempo real.'
  },
  {
    icono: '🎟️',
    titulo: 'Reserva tu lugar en segundos',
    descripcion: 'Como participante, navega el catálogo, reserva con un clic y recibe un comprobante con código único.'
  },
  {
    icono: '🔔',
    titulo: 'Notificaciones en tiempo real',
    descripcion: 'Recibe avisos instantáneos vía WebSocket cuando tu reservación se confirma o un evento cambia.'
  }
]

export default function Bienvenida() {
  const { isAuthenticated, rol, cargando } = useAuth()
  const navigate = useNavigate()

  // Si el usuario ya está logueado, lo mandamos a su panel.
  useEffect(() => {
    if (!cargando && isAuthenticated && rol) {
      navigate(ROL_HOME[rol] || '/eventos', { replace: true })
    }
  }, [cargando, isAuthenticated, rol, navigate])

  // Evitamos parpadeo mientras decidimos si redirigir.
  if (!cargando && isAuthenticated && rol) {
    return null
  }

  return (
    <div className="bienvenida">
      <section className="hero">
        <div className="hero-contenido">
          <span className="hero-chip">Sistema de Gestión de Eventos y Reservas</span>
          <h1 className="hero-titulo">SIGER</h1>
          <p className="hero-subtitulo">
            Publica, descubre y reserva eventos universitarios en un solo lugar.
            Cupos controlados en tiempo real, notificaciones instantáneas y comprobantes únicos.
          </p>
          <div className="hero-acciones">
            <Link to="/registro" className="btn btn-primary btn-lg">Registrarse</Link>
            <Link to="/login" className="btn btn-ghost btn-lg">Iniciar sesión</Link>
          </div>
          <p className="hero-link-secundario">
            <Link to="/eventos" className="auth-link">Explorar el catálogo público →</Link>
          </p>
        </div>
      </section>

      <section className="features" aria-label="Características principales">
        <h2 className="features-titulo">¿Qué puedes hacer con SIGER?</h2>
        <ul className="features-grid">
          {FEATURES.map((f) => (
            <li key={f.titulo} className="feature-card">
              <span className="feature-icono" aria-hidden="true">{f.icono}</span>
              <h3 className="feature-titulo">{f.titulo}</h3>
              <p className="feature-desc">{f.descripcion}</p>
            </li>
          ))}
        </ul>
      </section>

      <section className="recuperar-recordatorio" aria-label="Recuperación de contraseña">
        <p>
          ¿Olvidaste tu contraseña?{' '}
          <Link to="/recuperar-contrasena" className="auth-link">Recupérala aquí</Link>.
        </p>
      </section>

      <footer className="bienvenida-footer">
        <p>© {new Date().getFullYear()} SIGER — Universidad. Hecho con React + Spring Boot.</p>
      </footer>
    </div>
  )
}
