import { useState, useCallback, createContext, useContext, useRef } from 'react';
import { Toast } from '../components/Toast';

export type ToastTipo = 'exito' | 'error' | 'info';

export interface IToast {
  id: number;
  tipo: ToastTipo;
  mensaje: string;
}

interface ToastContextValue {
  exito: (mensaje: string, opts?: { duration?: number }) => number;
  error: (mensaje: string, opts?: { duration?: number }) => number;
  info: (mensaje: string, opts?: { duration?: number }) => number;
  remove: (id: number) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

export const TIPOS: Record<ToastTipo, string> = {
  exito: 'toast toast-exito',
  error: 'toast toast-error',
  info: 'toast toast-info',
};

let idCounter = 0;

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<IToast[]>([]);
  const timers = useRef<Record<number, ReturnType<typeof setTimeout>>>({});

  const remove = useCallback((id: number) => {
    setToasts((list) => list.filter((t) => t.id !== id));
    if (timers.current[id]) {
      clearTimeout(timers.current[id]);
      delete timers.current[id];
    }
  }, []);

  const push = useCallback(
    (tipo: ToastTipo, mensaje: string, opts: { duration?: number } = {}) => {
      const id = ++idCounter;
      const duration = opts.duration ?? 4500;
      const toast: IToast = { id, tipo, mensaje };
      setToasts((list) => [...list, toast]);
      if (duration > 0) {
        timers.current[id] = setTimeout(() => remove(id), duration);
      }
      return id;
    },
    [remove],
  );

  const exito = useCallback(
    (m: string, o?: { duration?: number }) => push('exito', m, o),
    [push],
  );
  const error = useCallback((m: string, o?: { duration?: number }) => push('error', m, o), [push]);
  const info = useCallback((m: string, o?: { duration?: number }) => push('info', m, o), [push]);

  const value = { exito, error, info, remove };

  return (
    <ToastContext.Provider value={value}>
      {children}
      <Toast toastList={toasts} remove={remove}/>
    </ToastContext.Provider>
  );
}

/** useToast() → { exito, error, info, remove } */
export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast debe usarse dentro de <ToastProvider>');
  return ctx;
}
