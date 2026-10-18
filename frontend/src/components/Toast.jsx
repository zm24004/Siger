import { createContext, useCallback, useContext, useRef, useState } from 'react'

const ToastContext = createContext(null)

let idCounter = 0
const TIPOS = {
  exito: 'toast toast-exito',
  error: 'toast toast-error',
  info: 'toast toast-info'
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])
  const timers = useRef({})

  const remove = useCallback((id) => {
    setToasts((list) => list.filter((t) => t.id !== id))
    if (timers.current[id]) {
      clearTimeout(timers.current[id])
      delete timers.current[id]
    }
  }, [])

  const push = useCallback(
    (tipo, mensaje, opts = {}) => {
      const id = ++idCounter
      const duration = opts.duration ?? 4500
      const toast = { id, tipo, mensaje }
      setToasts((list) => [...list, toast])
      if (duration > 0) {
        timers.current[id] = setTimeout(() => remove(id), duration)
      }
      return id
    },
    [remove]
  )

  const exito = useCallback((m, o) => push('exito', m, o), [push])
  const error = useCallback((m, o) => push('error', m, o), [push])
  const info = useCallback((m, o) => push('info', m, o), [push])

  const value = { exito, error, info, remove }

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-container" role="region" aria-live="polite" aria-label="Notificaciones">
        {toasts.map((t) => (
          <div key={t.id} className={TIPOS[t.tipo] || TIPOS.info} role="status">
            <span className="toast-mensaje">{t.mensaje}</span>
            <button
              type="button"
              className="toast-cerrar"
              onClick={() => remove(t.id)}
              aria-label="Cerrar notificación"
            >
              ×
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

/**
 * useToast() → { exito, error, info, remove }
 */
export function useToast() {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast debe usarse dentro de <ToastProvider>')
  return ctx
}
