import { useEffect, useState } from 'react';
import {
  CalendarDays,
  ChevronDown,
  ClipboardCheck,
  LayoutDashboard,
  LogOut,
  Menu,
  Plus,
  Settings,
  Users,
  X,
} from 'lucide-react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Brand from './Brand';

const enlacesPorRol = {
  ADMINISTRADOR: [
    { to: '/admin', label: 'Panel principal', icon: LayoutDashboard, end: true },
  ],
  ORGANIZADOR: [
    { to: '/organizador', label: 'Panel principal', icon: LayoutDashboard, end: true },
    { to: '/organizador/eventos', label: 'Mis eventos', icon: CalendarDays, end: false },
    { to: '/organizador/nuevo-evento', label: 'Crear evento', icon: Plus, end: false },
    { to: '/organizador/asistencia', label: 'Registrar asistencia', icon: ClipboardCheck, end: false },
  ],
} as const;

export default function Sidebar() {
  const { isAuthenticated, rol, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const [gestionAbierta, setGestionAbierta] = useState(true);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  useEffect(() => {
    setIsOpen(false);
    if (location.pathname.startsWith('/admin/')) {
      setGestionAbierta(true);
    }
  }, [location.pathname, location.search]);

  if (!isAuthenticated || (rol !== 'ADMINISTRADOR' && rol !== 'ORGANIZADOR')) {
    return null;
  }

  const enlaces = enlacesPorRol[rol];

  return (
    <div className="order-2 shrink-0 md:order-none md:col-start-1 md:row-span-3 md:row-start-1 md:flex md:self-stretch md:w-60">
      <button
        className="mx-4 mt-3 inline-flex items-center gap-2 rounded-md border border-(--color-primario) bg-(--color-primario-oscuro) px-3 py-2 font-semibold text-white shadow-sm hover:bg-(--color-primario) md:hidden"
        type="button"
        aria-label={isOpen ? 'Cerrar menú de navegación' : 'Abrir menú de navegación'}
        aria-controls="app-sidebar"
        aria-expanded={isOpen}
        onClick={() => setIsOpen((open) => !open)}
      >
        {isOpen ? <X size={20} aria-hidden="true" /> : <Menu size={20} aria-hidden="true" />}
        <span>Menú</span>
      </button>

      {isOpen && (
        <button
          className="fixed inset-0 z-[900] h-full w-full border-0 bg-slate-900/45 md:hidden"
          type="button"
          aria-label="Cerrar menú de navegación"
          onClick={() => setIsOpen(false)}
        />
      )}

      <aside
        id="app-sidebar"
        className={`${
          isOpen
            ? 'fixed inset-y-0 left-0 z-[901] flex h-dvh w-[min(280px,85vw)]'
            : 'hidden'
        } flex max-h-dvh flex-col overflow-hidden border-r border-(--color-primario) bg-(--color-primario-oscuro) px-3 text-white shadow-xl md:fixed md:top-0 md:right-auto md:bottom-0 md:left-0 md:z-20 md:flex md:h-dvh md:min-h-0 md:w-60 md:max-h-none md:shrink-0 md:shadow-none`}
        aria-label="Navegación de administración y organización"
      >
        <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain pt-[calc(var(--nav-h)+1.25rem)]">
        <div className="border-b border-white/20 px-2 pb-5">
          <Brand tone="dark" />
        </div>
        <div className="flex items-center justify-between border-b border-white/20 px-2 pb-4 pt-4">
          <span className="text-xs font-bold uppercase tracking-widest text-white/80">
            {rol === 'ADMINISTRADOR' ? 'Administración' : 'Organización'}
          </span>
          <button
            className="inline-flex h-9 w-9 items-center justify-center rounded-md text-white/80 hover:bg-white/10 hover:text-white focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white md:hidden"
            type="button"
            aria-label="Cerrar menú de navegación"
            onClick={() => setIsOpen(false)}
          >
            <X size={20} aria-hidden="true" />
          </button>
        </div>
        <nav className="grid gap-1 pt-3">
          {rol === 'ADMINISTRADOR' ? (
            <>
              {enlaces.map(({ to, label, icon: Icon, end }) => (
                <NavLink
                  key={to}
                  to={to}
                  end={end}
                  className={({ isActive }) =>
                    `flex items-center gap-3 rounded-lg px-3 py-3 text-sm font-medium transition-colors ${
                      isActive
                    ? 'bg-(--color-primario) font-semibold !text-white shadow-sm'
                    : '!text-white/75 hover:bg-white/10 hover:!text-white'
                    }`
                  }
                >
                  <Icon size={19} aria-hidden="true" />
                  <span>{label}</span>
                </NavLink>
              ))}
              <button
                type="button"
                className={`flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm font-medium transition-colors ${
                  location.pathname.startsWith('/admin/')
                    ? 'bg-(--color-primario) font-semibold text-white shadow-sm'
                    : 'text-white/60 hover:bg-white/10 hover:text-white'
                }`}
                aria-expanded={gestionAbierta}
                aria-controls="admin-system-submenu"
                onClick={() => setGestionAbierta((abierta) => !abierta)}
              >
                <Settings size={19} aria-hidden="true" />
                <span className="flex-1">Gestión del sistema</span>
                <ChevronDown
                  size={16}
                  aria-hidden="true"
                  className={`transition-transform ${gestionAbierta ? 'rotate-180' : ''}`}
                />
              </button>
              {gestionAbierta && (
                <div id="admin-system-submenu" className="ml-5 grid gap-1 border-l border-white/20 pl-3">
                  <NavLink
                    to="/admin/usuarios"
                    className={({ isActive }) =>
                      `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                        isActive
                          ? 'bg-(--color-primario) font-semibold !text-white shadow-sm'
                          : '!text-white/70 hover:bg-white/10 hover:!text-white'
                      }`
                    }
                  >
                    <Users size={17} aria-hidden="true" />
                    <span>Administración de usuarios</span>
                  </NavLink>
                  <NavLink
                    to="/admin/eventos"
                    className={({ isActive }) =>
                      `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                        isActive
                          ? 'bg-(--color-primario) font-semibold !text-white shadow-sm'
                          : '!text-white/70 hover:bg-white/10 hover:!text-white'
                      }`
                    }
                  >
                    <CalendarDays size={17} aria-hidden="true" />
                    <span>Eventos</span>
                  </NavLink>
                </div>
              )}
            </>
          ) : (
            enlaces.map(({ to, label, icon: Icon, end }) => (
              <NavLink
                key={to}
                to={to}
                end={end}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-lg px-3 py-3 text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-(--color-primario) font-semibold !text-white shadow-sm'
                      : '!text-white/75 hover:bg-white/10 hover:!text-white'
                  }`
                }
              >
                <Icon size={19} aria-hidden="true" />
                <span>{label}</span>
              </NavLink>
            ))
          )}
        </nav>
        </div>
        <div className="shrink-0 border-t border-white/20 py-3">
          <button
            type="button"
            className="flex w-full items-center gap-3 rounded-lg px-3 py-3 text-sm font-medium text-white/80 transition-colors hover:bg-white/10 hover:text-white focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white"
            aria-label="Cerrar sesión"
            onClick={handleLogout}
          >
            <LogOut size={19} aria-hidden="true" />
            <span>Cerrar sesión</span>
          </button>
        </div>
      </aside>
    </div>
  );
}
