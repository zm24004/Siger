import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { loginPost } from '../api/auth.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useToast } from '../components/Toast.jsx'

const ROL_HOME = {
  PARTICIPANTE: '/mi-panel',
  ORGANIZADOR: '/organizador',
  ADMINISTRADOR: '/admin'
}

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const toast = useToast()

  const [form, setForm] = useState({ email: '', contrasena: '' })
  const [errores, setErrores] = useState({})
  const [enviando, setEnviando] = useState(false)

  function handleChange(e) {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
    setErrores((er) => ({ ...er, [name]: undefined }))
  }

  function validar() {
    const e = {}
    if (!form.email.trim()) e.email = 'El correo es obligatorio.'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Correo no válido.'
    if (!form.contrasena) e.contrasena = 'La contraseña es obligatoria.'
    setErrores(e)
    return Object.keys(e).length === 0
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!validar()) return
    setEnviando(true)
    try {
      const data = await loginPost({ email: form.email.trim(), contrasena: form.contrasena })
      login(data)
      toast.exito(`Bienvenido, ${data.nombre}.`)
      const destino = location.state?.from || ROL_HOME[data.rol] || '/'
      navigate(destino, { replace: true })
    } catch (err) {
      if (err.status === 401) {
        toast.error('Correo o contraseña incorrectos.')
        setErrores({ form: 'Credenciales inválidas.' })
      } else {
        toast.error(err.message || 'No se pudo iniciar sesión.')
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <h1 className="auth-titulo">Iniciar sesión</h1>
        <p className="auth-subtitulo">Accede a tu cuenta SIGER para gestionar reservas de eventos.</p>

        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          <div className="form-group">
            <label htmlFor="email">Correo electrónico</label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              className={`form-control ${errores.email ? 'is-invalid' : ''}`}
              value={form.email}
              onChange={handleChange}
              placeholder="usuario@ejemplo.com"
              aria-invalid={!!errores.email}
              aria-describedby={errores.email ? 'err-email' : undefined}
            />
            {errores.email && (
              <p id="err-email" className="form-error">{errores.email}</p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="contrasena">Contraseña</label>
            <input
              id="contrasena"
              name="contrasena"
              type="password"
              autoComplete="current-password"
              className={`form-control ${errores.contrasena ? 'is-invalid' : ''}`}
              value={form.contrasena}
              onChange={handleChange}
              aria-invalid={!!errores.contrasena}
              aria-describedby={errores.contrasena ? 'err-contra' : undefined}
            />
            {errores.contrasena && (
              <p id="err-contra" className="form-error">{errores.contrasena}</p>
            )}
          </div>

          {errores.form && (
            <p className="form-error form-error-block" role="alert">{errores.form}</p>
          )}

          <button type="submit" className="btn btn-primary btn-block" disabled={enviando}>
            {enviando ? 'Ingresando…' : 'Iniciar sesión'}
          </button>

          <p className="auth-pie auth-pie-sm">
            ¿Olvidaste tu contraseña?{' '}
            <Link to="/recuperar-contrasena" className="auth-link">Recupérala</Link>
          </p>
        </form>

        <p className="auth-pie">
          ¿No tienes cuenta?{' '}
          <Link to="/registro" className="auth-link">Regístrate</Link>
        </p>
      </div>
    </div>
  )
}
