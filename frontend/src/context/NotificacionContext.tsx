import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { useAuth } from './AuthContext';
import type { Notificacion } from '../interfaces/api-interface/notificaciones.interface';
import { notificacionService } from '../api/notificaciones';
import conectarNotificaciones from '../api/web-socket';

interface NotificacionContextValue {
  noLeidas: number;
  ultima: Notificacion | null;
  conectado: boolean;
  marcarLeidasVista: () => void;
}

const NotificacionContext = createContext<NotificacionContextValue | null>(null);

/**
 * Provider that:
 *   1. On login, fetches the initial unread notification count via REST.
 *   2. Opens a SockJS + STOMP socket to the backend.
 *   3. On every incoming STOMP frame, bumps the unread counter and stashes
 *      the latest message so consumers (e.g. the Navbar) can toast it.
 *   4. Disconnects cleanly on logout / unmount.
 */
export const NotificacionProvider = ({ children }: { children: React.ReactNode; }) => {
  const { usuario } = useAuth();
  const token = usuario?.token || null;

  const [noLeidas, setNoLeidas] = useState(0);
  const [ultima, setUltima] = useState<Notificacion | null>(null);
  const [conectado, setConectado] = useState(false);

  useEffect(() => {
    if (!token) {
      // No session → reset everything.
      setNoLeidas(0);
      setUltima(null);
      setConectado(false);
      return;
    }

    let controller: { desconectar: () => void } | undefined;
    let cancelled = false;

    // Initial unread count from the REST endpoint (best-effort).


    notificacionService.getNoLeidas()
      .then((count) => {
        if (!cancelled) { 
          setNoLeidas(count);
        }
      })
      .catch(() => {
        /* silent: the WS will still work, just without an accurate baseline */
        setNoLeidas(0);
      });

    // Open the STOMP socket; on each frame bump the counter and store the msg.
    controller = conectarNotificaciones(
      token,
      (msg) => {
        setNoLeidas((prev) => prev + 1);
        setUltima(msg as unknown as Notificacion);
      },
      (connected) => setConectado(connected),
    );

    return () => {
      cancelled = true;
      controller?.desconectar();
    };
  }, [token]);

  /** Called when the user opens the notifications view (MisReservaciones).
   *  Resets the unread counter locally; the REST endpoint handles persistence.
   */
  const marcarLeidasVista = () => {
    setNoLeidas(0);
  }

  const value = useMemo<NotificacionContextValue>(
    () => ({ noLeidas, ultima, conectado, marcarLeidasVista }),
    [noLeidas, ultima, conectado],
  );

  return (
    <NotificacionContext.Provider value={value}>
      {children}
    </NotificacionContext.Provider>
  );
}

/** useNotificaciones() → { noLeidas, ultima, conectado, marcarLeidasVista } */
export function useNotificaciones(): NotificacionContextValue {
  const ctx = useContext(NotificacionContext);
  if (!ctx) {
    throw new Error('useNotificaciones debe usarse dentro de <NotificacionProvider>');
  }
  return ctx;
}
