import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="estado-vacio estado-404">
      <p className="error-404">404</p>
      <h1>Página no encontrada</h1>
      <p>La dirección que buscas no existe o fue movida.</p>
      <Link to="/" className="btn btn-primary">Volver al inicio</Link>
    </div>
  )
}
