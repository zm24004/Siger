import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { useAuth } from './AuthContext.jsx'
import { getNoLeidas } from '../api/notificaciones.js'
import { conectarNotificaciones } from '../api/ws.js'

const NotificacionContext = createContext(null)

/**
 * Provider that:
 *   1. On login, fetches the initial unread notification count via REST.
 *   2. Opens a SockJS + STOMP socket to the backend.
 *   3. On every incoming STOMP frame, bumps the unread counter and stashes
 *      the latest message so consumers (e.g. the Navbar) can toast it.
 *   4. Disconnects cleanly on logout / unmount.
 */
export function NotificacionProvider({ children }) {
  const { usuario } = useAuth()
  const token = usuario?.token || null

  const [noLeidas, setNoLeidas] = useState(0)
  const [ultima, setUltima] = useState(null)
  const [conectado, setConectado] = useState(false)

  useEffect(() => {
    if (!token) {
      // No session → reset everything.
      setNoLeidas(0)
      setUltima(null)
      setConectado(false)
      return
    }

    let controller
    let cancelled = false

    // Initial unread count from the REST endpoint (best-effort).
    getNoLeidas()
      .then((count) => {
        if (!cancelled) setNoLeidas(Number(count) || 0)
      })
      .catch(() => {
        /* silent: the WS will still work, just without an accurate baseline */
      })

    // Open the STOMP socket; on each frame bump the counter and store the msg.
    controller = conectarNotificaciones(
      token,
      (msg) => {
        setNoLeidas((prev) => prev + 1)
        setUltima(msg)
      },
      (connected) => setConectado(connected)
    )

    return () => {
      cancelled = true
      controller?.desconectar()
    }
  }, [token])

  /**
   * Called when the user opens the notifications view (MisReservaciones).
   * Resets the unread counter locally; the REST endpoint handles persistence.
   */
  function marcarLeidasVista() {
    setNoLeidas(0)
  }

  const value = useMemo(
    () => ({ noLeidas, ultima, conectado, marcarLeidasVista }),
    [noLeidas, ultima, conectado]
  )

  return (
    <NotificacionContext.Provider value={value}>
      {children}
    </NotificacionContext.Provider>
  )
}

/**
 * useNotificaciones() → { noLeidas, ultima, conectado, marcarLeidasVista }
 */
export function useNotificaciones() {
  const ctx = useContext(NotificacionContext)
  if (!ctx) {
    throw new Error('useNotificaciones debe usarse dentro de <NotificacionProvider>')
  }
  return ctx
}
