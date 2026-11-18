import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { useAuth } from './AuthContext';
import type {
  Notificacion,
  NotificacionCursor,
} from '../interfaces/api-interface/notificaciones.interface';
import { notificacionService } from '../api/notificaciones';
import conectarNotificaciones from '../api/web-socket';

const TAMANO_PAGINA = 20;
const INTERVALO_SINCRONIZACION_MS = 30_000;

interface NotificacionContextValue {
  notificaciones: Notificacion[];
  noLeidas: number;
  ultima: Notificacion | null;
  conectado: boolean;
  hayMas: boolean;
  cargandoNotificaciones: boolean;
  errorNotificaciones: string | null;
  cargarMas: () => Promise<void>;
  refrescarNotificaciones: () => Promise<void>;
  marcarLeida: (id: number) => Promise<void>;
}

const NotificacionContext = createContext<NotificacionContextValue | null>(null);

function combinarNotificaciones(
  actuales: Notificacion[],
  nuevas: Notificacion[],
): Notificacion[] {
  const porId = new Map<number, Notificacion>();
  for (const notificacion of actuales) porId.set(notificacion.idNotificacion, notificacion);
  for (const notificacion of nuevas) {
    const anterior = porId.get(notificacion.idNotificacion);
    porId.set(
      notificacion.idNotificacion,
      anterior?.leido && !notificacion.leido
        ? { ...notificacion, leido: true }
        : notificacion,
    );
  }

  return [...porId.values()].sort((a, b) => {
    const fechaA = new Date(String(a.fechaEnvio)).getTime();
    const fechaB = new Date(String(b.fechaEnvio)).getTime();
    if (fechaA !== fechaB) return fechaB - fechaA;
    return b.idNotificacion - a.idNotificacion;
  });
}

function mensajeError(error: unknown): string {
  return error instanceof Error ? error.message : 'No se pudieron cargar las notificaciones.';
}

