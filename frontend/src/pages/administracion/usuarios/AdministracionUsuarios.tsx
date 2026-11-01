import { useEffect, useState } from 'react';
import { usuarioService } from '../../../api/usuarios';
import type { USER_FILER_TYPE, Usuario } from '../../../interfaces/api-interface/usuario.interface';
import { Loader } from '../../../components/Loader';
import * as fechaUtils from '../../../utils/date-utils'
import { useToast } from '../../../context/ToastContext';
import { Table } from '../../../components/untitledui/application/table/table';
import { PaginationPageDefault } from '../../../components/untitledui/application/pagination/pagination';
import type { Paginado } from '../../../interfaces/common';

const ITEMS_PER_PAGE = 10;

function estadoActivo(estado: boolean | string | null | undefined): boolean {
  if (estado === true) return true;
  if (estado === false) return false;
  if (typeof estado === "string") {
    const v = estado.trim().toUpperCase();
    return v === "ACTIVO" || v === "TRUE" || v === "1";
  }
  return false;
}

function estadoTexto(estado: boolean | string | null | undefined): "ACTIVO" | "INACTIVO" {
  return estadoActivo(estado) ? "ACTIVO" : "INACTIVO";
}

export const AdministracionUsuarios = () => {
  const [usuariosPaginados, setUsuariosPaginados] = useState<Paginado<Usuario>>();
  const [cargando, setCargando] = useState(true);
  const [paginaActual, setPaginaActual] = useState(1);
  const [filtroEstado, setFiltroEstado] = useState<boolean | null>(null);
  const [filtroRol, setFiltroRol] = useState<USER_FILER_TYPE>(null);
  const [recargaUsuarios, setRecargaUsuarios] = useState(0);
  const [usuariosActualizando, setUsuariosActualizando] = useState<Set<number>>(
    () => new Set(),
  );
  const { error: mostrarError, exito: mostrarExito } = useToast();

  useEffect(() => {
    let activo = true;

    usuarioService
      .getUsuarios({
        page: paginaActual,
        limit: ITEMS_PER_PAGE,
        rol: filtroRol,
        estado: filtroEstado,
      })
      .then((resp) => {
        if (!activo) return;
        setUsuariosPaginados(resp);
      })
      .catch((error: unknown) => {
        if (!activo) return;
        mostrarError(
          error instanceof Error
            ? error.message
            : 'No se pudieron cargar los usuarios.',
        );
      })
      .finally(() => {
        if (activo) setCargando(false);
      });

    return () => {
      activo = false;
    };
  }, [paginaActual, filtroEstado, filtroRol, recargaUsuarios, mostrarError]);

  async function cambiarEstadoUsuario(usuario: Usuario, activo: boolean) {
    const siguienteEstado = !activo;
    setUsuariosActualizando((actualizando) => new Set(actualizando).add(usuario.idUsuario));

    try {
      await usuarioService.cambiarEstado(
        usuario.idUsuario,
        siguienteEstado,
      );
      setRecargaUsuarios((recarga) => recarga + 1);
      mostrarExito(`Usuario ${siguienteEstado ? 'activado' : 'desactivado'} correctamente.`);
    } catch (error) {
      mostrarError(
        error instanceof Error
          ? error.message
          : 'No se pudo cambiar el estado del usuario.',
      );
    } finally {
      setUsuariosActualizando((actualizando) => {
        const siguiente = new Set(actualizando);
        siguiente.delete(usuario.idUsuario);
        return siguiente;
      });
    }
  }

  return (
    <section className="mx-auto w-full max-w-7xl p-4 sm:p-6 lg:p-8" aria-labelledby="admin-users-title">
      <header className="mb-6">
        <p className="text-sm font-semibold uppercase tracking-wide text-blue-700">Gestión del sistema</p>
        <h1 id="admin-users-title" className="mt-1 text-2xl font-bold tracking-tight text-gray-900 sm:text-3xl">
          Administración de usuarios
        </h1>
      </header>

      <div className="mb-5 grid grid-cols-1 gap-4 rounded-xl border border-gray-200 bg-white p-4 shadow-sm sm:grid-cols-2">
        <div className="grid gap-1.5">
          <label htmlFor="filtro-estado-usuarios" className="text-sm font-medium text-gray-700">Estado</label>
          <select
            id="filtro-estado-usuarios"
            className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
            value={filtroEstado === null ? '' : String(filtroEstado)}
            onChange={(event) => {
              const value = event.target.value;
              setFiltroEstado(value === '' ? null : value === 'true');
              setPaginaActual(1);
            }}
          >
            <option value="">Todos los estados</option>
            <option value="true">Activos</option>
            <option value="false">Inactivos</option>
          </select>
        </div>
        <div className="grid gap-1.5">
          <label htmlFor="filtro-rol-usuarios" className="text-sm font-medium text-gray-700">Rol</label>
          <select
            id="filtro-rol-usuarios"
            className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
            value={filtroRol ?? ''}
            onChange={(event) => {
              const value = event.target.value;
              setFiltroRol(
                value === 'ADM' || value === 'ORG' || value === 'PAR'
                  ? value
                  : null,
              );
              setPaginaActual(1);
            }}
          >
            <option value="">Todos los roles</option>
            <option value="ADM">Administradores</option>
            <option value="ORG">Organizadores</option>
            <option value="PAR">Participantes</option>
          </select>
        </div>
      </div>
      {cargando ? (
        <Loader />
      ) : !usuariosPaginados?.content.length ? (
        <div className="rounded-xl border border-dashed border-gray-300 bg-white px-6 py-12 text-center text-sm text-gray-600">
          No se encontraron usuarios con los filtros seleccionados.
        </div>
      ) : (
        <>
          <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
          <Table aria-label="Usuarios">
            <Table.Header className="[&>tr>th]:bg-gray-50 [&>tr>th]:text-gray-600 [&>tr>th]:text-xs [&>tr>th]:font-semibold [&>tr>th]:uppercase [&>tr>th]:tracking-wider">
              <Table.Head id="nombre">Nombre</Table.Head>
              <Table.Head id="correo">Correo</Table.Head>
              <Table.Head id="rol">Rol</Table.Head>
              <Table.Head id="telefono">Teléfono</Table.Head>
              <Table.Head id="registro">Registro</Table.Head>
              <Table.Head id="estado">Estado</Table.Head>
              <Table.Head id="acciones">Acciones</Table.Head>
            </Table.Header>
            <Table.Body>
              {usuariosPaginados.content.map((u) => {
                const id = u.idUsuario;
                const activo = estadoActivo(u.estado);
                const actualizando = usuariosActualizando.has(id);
                const nombreCompletoFila = [u.nombre, u.apellido].filter(Boolean).join(" ") || "—";
                const telefonoFila =  [u.codigoPais, u.telefono].filter(Boolean).join(" ") || "—";
                return (
                  <Table.Row key={id} id={String(id)}>
                    <Table.Cell className="font-medium text-gray-900">{nombreCompletoFila}</Table.Cell>
                    <Table.Cell>
                      <a href={`mailto:${u.email}`} className="text-blue-700 hover:text-blue-900 hover:underline">
                        {u.email}
                      </a>
                    </Table.Cell>
                    <Table.Cell>
                      <span className="inline-flex rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-medium text-indigo-700">
                        {u.rol}
                      </span>
                    </Table.Cell>
                    <Table.Cell>{telefonoFila}</Table.Cell>
                    <Table.Cell>
                      {fechaUtils.fechaCorta(u.fechaRegistro.toString())}
                    </Table.Cell>
                    <Table.Cell>
                      <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${
                        estadoActivo(u.estado) ? 'bg-emerald-50 text-emerald-700' : 'bg-gray-100 text-gray-700'
                      }`}>
                        {estadoTexto(u.estado)}
                      </span>
                    </Table.Cell>
                    <Table.Cell>
                      <button
                        type="button"
                        className={`rounded-lg px-3 py-2 text-xs font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-50 ${
                          activo
                            ? 'bg-red-50 text-red-700 hover:bg-red-100'
                            : 'bg-blue-700 text-white hover:bg-blue-800'
                        }`}
                        onClick={() => cambiarEstadoUsuario(u, activo)}
                        disabled={actualizando}
                        aria-label={`${activo ? "Desactivar" : "Activar"} a ${nombreCompletoFila}`}
                      >
                        {actualizando ? "Actualizando..." : activo ? "Desactivar" : "Activar"}
                      </button>
                    </Table.Cell>
                  </Table.Row>
                );
              })}
            </Table.Body>
          </Table>
          </div>
          {usuariosPaginados.totalPages > 1 && (
            <div className="mt-4 rounded-xl border border-gray-200 bg-white px-4 py-3">
              <span className="mb-3 block text-sm text-gray-600" aria-live="polite">
                Mostrando {(paginaActual - 1) * ITEMS_PER_PAGE + 1}-
                {Math.min(
                  (paginaActual - 1) * ITEMS_PER_PAGE + usuariosPaginados.content.length,
                  usuariosPaginados.totalElements,
                )} de {usuariosPaginados.totalElements} usuarios
              </span>
              <PaginationPageDefault
                page={paginaActual}
                total={usuariosPaginados.totalPages}
                onPageChange={setPaginaActual}
              />
            </div>
          )}
        </>
      )}
    </section>
  );
};