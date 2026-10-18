import { createContext, useContext, useEffect, useMemo, useState } from 'react'

const AuthContext = createContext(null)

const STORAGE_KEY = 'sg_usuario'
const TOKEN_KEY = 'sg_token'

/**
 * Reads the persisted user (and token) from localStorage synchronously,
 * so we can render the correct routes on first paint.
 */
function leerUsuarioInicial() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw)
    if (!parsed || !parsed.token) return null
    return parsed
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(leerUsuarioInicial)
  const [cargando, setCargando] = useState(true)

  // Verificamos disponibilidad de localStorage para marcar el fin de la carga inicial.
  useEffect(() => {
    try {
      const u = leerUsuarioInicial()
      setUsuario((prev) => prev ?? u)
    } finally {
      setCargando(false)
    }
  }, [])

  /**
   * login(data): recibe la respuesta del backend { token, nombre, apellido, rol, idUsuario }
   * y la guarda en estado y en localStorage.
   */
  function login(data) {
    if (!data || !data.token) {
      throw new Error('Datos de sesión inválidos')
    }
    const payload = {
      token: data.token,
      nombre: data.nombre,
      apellido: data.apellido || '',
      rol: data.rol,
      idUsuario: data.idUsuario
    }
    localStorage.setItem(TOKEN_KEY, payload.token)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
    setUsuario(payload)
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(STORAGE_KEY)
    setUsuario(null)
  }

  const value = useMemo(
    () => ({
      usuario,
      login,
      logout,
      cargando,
      // Helpers prácticos
      isAuthenticated: !!usuario,
      rol: usuario?.rol || null,
      tieneRol: (roles) => (usuario?.rol ? roles.includes(usuario.rol) : false)
    }),
    [usuario, cargando]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  }
  return ctx
}

export const TOKEN_STORAGE_KEY = TOKEN_KEY
export const USER_STORAGE_KEY = STORAGE_KEY
