import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

/**
 * Builds the SockJS WebSocket URL from the same env-var pattern used by
 * http.js (VITE_API_URL || VITE_API_BASE_URL || '/api').
 * We strip the trailing '/api' segment and append '/ws-notificaciones',
 * resolving relative bases against the current origin so it stays same-origin.
 *
 * SockJS expects an HTTP(S) URL — it handles the upgrade negotiation itself.
 */
function buildWsUrl() {
  const base = import.meta.env.VITE_API_URL
    || import.meta.env.VITE_API_BASE_URL
    || '/api'
  // Strip everything after (and including) the last '/api' segment.
  const trimmed = base.replace(/\/api\/?$/, '')
  // SockJS requires an absolute URL. If the base is relative (e.g. '/api'),
  // resolve it against the current origin so nginx/Vite can proxy it.
  const wsBase = /^https?:\/\//i.test(trimmed)
    ? trimmed
    : `${window.location.origin}${trimmed}`
  return `${wsBase}/ws-notificaciones`
}

/**
 * Opens a SockJS + STOMP connection to receive user-scoped notifications.
 *
 * @param {string} token       JWT bearer token (sent as Authorization header on CONNECT).
 * @param {(msg: object) => void} onMensaje     Called when a notification frame arrives.
 * @param {(connected: boolean) => void} [onConnectChange] Called on connect/disconnect.
 * @returns {{ desconectar: () => void }} Controller to deactivate the client.
 */
export function conectarNotificaciones(token, onMensaje, onConnectChange) {
  const wsUrl = buildWsUrl()

  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      onConnectChange?.(true)
      client.subscribe('/user/queue/notificaciones', (msg) => {
        try {
          onMensaje(JSON.parse(msg.body))
        } catch {
          /* ignore malformed frames */
        }
      })
    },
    onDisconnect: () => onConnectChange?.(false),
    onStompError: (frame) => console.error('STOMP error', frame),
    onWebSocketError: (e) => console.error('WS error', e)
  })

  client.activate()

  return {
    desconectar: () => client.deactivate()
  }
}

export default conectarNotificaciones
