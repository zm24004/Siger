export default function Footer() {
  const year = new Date().getFullYear();
  return (
    <footer className="footer" role="contentinfo">
      <div className="footer-contenedor">
        <p className="footer-texto">
          © {year} SIGER — Universidad De El Salvador. Projecto Sistema de
          Gestión de Reservacion de Eventos.
        </p>
      </div>
    </footer>
  );
}
