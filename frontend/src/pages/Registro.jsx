import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { registroPost } from '../api/auth.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useToast } from '../components/Toast.jsx'
import { PAISES, detectarPais } from '../data/paises.js'

const ROL_HOME = {
  PARTICIPANTE: '/mi-panel',
  ORGANIZADOR: '/organizador',
  ADMINISTRADOR: '/admin'
}

const ROLES = [
  { value: 'PARTICIPANTE', label: 'Participante', descripcion: 'Reservo lugares en eventos' },
  { value: 'ORGANIZADOR', label: 'Organizador', descripcion: 'Publico y gestiono eventos' }
]

function emailValido(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

export default function Registro() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()

  const [form, setForm] = useState({
    nombre: '',
    apellido: '',
    email: '',
    telefono: '',
    codigoPais: '+503',
    contrasena: '',
    confirmar: '',
    rol: 'PARTICIPANTE'
  })
  const [errores, setErrores] = useState({})
  const [enviando, setEnviando] = useState(false)
  const [mostrarPass, setMostrarPass] = useState(false)
  const [mostrarConfirm, setMostrarConfirm] = useState(false)

  // Auto-detección del país del navegador al montar el formulario.
  useEffect(() => {
    const detectado = detectarPais()
    setForm((f) => ({ ...f, codigoPais: detectado.codigo }))
  }, [])

  function handleChange(e) {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
    setErrores((er) => ({ ...er, [name]: undefined }))
  }

  function handlePais(e) {
    setForm((f) => ({ ...f, codigoPais: e.target.value }))
    setErrores((er) => ({ ...er, telefono: undefined }))
  }

  function validar() {
    const e = {}
    if (!form.nombre.trim()) e.nombre = 'El nombre es obligatorio.'
    else if (form.nombre.trim().length < 3) e.nombre = 'Mínimo 3 caracteres.'
    // Apellido es opcional pero, si viene, debe tener al menos 3 caracteres.
    if (form.apellido.trim() && form.apellido.trim().length < 3) {
      e.apellido = 'Mínimo 3 caracteres (o déjalo vacío).'
    }
    if (!form.email.trim()) e.email = 'El correo es obligatorio.'
    else if (!emailValido(form.email)) e.email = 'Correo no válido.'
    if (!form.telefono.trim()) e.telefono = 'El teléfono es obligatorio.'
    else if (!/^[0-9]{6,15}$/.test(form.telefono.trim())) {
      e.telefono = 'Escribe solo dígitos (6–15). El código de país va aparte.'
    }
    if (!form.contrasena) e.contrasena = 'La contraseña es obligatoria.'
    else if (form.contrasena.length < 8) e.contrasena = 'Mínimo 8 caracteres.'
    if (form.contrasena !== form.confirmar) e.confirmar = 'Las contraseñas no coinciden.'
    if (!['PARTICIPANTE', 'ORGANIZADOR'].includes(form.rol)) e.rol = 'Rol no válido.'
    setErrores(e)
    return Object.keys(e).length === 0
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!validar()) return
    setEnviando(true)
    try {
      const payload = {
        nombre: form.nombre.trim(),
        apellido: form.apellido.trim(),
        email: form.email.trim(),
        telefono: form.telefono.trim(),
        codigoPais: form.codigoPais,
        contrasena: form.contrasena,
        rol: form.rol
      }
      const data = await registroPost(payload)
      login(data)
      toast.exito(`Cuenta creada. ¡Bienvenido, ${data.nombre}!`)
      navigate(ROL_HOME[data.rol] || '/', { replace: true })
    } catch (err) {
      if (err.status === 400 || err.status === 409) {
        const msg = err.message || ''
        if (/email|correo/i.test(msg)) {
          setErrores({ email: msg })
        } else if (/tel[eé]fono|c[oó]digo/i.test(msg)) {
          setErrores({ telefono: msg })
        } else if (/contrase[ñn]a/i.test(msg)) {
          setErrores({ contrasena: msg })
        } else if (/rol/i.test(msg)) {
          setErrores({ rol: msg })
        } else {
          toast.error(msg || 'No se pudo registrar el usuario.')
        }
      } else {
        toast.error(err.message || 'No se pudo registrar el usuario.')
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="auth-wrapper">
      <div className="auth-card auth-card-wide">
        <h1 className="auth-titulo">Crear cuenta</h1>
        <p className="auth-subtitulo">Regístrate para reservar o gestionar eventos.</p>

        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          <div className="form-grid-2">
            <div className="form-group">
              <label htmlFor="nombre">Nombre <span aria-hidden="true">*</span></label>
              <input
                id="nombre"
                name="nombre"
                type="text"
                autoComplete="given-name"
                className={`form-control ${errores.nombre ? 'is-invalid' : ''}`}
                value={form.nombre}
                onChange={handleChange}
                aria-invalid={!!errores.nombre}
                aria-describedby={errores.nombre ? 'err-nombre' : undefined}
                required
              />
              {errores.nombre && <p id="err-nombre" className="form-error">{errores.nombre}</p>}
            </div>

            <div className="form-group">
              <label htmlFor="apellido">Apellido <span className="form-ayuda-opcional">(opcional)</span></label>
              <input
                id="apellido"
                name="apellido"
                type="text"
                autoComplete="family-name"
                className={`form-control ${errores.apellido ? 'is-invalid' : ''}`}
                value={form.apellido}
                onChange={handleChange}
                aria-invalid={!!errores.apellido}
                aria-describedby={errores.apellido ? 'err-apellido' : undefined}
              />
              {errores.apellido && <p id="err-apellido" className="form-error">{errores.apellido}</p>}
            </div>
          </div>

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
            <label htmlFor="telefono">Teléfono <span aria-hidden="true">*</span></label>
            <div className="form-row-phone">
              <label htmlFor="codigoPais" className="sr-only">Código de país</label>
              <select
                id="codigoPais"
                name="codigoPais"
                className="form-control form-control-pais"
                value={form.codigoPais}
                onChange={handlePais}
                aria-label="Código de país"
              >
                {PAISES.map((p) => (
                  <option key={p.iso} value={p.codigo}>
                    {p.bandera} {p.codigo} {p.nombre}
                  </option>
                ))}
              </select>
              <input
                id="telefono"
                name="telefono"
                type="tel"
                inputMode="numeric"
                autoComplete="tel-national"
                className={`form-control form-control-tel ${errores.telefono ? 'is-invalid' : ''}`}
                value={form.telefono}
                onChange={handleChange}
                placeholder="7612 3456"
                aria-invalid={!!errores.telefono}
                aria-describedby={errores.telefono ? 'err-telefono' : undefined}
                required
              />
            </div>
            <p className="form-ayuda">Detectamos tu país automáticamente. Puedes cambiarlo si es necesario.</p>
            {errores.telefono && <p id="err-telefono" className="form-error">{errores.telefono}</p>}
          </div>

          <div className="form-grid-2">
            <div className="form-group">
              <label htmlFor="contrasena">Contraseña <span aria-hidden="true">*</span></label>
              <div className="input-wrap">
                <input
                  id="contrasena"
                  name="contrasena"
                  type={mostrarPass ? 'text' : 'password'}
                  autoComplete="new-password"
                  className={`form-control ${errores.contrasena ? 'is-invalid' : ''}`}
                  value={form.contrasena}
                  onChange={handleChange}
                  aria-invalid={!!errores.contrasena}
                  aria-describedby={errores.contrasena ? 'err-contra' : undefined}
                  minLength={8}
                  required
                />
                <button
                  type="button"
                  className="btn-toggle-pass"
                  onClick={() => setMostrarPass((v) => !v)}
                  aria-label={mostrarPass ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                  aria-pressed={mostrarPass}
                  tabIndex={-1}
                >
                  {mostrarPass ? '🙈' : '👁️'}
                </button>
              </div>
              {errores.contrasena && <p id="err-contra" className="form-error">{errores.contrasena}</p>}
            </div>

            <div className="form-group">
              <label htmlFor="confirmar">Confirmar contraseña <span aria-hidden="true">*</span></label>
              <div className="input-wrap">
                <input
                  id="confirmar"
                  name="confirmar"
                  type={mostrarConfirm ? 'text' : 'password'}
                  autoComplete="new-password"
                  className={`form-control ${errores.confirmar ? 'is-invalid' : ''}`}
                  value={form.confirmar}
                  onChange={handleChange}
                  aria-invalid={!!errores.confirmar}
                  aria-describedby={errores.confirmar ? 'err-conf' : undefined}
                  required
                />
                <button
                  type="button"
                  className="btn-toggle-pass"
                  onClick={() => setMostrarConfirm((v) => !v)}
                  aria-label={mostrarConfirm ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                  aria-pressed={mostrarConfirm}
                  tabIndex={-1}
                >
                  {mostrarConfirm ? '🙈' : '👁️'}
                </button>
              </div>
              {errores.confirmar && <p id="err-conf" className="form-error">{errores.confirmar}</p>}
            </div>
          </div>

          <fieldset className="form-group form-fieldset-rol" aria-label="Tipo de cuenta">
            <legend className="form-label-legend">Tipo de cuenta <span aria-hidden="true">*</span></legend>
            <div className="rol-opciones">
              {ROLES.map((r) => {
                const id = `rol-${r.value.toLowerCase()}`
                const selected = form.rol === r.value
                return (
                  <label
                    key={r.value}
                    htmlFor={id}
                    className={`rol-card ${selected ? 'rol-card-selected' : ''}`}
                  >
                    <input
                      id={id}
                      type="radio"
                      name="rol"
                      value={r.value}
                      checked={selected}
                      onChange={handleChange}
                    />
                    <span className="rol-card-titulo" aria-hidden="true">{r.label}</span>
                    <span className="rol-card-desc">{r.descripcion}</span>
                  </label>
                )
              })}
            </div>
            {errores.rol && <p className="form-error">{errores.rol}</p>}
          </fieldset>

          <button type="submit" className="btn btn-primary btn-block" disabled={enviando}>
            {enviando ? 'Registrando…' : 'Crear cuenta'}
          </button>
        </form>

        <p className="auth-pie">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="auth-link">Inicia sesión</Link>
        </p>
        <p className="auth-pie auth-pie-sm">
          ¿Olvidaste tu contraseña?{' '}
          <Link to="/recuperar-contrasena" className="auth-link">Recupérala</Link>
        </p>
      </div>
    </div>
  )
}
