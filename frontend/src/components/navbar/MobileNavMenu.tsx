import { useState } from "react";
import { Bell, LogOut } from "lucide-react";
import { Link, NavLink } from "react-router-dom";
import type { Rol } from "../../interfaces/common";

interface MobileNavMenuProps {
    isAuthenticated: boolean;
    rol: Rol | null;
    panelPath: string;
    noLeidas: number;
    onLogout: () => void;
}

const linkClassName = "navbar-link block w-full";

export const MobileNavMenu = ({
    isAuthenticated,
    rol,
    panelPath,
    noLeidas,
    onLogout,
}: MobileNavMenuProps) => {
    const [isOpen, setIsOpen] = useState(false);

    const closeMenu = () => setIsOpen(false);
    const handleLogout = () => {
        closeMenu();
        onLogout();
    };

    return (
        <>
            <button
                type="button"
                aria-label={isOpen ? "Cerrar menú" : "Abrir menú"}
                aria-expanded={isOpen}
                aria-controls="mobile-menu"
                id="menu-toggle"
                className="sm:hidden"
                onClick={() => setIsOpen((open) => !open)}
            >
                <svg
                    width="21"
                    height="15"
                    viewBox="0 0 21 15"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                >
                    <rect width="21" height="1.5" rx=".75" fill="#426287" />
                    <rect x="8" y="6" width="13" height="1.5" rx=".75" fill="#426287" />
                    <rect x="6" y="13" width="15" height="1.5" rx=".75" fill="#426287" />
                </svg>
            </button>

            <nav
                id="mobile-menu"
                aria-label="Navegación móvil"
                className={`${isOpen ? "flex" : "hidden"} absolute top-15 left-0 z-50 w-full flex-col items-stretch gap-2 bg-white px-5 py-4 text-sm shadow-md sm:hidden`}
            >
                <NavLink to="/" className={linkClassName} onClick={closeMenu}>
                    Inicio
                </NavLink>
                <NavLink to="/eventos-publicos" className={linkClassName} onClick={closeMenu}>
                    Eventos Populares
                </NavLink>

                {isAuthenticated && rol === "PARTICIPANTE" && (
                    <>
                        <NavLink to="/mi-panel" className={linkClassName} onClick={closeMenu}>
                            Mi panel
                        </NavLink>
                        <NavLink to="/mis-reservaciones" className={linkClassName} onClick={closeMenu}>
                            Mis reservas
                        </NavLink>
                    </>
                )}

                {isAuthenticated && rol === "ORGANIZADOR" && (
                    <>
                        <NavLink to="/organizador" className={linkClassName} end onClick={closeMenu}>
                            Mi panel
                        </NavLink>
                        <NavLink to="/organizador/nuevo-evento" className={linkClassName} onClick={closeMenu}>
                            Crear evento
                        </NavLink>
                        <NavLink to="/organizador/asistencia" className={linkClassName} onClick={closeMenu}>
                            Asistencia
                        </NavLink>
                    </>
                )}

                {isAuthenticated && rol === "ADMINISTRADOR" && (
                    <NavLink to="/admin" className={linkClassName} onClick={closeMenu}>
                        Admin
                    </NavLink>
                )}

                {isAuthenticated ? (
                    <>
                        <Link
                            to={panelPath}
                            aria-label={`Notificaciones${noLeidas > 0 ? `, ${noLeidas} sin leer` : ""}`}
                            className={`${linkClassName} flex items-center gap-2`}
                            onClick={closeMenu}
                        >
                            <Bell className="h-5 w-5 text-(--color-primario-oscuro)" aria-hidden="true" />
                            <span>Notificaciones</span>
                            {noLeidas > 0 && (
                                <span className="inline-flex items-center rounded-full bg-red-500 px-1.5 py-0.5 text-xs font-semibold leading-4 text-white">
                                    {noLeidas > 99 ? "99+" : noLeidas}
                                </span>
                            )}
                        </Link>
                        <button
                            type="button"
                            className={`${linkClassName} flex cursor-pointer items-center gap-2 text-left`}
                            onClick={handleLogout}
                        >
                            <LogOut className="h-5 w-5 text-(--color-primario-oscuro)" aria-hidden="true" />
                            Cerrar sesión
                        </button>
                    </>
                ) : (
                    <div className="flex flex-col gap-2 pt-2">
                        <Link
                            to="/login"
                            className="rounded-xl border border-(--color-primario) px-6 py-2 text-center text-black transition hover:bg-(--color-primario-claro)"
                            onClick={closeMenu}
                        >
                            Iniciar Sesión
                        </Link>
                        <Link
                            to="/registro"
                            className="rounded-xl bg-(--color-primario) px-6 py-2 text-center !text-white transition hover:bg-(--color-primario-oscuro)"
                            onClick={closeMenu}
                        >
                            Registrarse
                        </Link>
                    </div>
                )}
            </nav>
        </>
    );
};
