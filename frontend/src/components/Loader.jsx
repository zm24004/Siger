export default function Loader({ label = 'Cargando…' }) {
  return (
    <div className="loader" role="status" aria-live="polite" aria-label={label}>
      <span className="loader-spinner" aria-hidden="true"></span>
      <span className="loader-text">{label}</span>
    </div>
  )
}
