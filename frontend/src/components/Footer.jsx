export default function Footer() {
  const year = new Date().getFullYear();
  return (
    <footer className="footer" role="contentinfo">
      <div className="footer-contenedor">
        <p className="footer-texto">
          © {year} SIGER — Universidad De El Salvador. Projecto Sistema de
          Gestión de Reservacion de Eventos.
        </p>
        <p className="footer-subtexto">
          Proyecto ADS GT-1 - Grupo 10, Hecho con React + Vite + Springboot +
          Java + CS3.
        </p>
      </div>
    </footer>
  );
}
