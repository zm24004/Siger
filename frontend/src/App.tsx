import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import PanelAdmin from './pages/administracion/PanelAdmin';
import Login from './pages/autenticacion/Login';
import RecuperarContrasena from './pages/autenticacion/RecuperarContrasena';
import Registro from './pages/autenticacion/Registro';
import FormularioEvento from './pages/organizador/FormularioEvento';
import PanelOrganizador from './pages/organizador/PanelOrganizador';
import RegistrarAsistencia from './pages/organizador/RegistrarAsistencia';
import Comprobante from './pages/participante/Comprobante';
import MisReservaciones from './pages/participante/MisReservaciones';
import PanelParticipante from './pages/participante/PanelParticipante';
import Bienvenida from './pages/publicas/Bienvenida';
import Catalogo from './pages/publicas/Catalogo';
import EventoDetalle from './pages/publicas/EventoDetalle';
import NotFound from './pages/sistema/NotFound';
import RutaProtegida from './components/RutaProtegida';
import Footer from './components/Footer';

import './styles/auth.css';
import './styles/bienvenida.css';
import './styles/comprobante.css';
import './styles/comprobante.css';
import './styles/evento.css';
import './styles/global.css';
import './styles/navbar.css';
import './styles/panel-additions.css';
import './styles/panel.css';



export default function App() {
  return (
    <div className="app-wrapper">
      <Navbar />
      <main className="app-main">
        <Routes>
          {/* Públicas */}
          <Route path="/" element={<Bienvenida />} />
          <Route path="/login" element={<Login />} />
          <Route path="/registro" element={<Registro />} />
          <Route path="/recuperar-contrasena" element={<RecuperarContrasena />} />
          <Route path="/eventos" element={<Catalogo />} />
          <Route path="/evento/:id" element={<EventoDetalle />} />

          {/* PARTICIPANTE */}
          <Route
            path="/mi-panel"
            element={
              <RutaProtegida roles={['PARTICIPANTE']}>
                <PanelParticipante />
              </RutaProtegida>
            }
          />
          <Route
            path="/mis-reservaciones"
            element={
              <RutaProtegida roles={['PARTICIPANTE']}>
                <MisReservaciones />
              </RutaProtegida>
            }
          />
          <Route
            path="/comprobante/:codigo"
            element={
              <RutaProtegida roles={['PARTICIPANTE']}>
                <Comprobante />
              </RutaProtegida>
            }
          />

          {/* ORGANIZADOR */}
          <Route
            path="/organizador"
            element={
              <RutaProtegida roles={['ORGANIZADOR']}>
                <PanelOrganizador />
              </RutaProtegida>
            }
          />
          <Route
            path="/organizador/eventos"
            element={
              <RutaProtegida roles={['ORGANIZADOR']}>
                <PanelOrganizador />
              </RutaProtegida>
            }
          />
          <Route
            path="/organizador/nuevo-evento"
            element={
              <RutaProtegida roles={['ORGANIZADOR']}>
                <FormularioEvento />
              </RutaProtegida>
            }
          />
          <Route
            path="/organizador/asistencia"
            element={
              <RutaProtegida roles={['ORGANIZADOR']}>
                <RegistrarAsistencia />
              </RutaProtegida>
            }
          />

          {/* ADMINISTRADOR */}
          <Route
            path="/admin"
            element={
              <RutaProtegida roles={['ADMINISTRADOR']}>
                <PanelAdmin />
              </RutaProtegida>
            }
          />

          {/* 404 */}
          <Route path="/404" element={<NotFound />} />
          <Route path="*" element={<Navigate to="/404" replace />} />
        </Routes>
      </main>
      <Footer />
    </div>
  )
}