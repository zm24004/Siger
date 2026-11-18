
interface LoaderProps {
  label?: string;
}

export const Loader = ({label = 'Cargando…' }: LoaderProps) =>{
  return (
    <div
      className="loader"
      role="status"
      aria-live="polite"
      aria-label={label}
    >
      <span className="loader-spinner" aria-hidden="true" />
      <span className="loader-text">{label}</span>
    </div>
  );
}

