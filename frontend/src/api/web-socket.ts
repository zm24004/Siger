import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

/** Builds the SockJS URL below the same versioned API base used by HTTP.
 *  Relative bases resolve against the current origin so Docker traffic stays same-origin.
 *
 *  SockJS expects an HTTP(S) URL — it handles the upgrade negotiation itself.
 */
const buildWsUrl = (): string => {
  const base = import.meta.env.VITE_API_URL || import.meta.env.VITE_API_BASE_URL || '/api/v1';
  const normalizedBase = base.replace(/\/+$/, '');
  const wsBase = /^https?:\/\//i.test(normalizedBase)
    ? normalizedBase
    : `${window.location.origin}${normalizedBase}`;
  return `${wsBase}/ws-notificaciones`;
}

/** Opens a SockJS + STOMP connection to receive user-scoped notifications.
 *
 * @param token - JWT bearer token (sent as Authorization header on CONNECT).
 * @param onMensaje - Called when a notification frame arrives.
 * @param onConnectChange - Called on connect/disconnect.
 * @returns Controller with `desconectar()` so the client can be deactivated.
 */
const conectarNotificaciones = (
  token: string,
  onMensaje: (msg: Notification) => void,
  onConnectChange?: (connected: boolean) => void,
) => {

  if(!token) return { desconectar: () => {} };

  const wsUrl = buildWsUrl();

  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      onConnectChange?.(true);
      client.subscribe('/user/queue/notificaciones', (msg) => {
        try {
          onMensaje(JSON.parse(msg.body) as Notification);
        } catch {
          /* ignore malformed frames */
        }
      });
    },
    onDisconnect: () => onConnectChange?.(false),
    onStompError: (frame) => console.error('STOMP error', frame),
    onWebSocketError: (e) => console.error('WS error', e),
  });

  client.activate();

  return {
    desconectar: () => client.deactivate(),
  };
}

export default conectarNotificaciones;
