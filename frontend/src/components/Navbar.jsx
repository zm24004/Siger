import { useEffect } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useNotificaciones } from '../context/NotificacionContext.jsx'
import { useToast } from './Toast.jsx'

const ROL_HOME = {
  PARTICIPANTE: '/mi-panel',
  ORGANIZADOR: '/organizador',
  ADMINISTRADOR: '/admin'
}

export default function Navbar() {
  const { usuario, logout, isAuthenticated, rol } = useAuth()
  const { noLeidas, ultima, conectado } = useNotificaciones()
  const toast = useToast()
  const navigate = useNavigate()

  useEffect(() => {
    // Cierra el menú móvil al cambiar de ruta (accesibilidad)
    const closeOnNav = () => {
      const toggler = document.getElementById('nav-toggle')
      const menu = document.getElementById('nav-menu')
      if (menu) menu.classList.remove('open')
      if (toggler) toggler.setAttribute('aria-expanded', 'false')
    }
    return closeOnNav
  }, [])

  // Toast en tiempo real cuando llega una nueva notificación vía STOMP.
  useEffect(() => {
    if (ultima?.mensaje) {
      toast.info(ultima.mensaje)
    }
  }, [ultima])

  function handleLogout() {
    logout()
    navigate('/')
  }

  function toggleMenu() {
    const menu = document.getElementById('nav-menu')
    const btn = document.getElementById('nav-toggle')
    if (!menu || !btn) return
    const isOpen = menu.classList.toggle('open')
    btn.setAttribute('aria-expanded', String(isOpen))
  }

  const panelPath = ROL_HOME[rol] || '/eventos'

  return (
    <header className="navbar" role="banner">
      <div className="navbar-contenedor">
        <Link to="/" className="navbar-brand" aria-label="Ir a la página de inicio de SIGER">
          <span className="navbar-logo" aria-hidden="true">S</span>
          <span className="navbar-titulo">SIGER</span>
        </Link>

        <button
          id="nav-toggle"
          className="navbar-toggle"
          aria-label="Abrir menú de navegación"
          aria-controls="nav-menu"
          aria-expanded="false"
          onClick={toggleMenu}
          type="button"
        >
          <span aria-hidden="true"></span>
          <span aria-hidden="true"></span>
          <span aria-hidden="true"></span>
        </button>

        <nav id="nav-menu" className="navbar-menu" aria-label="Navegación principal">
          <NavLink to="/eventos" className="navbar-link">Eventos</NavLink>

          {isAuthenticated && rol === 'PARTICIPANTE' && (
            <>
              <NavLink to="/mi-panel" className="navbar-link">Mi panel</NavLink>
              <NavLink to="/mis-reservaciones" className="navbar-link">Mis reservas</NavLink>
            </>
          )}
          {isAuthenticated && rol === 'ORGANIZADOR' && (
            <>
              <NavLink to="/organizador" className="navbar-link" end>Mi panel</NavLink>
              <NavLink to="/organizador/nuevo-evento" className="navbar-link">Crear evento</NavLink>
              <NavLink to="/organizador/asistencia" className="navbar-link">Asistencia</NavLink>
            </>
          )}
          {isAuthenticated && rol === 'ADMINISTRADOR' && (
            <NavLink to="/admin" className="navbar-link">Admin</NavLink>
          )}

          {!isAuthenticated ? (
            <>
              <Link to="/login" className="navbar-link">Iniciar sesión</Link>
              <Link to="/registro" className="btn btn-primary navbar-cta">Registrarse</Link>
            </>
          ) : (
            <>
              <Link
                to={panelPath}
                className="navbar-notif"
                aria-label={`Notificaciones${noLeidas > 0 ? `, ${noLeidas} sin leer` : ''}`}
                title="Tus notificaciones"
              >
                <span aria-hidden="true">🔔</span>
                {noLeidas > 0 && (
                  <span className="navbar-notif-badge" aria-hidden="true">
                    {noLeidas > 99 ? '99+' : noLeidas}
                  </span>
                )}
                <span
                  className={`navbar-notif-dot${conectado ? '' : ' off'}`}
                  title={conectado ? 'Conectado' : 'Desconectado'}
                  aria-hidden="true"
                ></span>
              </Link>
              <Link
                to={panelPath}
                className="navbar-usuario"
                aria-label={`Sesión iniciada como ${usuario?.nombre}${usuario?.apellido ? ' ' + usuario.apellido : ''}. Ir a mi panel`}
              >
                {usuario?.nombre}
                {usuario?.apellido ? ` ${usuario.apellido}` : ''}
                <span className="navbar-rol">{usuario?.rol}</span>
              </Link>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={handleLogout}
                aria-label="Cerrar sesión"
              >
                Cerrar sesión
              </button>
            </>
          )}
        </nav>
      </div>
    </header>
  )
}
