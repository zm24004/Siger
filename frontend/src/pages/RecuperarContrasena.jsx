import { useState } from 'react'
import { Link } from 'react-router-dom'
import { recuperarContrasenaPost } from '../api/auth.js'
import { useToast } from '../components/Toast.jsx'

function emailValido(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

export default function RecuperarContrasena() {
  const toast = useToast()
  const [form, setForm] = useState({ email: '', nombreOAlias: '' })
  const [errores, setErrores] = useState({})
  const [enviando, setEnviando] = useState(false)
  const [resultado, setResultado] = useState(null) // { email, message, protocolo }

  function handleChange(e) {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
    setErrores((er) => ({ ...er, [name]: undefined }))
  }

  function validar() {
    const e = {}
    if (!form.email.trim()) e.email = 'El correo es obligatorio.'
    else if (!emailValido(form.email.trim())) e.email = 'Correo no válido.'
    if (!form.nombreOAlias.trim()) e.nombreOAlias = 'El nombre o alias es obligatorio.'
    else if (form.nombreOAlias.trim().length < 2) e.nombreOAlias = 'Mínimo 2 caracteres.'
    setErrores(e)
    return Object.keys(e).length === 0
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!validar()) return
    setEnviando(true)
    try {
      const data = await recuperarContrasenaPost({
        email: form.email.trim(),
        nombreOAlias: form.nombreOAlias.trim()
      })
      // El backend SIEMPRE devuelve el mismo mensaje (protocolo de seguridad).
      setResultado({
        email: form.email.trim(),
        message:
          data?.message ||
          `Se ha enviado un correo a ${form.email.trim()} con las instrucciones para recuperar tu contraseña.`,
        protocolo:
          data?.protocolo ||
          'Revisa tu bandeja de entrada. Si no lo recibes en unos minutos, revisa spam.'
      })
    } catch (err) {
      toast.error(err.message || 'No se pudo procesar la solicitud. Inténtalo más tarde.')
    } finally {
      setEnviando(false)
    }
  }

  function volver() {
    setResultado(null)
    setForm({ email: '', nombreOAlias: '' })
  }

  return (
    <div className="auth-wrapper auth-recovery-wrapper">
      <div className="auth-card auth-recovery-card">
        {resultado ? (
          <div className="auth-success-card" role="status" aria-live="polite">
            <span className="auth-success-icono" aria-hidden="true">✅</span>
            <h1 className="auth-titulo auth-titulo-center">Revisa tu correo</h1>
            <p className="auth-success-mensaje">{resultado.message}</p>
            <p className="auth-success-protocolo">{resultado.protocolo}</p>
            <p className="auth-success-nota">
              Por seguridad, no revelamos si la cuenta existe. Si no recibes el correo,
              verifica la dirección o contacta al administrador.
            </p>
            <div className="auth-success-acciones">
              <Link to="/login" className="btn btn-primary btn-block">Volver a iniciar sesión</Link>
              <button type="button" className="btn btn-ghost btn-block" onClick={volver}>
                Probar con otro correo
              </button>
            </div>
          </div>
        ) : (
          <>
            <h1 className="auth-titulo">Recuperar contraseña</h1>
            <p className="auth-subtitulo">
              Te enviaremos (de forma simulada en este entorno) las instrucciones para
              restablecer tu acceso. Ingresa los datos que usaste al registrarte.
            </p>

            <form onSubmit={handleSubmit} className="auth-form" noValidate>
              <div className="form-group">
                <label htmlFor="email">Correo electrónico <span aria-hidden="true">*</span></label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  className={`form-control ${errores.email ? 'is-invalid' : ''}`}
                  value={form.email}
                  onChange={handleChange}
                  aria-invalid={!!errores.email}
                  aria-describedby={errores.email ? 'err-email' : undefined}
                  placeholder="usuario@ejemplo.com"
                  required
                />
                {errores.email && <p id="err-email" className="form-error">{errores.email}</p>}
              </div>

              <div className="form-group">
                <label htmlFor="nombreOAlias">Nombre o alias <span aria-hidden="true">*</span></label>
                <input
                  id="nombreOAlias"
                  name="nombreOAlias"
                  type="text"
                  autoComplete="username"
                  className={`form-control ${errores.nombreOAlias ? 'is-invalid' : ''}`}
                  value={form.nombreOAlias}
                  onChange={handleChange}
                  aria-invalid={!!errores.nombreOAlias}
                  aria-describedby={errores.nombreOAlias ? 'err-alias' : undefined}
                  placeholder="El nombre que usaste al registrarte"
                  required
                />
                {errores.nombreOAlias && <p id="err-alias" className="form-error">{errores.nombreOAlias}</p>}
              </div>

              <button type="submit" className="btn btn-primary btn-block" disabled={enviando}>
                {enviando ? 'Enviando…' : 'Enviar instrucciones'}
              </button>
            </form>

            <p className="auth-pie">
              ¿Recordaste tu contraseña?{' '}
              <Link to="/login" className="auth-link">Inicia sesión</Link>
            </p>
            <p className="auth-pie auth-pie-sm">
              ¿No tienes cuenta?{' '}
              <Link to="/registro" className="auth-link">Regístrate</Link>
            </p>
          </>
        )}
      </div>
    </div>
  )
}
