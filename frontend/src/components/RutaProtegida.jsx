import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import Loader from './Loader.jsx'

const ROL_HOME = {
  PARTICIPANTE: '/mi-panel',
  ORGANIZADOR: '/organizador',
  ADMINISTRADOR: '/admin'
}

/**
 * RutaProtegida
 * @param {Object} props
 * @param {string[]} [props.roles] - Roles permitidos. Si no se especifica, basta con estar autenticado.
 * @param {React.ReactNode} props.children - Contenido a renderizar si está autorizado.
 */
export default function RutaProtegida({ roles, children }) {
  const { usuario, cargando, rol } = useAuth()
  const location = useLocation()

  if (cargando) {
    return (
      <div className="centrado">
        <Loader />
      </div>
    )
  }

  if (!usuario) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }

  if (roles && roles.length > 0 && !roles.includes(rol)) {
    // Usuario autenticado pero sin permisos: lo mandamos a su propio panel.
    const destino = ROL_HOME[rol] || '/eventos'
    return <Navigate to={destino} replace />
  }

  return children
}
