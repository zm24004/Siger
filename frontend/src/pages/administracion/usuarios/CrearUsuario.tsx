import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { usuarioService, type UsuarioCreateRole } from '../../../api/usuarios';
import { useToast } from '../../../context/ToastContext';
import { PAISES, detectarPais } from '../../../data/paises';

type FormValues = {
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  codigoPais: string;
  contrasena: string;
  confirmar: string;
  rol: UsuarioCreateRole;
};

type FormErrors = Partial<Record<keyof FormValues, string>>;

const ROLES: { value: UsuarioCreateRole; label: string }[] = [
  { value: 'ADM', label: 'Administrador' },
  { value: 'ORG', label: 'Organizador' },
  { value: 'PAR', label: 'Participante' },
];

function emailValido(email: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function tieneMensaje(error: unknown): error is { message: string } {
  return (
    typeof error === 'object' &&
    error !== null &&
    'message' in error &&
    typeof error.message === 'string'
  );
}

export default function CrearUsuario() {
  const navigate = useNavigate();
  const toast = useToast();
  const [form, setForm] = useState<FormValues>({
    nombre: '',
    apellido: '',
    email: '',
    telefono: '',
    codigoPais: '+503',
    contrasena: '',
    confirmar: '',
    rol: 'PAR',
  });
  const [errores, setErrores] = useState<FormErrors>({});
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    const pais = detectarPais();
    setForm((actual) => ({ ...actual, codigoPais: pais.codigo }));
  }, []);

  function handleChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = event.target;
    setForm((actual) => ({ ...actual, [name]: value }));
    setErrores((actual) => ({ ...actual, [name]: undefined }));
  }

  function validar(): boolean {
    const siguientesErrores: FormErrors = {};

    if (!form.nombre.trim()) siguientesErrores.nombre = 'El nombre es obligatorio.';
    else if (form.nombre.trim().length < 3) siguientesErrores.nombre = 'Mínimo 3 caracteres.';
    else if (form.nombre.trim().length > 100) siguientesErrores.nombre = 'Máximo 100 caracteres.';
    if (!form.apellido.trim()) siguientesErrores.apellido = 'El apellido es obligatorio.';
    else if (form.apellido.trim().length < 3) siguientesErrores.apellido = 'Mínimo 3 caracteres.';
    else if (form.apellido.trim().length > 100) siguientesErrores.apellido = 'Máximo 100 caracteres.';
    if (!form.email.trim()) siguientesErrores.email = 'El correo es obligatorio.';
    else if (!emailValido(form.email)) siguientesErrores.email = 'Correo no válido.';
    else if (form.email.trim().length > 150) siguientesErrores.email = 'Máximo 150 caracteres.';
    if (!form.telefono.trim()) siguientesErrores.telefono = 'El teléfono es obligatorio.';
    else if (!/^[0-9]{6,15}$/.test(form.telefono.trim())) {
      siguientesErrores.telefono = 'Escribe solo dígitos (6–15). El código de país va aparte.';
    }
    if (!form.contrasena) siguientesErrores.contrasena = 'La contraseña es obligatoria.';
    else if (form.contrasena.length < 8 || form.contrasena.length > 72) {
      siguientesErrores.contrasena = 'La contraseña debe tener entre 8 y 72 caracteres.';
    }
    if (form.contrasena !== form.confirmar) {
      siguientesErrores.confirmar = 'Las contraseñas no coinciden.';
    }

    setErrores(siguientesErrores);
    return Object.keys(siguientesErrores).length === 0;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!validar()) return;

    setEnviando(true);
    try {
      await usuarioService.crearUsuario({
        nombre: form.nombre.trim(),
        apellido: form.apellido.trim(),
        email: form.email.trim(),
        telefono: form.telefono.trim(),
        codigoPais: form.codigoPais,
        contrasena: form.contrasena,
        rol: form.rol,
      });
      toast.exito('Usuario creado correctamente.');
      navigate('/admin/usuarios', { replace: true });
    } catch (error: unknown) {
      toast.error(
        tieneMensaje(error) ? error.message : 'No se pudo crear el usuario.',
      );
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="auth-wrapper">
      <section className="auth-card auth-card-wide" aria-labelledby="create-user-title">
        <p className="text-sm font-semibold uppercase tracking-wide text-blue-700">Gestión del sistema</p>
        <h1 id="create-user-title" className="auth-titulo mt-1">Agregar usuario</h1>
        <p className="auth-subtitulo">
          Completa los datos de la cuenta y asigna su rol en el sistema.
        </p>

        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          <div className="form-grid-2">
            <div className="form-group">
              <label htmlFor="nuevo-nombre">Nombre <span aria-hidden="true">*</span></label>
              <input
                id="nuevo-nombre"
                name="nombre"
                type="text"
                autoComplete="given-name"
                className={`form-control ${errores.nombre ? 'is-invalid' : ''}`}
                value={form.nombre}
                onChange={handleChange}
                aria-invalid={Boolean(errores.nombre)}
                aria-describedby={errores.nombre ? 'nuevo-error-nombre' : undefined}
                required
              />
              {errores.nombre && <p id="nuevo-error-nombre" className="form-error">{errores.nombre}</p>}
            </div>
            <div className="form-group">
              <label htmlFor="nuevo-apellido">Apellido <span aria-hidden="true">*</span></label>
              <input
                id="nuevo-apellido"
                name="apellido"
                type="text"
                autoComplete="family-name"
                className={`form-control ${errores.apellido ? 'is-invalid' : ''}`}
                value={form.apellido}
                onChange={handleChange}
                aria-invalid={Boolean(errores.apellido)}
                aria-describedby={errores.apellido ? 'nuevo-error-apellido' : undefined}
                required
              />
              {errores.apellido && <p id="nuevo-error-apellido" className="form-error">{errores.apellido}</p>}
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="nuevo-email">Correo electrónico <span aria-hidden="true">*</span></label>
            <input
              id="nuevo-email"
              name="email"
              type="email"
              autoComplete="email"
              className={`form-control ${errores.email ? 'is-invalid' : ''}`}
              value={form.email}
              onChange={handleChange}
              aria-invalid={Boolean(errores.email)}
              aria-describedby={errores.email ? 'nuevo-error-email' : undefined}
              placeholder="usuario@ejemplo.com"
              required
            />
            {errores.email && <p id="nuevo-error-email" className="form-error">{errores.email}</p>}
          </div>

          <div className="form-group">
            <label htmlFor="nuevo-telefono">Teléfono <span aria-hidden="true">*</span></label>
            <div className="form-row-phone">
              <select
                id="nuevo-codigo-pais"
                name="codigoPais"
                className="form-control form-control-pais"
                value={form.codigoPais}
                onChange={handleChange}
                aria-label="Código de país"
              >
                {PAISES.map((pais) => (
                  <option key={pais.iso} value={pais.codigo}>
                    {pais.bandera} {pais.codigo} {pais.nombre}
                  </option>
                ))}
              </select>
              <input
                id="nuevo-telefono"
                name="telefono"
                type="tel"
                inputMode="numeric"
                autoComplete="tel-national"
                className={`form-control form-control-tel ${errores.telefono ? 'is-invalid' : ''}`}
                value={form.telefono}
                onChange={handleChange}
                aria-invalid={Boolean(errores.telefono)}
                aria-describedby={errores.telefono ? 'nuevo-error-telefono' : undefined}
                required
              />
            </div>
            {errores.telefono && <p id="nuevo-error-telefono" className="form-error">{errores.telefono}</p>}
          </div>

          <div className="form-grid-2">
            <div className="form-group">
              <label htmlFor="nuevo-contrasena">Contraseña inicial <span aria-hidden="true">*</span></label>
              <input
                id="nuevo-contrasena"
                name="contrasena"
                type="password"
                autoComplete="new-password"
                className={`form-control ${errores.contrasena ? 'is-invalid' : ''}`}
                value={form.contrasena}
                onChange={handleChange}
                aria-invalid={Boolean(errores.contrasena)}
                aria-describedby={errores.contrasena ? 'nuevo-error-contrasena' : undefined}
                minLength={8}
                maxLength={72}
                required
              />
              {errores.contrasena && <p id="nuevo-error-contrasena" className="form-error">{errores.contrasena}</p>}
            </div>
            <div className="form-group">
              <label htmlFor="nuevo-confirmar">Confirmar contraseña <span aria-hidden="true">*</span></label>
              <input
                id="nuevo-confirmar"
                name="confirmar"
                type="password"
                autoComplete="new-password"
                className={`form-control ${errores.confirmar ? 'is-invalid' : ''}`}
                value={form.confirmar}
                onChange={handleChange}
                aria-invalid={Boolean(errores.confirmar)}
                aria-describedby={errores.confirmar ? 'nuevo-error-confirmar' : undefined}
                required
              />
              {errores.confirmar && <p id="nuevo-error-confirmar" className="form-error">{errores.confirmar}</p>}
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="nuevo-rol">Rol <span aria-hidden="true">*</span></label>
            <select
              id="nuevo-rol"
              name="rol"
              className={`form-control ${errores.rol ? 'is-invalid' : ''}`}
              value={form.rol}
              onChange={handleChange}
              aria-invalid={Boolean(errores.rol)}
              required
            >
              {ROLES.map((rol) => (
                <option key={rol.value} value={rol.value}>{rol.label}</option>
              ))}
            </select>
            {errores.rol && <p className="form-error">{errores.rol}</p>}
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={enviando}>
            {enviando ? 'Creando usuario…' : 'Crear usuario'}
          </button>
        </form>

        <p className="auth-pie">
          <Link to="/admin/usuarios" className="auth-link">Volver a administración de usuarios</Link>
        </p>
      </section>
    </div>
  );
}
