import { useEffect } from 'react'

/**
 * Modal simple con backdrop.
 * @param {Object} props
 * @param {boolean} props.open
 * @param {() => void} props.onClose
 * @param {string} [props.titulo]
 * @param {string} [props.ariaLabel]
 * @param {React.ReactNode} props.children
 * @param {React.ReactNode} [props.footer]
 */
export default function Modal({ open, onClose, titulo, ariaLabel, children, footer }) {
  // Cerrar con tecla Escape (accesibilidad)
  useEffect(() => {
    if (!open) return
    const onKey = (e) => {
      if (e.key === 'Escape') onClose?.()
    }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [open, onClose])

  if (!open) return null

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
  )
}
