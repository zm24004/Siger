import { useEffect } from 'react';

interface ModalProps {
  open: boolean;
  onClose: () => void;
  titulo?: string;
  ariaLabel?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
}

/** Simple modal with backdrop.
 * @param props
 * @param props.open - Controla la vibilidad (ocultar/mostrar) el modal
 * @param props.onClose - Funcion a ejecutar cuando se cierra el modal.
 * @param props.titulo - Titulo del modal.
 * @param props.ariaLabel - label para el dialogo.
 * @param props.children - Contenido del modal.
 * @param props.footer - Contenido del footer del modal.
 */
export default function Modal({
  open,
  onClose,
  titulo,
  ariaLabel,
  children,
  footer,
}: ModalProps) {
  // Cerrar con tecla Escape (accesibilidad)
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose?.();
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      className="modal-backdrop"
      onClick={onClose}
      role="presentation"
    >
      <div
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-label={ariaLabel || titulo || 'Diálogo'}
        onClick={(e) => e.stopPropagation()}
      >
        {titulo && (
          <header className="modal-header">
            <h2 className="modal-titulo">{titulo}</h2>
            <button
              type="button"
              className="modal-cerrar"
              onClick={onClose}
              aria-label="Cerrar diálogo"
            >
              ×
            </button>
          </header>
        )}
        <div className="modal-cuerpo">{children}</div>
        {footer && <footer className="modal-footer">{footer}</footer>}
      </div>
    </div>
  );
}


