import { useState } from "react";

import imageLogo from '../../assets/favicon.svg';
import { useAuth } from "../../context/AuthContext";
import { useNotificaciones } from "../../context/NotificacionContext";
import { Bell, LogOut } from 'lucide-react';
import { Link, NavLink } from "react-router-dom";

const ROL_HOME = {
    PARTICIPANTE: '/mi-panel',
    ORGANIZADOR: '/organizador',
    ADMINISTRADOR: '/admin'
} as const;
type Rol = keyof typeof ROL_HOME; // "PARTICIPANTE" | "ORGANIZADOR" | "ADMINISTRADOR"

export const CustomNavBar = () => {

    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const { logout, isAuthenticated, rol } = useAuth();
    const { noLeidas } = useNotificaciones();
    const panelPath: string = ROL_HOME[rol?.toUpperCase() as Rol] || '/eventos';

    const toggleMobileMenu = () => {
        setIsMobileMenuOpen((prev) => !prev);
    };

    return (
        <>
            <nav className="flex items-center justify-between px-6 md:px-16 lg:px-24 xl:px-32 py-2 border-b border-gray-300 bg-white relative transition-all">
                <div className="flex items-center gap-2">
                    <img
                        className="h-9 w-9"
                        src={imageLogo}
                        alt="dummyLogoColored"
                    />
                    <span className="text-xl font-bold text-(--color-primario-oscuro)">SIGER</span>
                </div>

                {/* Botón hamburguesa - solo móvil */}
                <button
                    aria-label="Menu"
                    id="menu-toggle"
                    className="sm:hidden"
                    onClick={toggleMobileMenu}
                >
                    <svg
                        width="21"
                        height="15"
                        viewBox="0 0 21 15"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <rect width="21" height="1.5" rx=".75" fill="#426287" />
                        <rect x="8" y="6" width="13" height="1.5" rx=".75" fill="#426287" />
                        <rect x="6" y="13" width="15" height="1.5" rx=".75" fill="#426287" />
                    </svg>
                </button>

                {/* Menú móvil */}
                <div
                    id="mobile-menu"
                    className={`${isMobileMenuOpen ? 'flex' : 'hidden'
                        } absolute top-15 left-0 w-full bg-white shadow-md py-4 flex-col items-start gap-2 px-5 text-sm md:hidden z-50`}
                >
                    <a href="#" className="block">
                        Home
                    </a>
                    <a href="#" className="block">
                        About
                    </a>
                    <a href="#" className="block">
                        Contact
                    </a>
                    <button className="cursor-pointer px-6 py-2 mt-2 bg-indigo-500 hover:bg-indigo-600 transition text-white rounded-full text-sm">
                        Login
                    </button>
                </div>


                <div className="hidden sm:flex items-center gap-8">
                    <NavLink to="/eventos-publicos" className={`navbar-link`}>Eventos</NavLink>

                    {
                        isAuthenticated && rol === 'PARTICIPANTE' && (
                            <>
                                <NavLink to="/mi-panel" className="navbar-link">Mi panel</NavLink>
                                <NavLink to="/mis-reservaciones" className="navbar-link">Mis reservas</NavLink>
                            </>
                        )
                    }

                    {
                        isAuthenticated && rol === 'ORGANIZADOR' && (
                            <>
                                <NavLink to="/organizador" className="navbar-link" end>Mi panel</NavLink>
                                <NavLink to="/organizador/nuevo-evento" className="navbar-link">Crear evento</NavLink>
                                <NavLink to="/organizador/asistencia" className="navbar-link">Asistencia</NavLink>
                            </>
                        )
                    }

                    {
                        isAuthenticated && rol === 'ADMINISTRADOR' && (
                            <NavLink to="/admin" className="navbar-link">Admin</NavLink>
                        )
                    }


                    {
                        (isAuthenticated) &&
                        (<>
                            <Link to={panelPath} className="py-4 px-1 relative border-2 border-transparent text-gray-800 rounded-full hover:text-gray-400 focus:outline-none focus:text-gray-500 transition duration-150 ease-in-out" aria-label="Cart">
                                <Bell className="text-(--color-primario-oscuro)" />
                                {
                                    (noLeidas > 0) &&
                                    <span className="absolute inset-0 object-top-right -mr-6">
                                        <div className="inline-flex items-center px-1.5 py-0.5 border-2 border-white rounded-full text-xs font-semibold leading-4 bg-red-500 text-white">
                                            {(noLeidas) > 99 ? "99+" : noLeidas}
                                        </div>
                                    </span>
                                }
                            </Link>
                            <button className=" flex gap-2 cursor-pointer ml-16 px-8 py-2 hover:bg-(--color-primario-claro) transition text-black rounded-xl"
                                onClick={logout}>
                                <LogOut className="w-6 h-6 text-(--color-primario-oscuro)" />
                            </button>
                        </>)
                    }

                    {
                        (!isAuthenticated) &&
                        <div className="flex gap-2">
                            <button className="cursor-pointer px-8 py-2 border border-(--color-primario) hover:bg-(--color-primario-claro) transition text-black rounded-xl">
                                Iniciar Sesión
                            </button>
                            <button className="cursor-pointer px-8 py-2 bg-(--color-primario) hover:bg-(--color-primario-oscuro) transition text-white rounded-xl">
                                Registrarse
                            </button>
                        </div>
                    }
                </div>




            </nav>
        </>
    )
}
