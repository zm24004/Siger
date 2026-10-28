import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import Footer from './components/Footer.jsx'
import RutaProtegida from './components/RutaProtegida.jsx'

import Bienvenida from './pages/publicas/Bienvenida.jsx'
import Catalogo from './pages/publicas/Catalogo.jsx'
import EventoDetalle from './pages/publicas/EventoDetalle.jsx'

import Login from './pages/autenticacion/Login.jsx'
import Registro from './pages/autenticacion/Registro.jsx'
import RecuperarContrasena from './pages/autenticacion/RecuperarContrasena.jsx'

import PanelParticipante from './pages/participante/PanelParticipante.jsx'
import MisReservaciones from './pages/participante/MisReservaciones.jsx'
import Comprobante from './pages/participante/Comprobante.jsx'

import PanelOrganizador from './pages/organizador/PanelOrganizador.jsx'
import FormularioEvento from './pages/organizador/FormularioEvento.jsx'
import RegistrarAsistencia from './pages/organizador/RegistrarAsistencia.jsx'

import PanelAdmin from './pages/administracion/PanelAdmin.jsx'
import NotFound from './pages/sistema/NotFound.jsx'

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