export const NotificacionProvider = ({ children }: { children: React.ReactNode }) => {
  const { usuario } = useAuth();
  const token = usuario?.token || null;
  const esParticipante = usuario?.rol === 'PARTICIPANTE';

  const [notificaciones, setNotificaciones] = useState<Notificacion[]>([]);
  const [noLeidas, setNoLeidas] = useState(0);
  const [ultima, setUltima] = useState<Notificacion | null>(null);
  const [conectado, setConectado] = useState(false);
  const [hayMas, setHayMas] = useState(false);
  const [cargandoNotificaciones, setCargandoNotificaciones] = useState(false);
  const [errorNotificaciones, setErrorNotificaciones] = useState<string | null>(null);

  const notificacionesRef = useRef<Notificacion[]>([]);
  const cursorRef = useRef<NotificacionCursor | null>(null);
  const hayMasRef = useRef(false);
  const cargaEnCursoRef = useRef(false);
  const refrescoPendienteRef = useRef(false);
  const revisionRef = useRef(0);
  const cambiosRef = useRef(new Map<number, { revision: number; notificacion: Notificacion }>());
  const revisionNoLeidasRef = useRef(0);
  const sincronizandoNoLeidasRef = useRef(false);
  const sincronizacionPendienteRef = useRef(false);
  const sincronizacionSesionRef = useRef<number | null>(null);
  const sincronizacionEjecucionRef = useRef(0);
  const sesionRef = useRef(0);
  const conectadoAntesRef = useRef(false);

  const publicarNotificaciones = useCallback((lista: Notificacion[]) => {
    notificacionesRef.current = lista;
    setNotificaciones(lista);
  }, []);

  const aplicarNotificacion = useCallback((notificacion: Notificacion) => {
    const actual = notificacionesRef.current.find(
      (item) => item.idNotificacion === notificacion.idNotificacion,
    );
    const actualizadas = combinarNotificaciones(notificacionesRef.current, [notificacion]);
    publicarNotificaciones(actualizadas);

    revisionRef.current += 1;
    if (cargaEnCursoRef.current) {
      cambiosRef.current.set(notificacion.idNotificacion, {
        revision: revisionRef.current,
        notificacion,
      });
    }

    return actual;
  }, [publicarNotificaciones]);

  const sincronizarNoLeidas = useCallback(async (sesion: number) => {
    sincronizacionPendienteRef.current = true;
    if (sincronizandoNoLeidasRef.current && sincronizacionSesionRef.current === sesion) return;

    sincronizandoNoLeidasRef.current = true;
    sincronizacionSesionRef.current = sesion;
    const ejecucion = ++sincronizacionEjecucionRef.current;
    try {
      while (sincronizacionPendienteRef.current && sesionRef.current === sesion) {
        sincronizacionPendienteRef.current = false;
        const revisionInicial = revisionNoLeidasRef.current;
        const count = await notificacionService.getNoLeidas();
        if (sesionRef.current !== sesion) return;

        if (revisionNoLeidasRef.current === revisionInicial) {
          setNoLeidas(count);
        } else {
          sincronizacionPendienteRef.current = true;
        }
      }
    } finally {
      if (sincronizacionEjecucionRef.current === ejecucion) {
        sincronizandoNoLeidasRef.current = false;
        sincronizacionSesionRef.current = null;
      }
      if (
        sincronizacionEjecucionRef.current === ejecucion
        && sincronizacionPendienteRef.current
        && sesionRef.current === sesion
      ) {
        void sincronizarNoLeidas(sesion);
      }
    }
  }, []);

  const ajustarNoLeidas = useCallback((delta: number) => {
    revisionNoLeidasRef.current += 1;
    setNoLeidas((actual) => Math.max(0, actual + delta));
    if (sincronizandoNoLeidasRef.current) sincronizacionPendienteRef.current = true;
  }, []);

  const cargarPagina = useCallback(async (sesion: number, cursor: NotificacionCursor | null) => {
    if (sesionRef.current !== sesion) return;
    if (cargaEnCursoRef.current) {
      if (cursor === null) refrescoPendienteRef.current = true;
      return;
    }
    cargaEnCursoRef.current = true;
    setCargandoNotificaciones(true);
    setErrorNotificaciones(null);
    const revisionInicial = revisionRef.current;

    try {
      const pagina = await notificacionService.getPagina(cursor, TAMANO_PAGINA);
      if (sesionRef.current !== sesion) return;

      const cambiosDuranteCarga = [...cambiosRef.current.values()]
        .filter(({ revision }) => revision > revisionInicial)
        .map(({ notificacion }) => notificacion);
      publicarNotificaciones(
        combinarNotificaciones(
          notificacionesRef.current,
          [...pagina.content, ...cambiosDuranteCarga],
        ),
      );
      for (const [id, cambio] of cambiosRef.current) {
        if (cambio.revision <= revisionRef.current) cambiosRef.current.delete(id);
      }
      if (cursor !== null || cursorRef.current === null) {
        cursorRef.current = pagina.nextCursor;
      }
      hayMasRef.current = pagina.hasMore;
      setHayMas(pagina.hasMore);
    } catch (error) {
      if (sesionRef.current === sesion) {
        setErrorNotificaciones(mensajeError(error));
      }
      throw error;
    } finally {
      if (sesionRef.current === sesion) {
        setCargandoNotificaciones(false);
        cargaEnCursoRef.current = false;
        if (refrescoPendienteRef.current) {
          refrescoPendienteRef.current = false;
          void cargarPagina(sesion, null).catch((error: unknown) => {
            console.error('No se pudo completar la resincronización de notificaciones', error);
          });
        }
      }
    }
  }, [publicarNotificaciones]);

  const cargarMas = useCallback(async () => {
    const sesion = sesionRef.current;
    if (!token || !esParticipante || !hayMasRef.current || cargaEnCursoRef.current) return;
    await cargarPagina(sesion, cursorRef.current);
  }, [cargarPagina, esParticipante, token]);

  const refrescarNotificaciones = useCallback(async () => {
    const sesion = sesionRef.current;
    if (!token || !esParticipante || cargaEnCursoRef.current) return;
    await cargarPagina(sesion, null);
  }, [cargarPagina, esParticipante, token]);

  const marcarLeida = useCallback(async (id: number) => {
    const sesion = sesionRef.current;
    const actualizada = await notificacionService.marcarLeida(id);
    if (sesionRef.current !== sesion) return;

    const anterior = aplicarNotificacion(actualizada);
    if (anterior && !anterior.leido && actualizada.leido) ajustarNoLeidas(-1);
  }, [aplicarNotificacion, ajustarNoLeidas]);

  useEffect(() => {
    const sesion = sesionRef.current + 1;
    sesionRef.current = sesion;
    conectadoAntesRef.current = false;
    notificacionesRef.current = [];
    cursorRef.current = null;
    hayMasRef.current = Boolean(token && esParticipante);
    cargaEnCursoRef.current = false;
    refrescoPendienteRef.current = false;
    revisionRef.current = 0;
    cambiosRef.current.clear();
    revisionNoLeidasRef.current = 0;
    sincronizacionPendienteRef.current = false;
    sincronizacionSesionRef.current = null;
    sincronizacionEjecucionRef.current += 1;
    sincronizandoNoLeidasRef.current = false;
    setNotificaciones([]);
    setNoLeidas(0);
    setUltima(null);
    setConectado(false);
    setHayMas(Boolean(token && esParticipante));
    setCargandoNotificaciones(false);
    setErrorNotificaciones(null);

    if (!token) {
      return () => {
        if (sesionRef.current === sesion) sesionRef.current += 1;
      };
    }

    if (esParticipante) {
      void cargarPagina(sesion, null).catch((error: unknown) => {
        console.error('No se pudo cargar la primera página de notificaciones', error);
      });
    }
    void sincronizarNoLeidas(sesion).catch((error: unknown) => {
      if (sesionRef.current === sesion) {
        console.error('No se pudo cargar el contador global de notificaciones', error);
      }
    });

    const controller = conectarNotificaciones(
      token,
      (notificacion) => {
        if (sesionRef.current !== sesion) return;
        const actual = aplicarNotificacion(notificacion);
        if (!actual) {
          setUltima(notificacion);
          if (!notificacion.leido) ajustarNoLeidas(1);
        }
      },
      (estaConectado) => {
        if (sesionRef.current !== sesion) return;
        setConectado(estaConectado);
        if (estaConectado && conectadoAntesRef.current) {
          if (esParticipante) {
            void cargarPagina(sesion, null).catch((error: unknown) => {
              console.error('No se pudieron actualizar notificaciones tras reconectar', error);
            });
          }
          void sincronizarNoLeidas(sesion).catch((error: unknown) => {
            console.error('No se pudo sincronizar el contador tras reconectar', error);
          });
        }
        if (estaConectado) conectadoAntesRef.current = true;
      },
    );
    const intervaloSincronizacion = window.setInterval(() => {
      if (esParticipante) {
        void cargarPagina(sesion, null).catch((error: unknown) => {
          console.error('No se pudieron resincronizar las notificaciones', error);
        });
      }
      void sincronizarNoLeidas(sesion).catch((error: unknown) => {
        console.error('No se pudo resincronizar el contador de notificaciones', error);
      });
    }, INTERVALO_SINCRONIZACION_MS);

    return () => {
      if (sesionRef.current === sesion) sesionRef.current += 1;
      window.clearInterval(intervaloSincronizacion);
      controller.desconectar();
    };
  }, [token, esParticipante, aplicarNotificacion, ajustarNoLeidas, cargarPagina, sincronizarNoLeidas]);

  const value = useMemo<NotificacionContextValue>(
    () => ({
      notificaciones,
      noLeidas,
      ultima,
      conectado,
      hayMas,
      cargandoNotificaciones,
      errorNotificaciones,
      cargarMas,
      refrescarNotificaciones,
      marcarLeida,
    }),
    [
      notificaciones,
      noLeidas,
      ultima,
      conectado,
      hayMas,
      cargandoNotificaciones,
      errorNotificaciones,
      cargarMas,
      refrescarNotificaciones,
      marcarLeida,
    ],
  );

  return (
    <NotificacionContext.Provider value={value}>
      {children}
    </NotificacionContext.Provider>
  );
};

export function useNotificaciones(): NotificacionContextValue {
  const ctx = useContext(NotificacionContext);
  if (!ctx) {
    throw new Error('useNotificaciones debe usarse dentro de <NotificacionProvider>');
  }
  return ctx;
}
