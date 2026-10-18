import './styles/global.css'
import './styles/auth.css'
import './styles/bienvenida.css'
import './styles/navbar.css'
import './styles/evento.css'
import './styles/comprobante.css'
import './styles/panel.css'
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import { NotificacionProvider } from './context/NotificacionContext.jsx'
import { ToastProvider } from './components/Toast.jsx'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <NotificacionProvider>
            <App />
          </NotificacionProvider>
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  </React.StrictMode>
)
