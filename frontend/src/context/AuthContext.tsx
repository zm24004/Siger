import { createContext, useContext, useEffect, useMemo, useState } from 'react';

import type { Rol } from '../interfaces/common.js';
import type { LoginAuthUserResponse } from '../interfaces/api-interface/auth.interface.js';

const AuthContext = createContext<AuthContextValue | null>(null);

const STORAGE_KEY = 'sg_usuario';
const TOKEN_KEY = 'sg_token';

/** Reads the persisted user (and token) from localStorage synchronously,
 *  so we can render the correct routes on first paint. */
const leerUsuarioInicial=(): LoginAuthUserResponse | null => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as LoginAuthUserResponse;
    if (!parsed || !parsed.token) return null;
    return parsed;
  } catch {
    return null;
  }
}

export interface AuthContextValue {
  usuario: LoginAuthUserResponse | null;
  login: (data: LoginAuthUserResponse) => void;
  logout: () => void;
  cargando: boolean;
  isAuthenticated: boolean;
  rol: Rol | null;
  tieneRol: (roles: Rol[]) => boolean;
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [usuario, setUsuario] = useState<LoginAuthUserResponse | null>(leerUsuarioInicial);
  const [cargando, setCargando] = useState(true);

  // Verificamos disponibilidad de localStorage para marcar el fin de la carga inicial.
  useEffect(() => {
    try {
      const u = leerUsuarioInicial();
      setUsuario((prev) => prev ?? u);
    } finally {
      setCargando(false);
    }
  }, []);

  /** login(data): recibe la respuesta del backend { token, nombre, apellido, rol, idUsuario }
   *  y la guarda en estado y en localStorage.
   */
  const login = (data: LoginAuthUserResponse) => {
    if (!data || !data.token) {
      throw new Error('Datos de sesión inválidos');
    }
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    setUsuario(data);
  }

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(STORAGE_KEY);
    setUsuario(null);
  }

  const value = useMemo<AuthContextValue>(
    () => ({
      usuario,
      login,
      logout,
      cargando,
      // Helpers prácticos
      isAuthenticated: !!usuario,
      rol: usuario?.rol || null,
      tieneRol: (roles) => (usuario?.rol ? roles.includes(usuario.rol) : false),
    }),
    [usuario, cargando],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  }
  return ctx;
}

export const TOKEN_STORAGE_KEY = TOKEN_KEY;
export const USER_STORAGE_KEY = STORAGE_KEY;
