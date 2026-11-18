import { TIPOS, type IToast } from '../context/ToastContext'

interface ToastProps {
  toastList: IToast[];
  remove: (id: number) => void;
}

export const Toast = ({toastList, remove}: ToastProps) => {
  return (
    <>
      <div
        className="toast-container"
        role="region"
        aria-live="polite"
        aria-label="Notificaciones"
      >
        {toastList.map((t) => (
          <div
            key={t.id}
            className={TIPOS[t.tipo] || TIPOS.info}
            role="status"
          >
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
    </ >
  )
}
