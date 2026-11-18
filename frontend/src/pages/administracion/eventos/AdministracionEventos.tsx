import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { eventoService } from '../../../api/eventos';
import { ubicacionService } from '../../../api/ubicaciones';
import { Loader } from '../../../components/Loader';
import Modal from '../../../components/Modal';
import { PaginationPageDefault } from '../../../components/untitledui/application/pagination/pagination';
import { Table } from '../../../components/untitledui/application/table/table';
import { useToast } from '../../../context/ToastContext';
import type {
  EVENTO_FILTER_TYPE,
  EventoDetalle,
  EventoPaginadoFilters,
} from '../../../interfaces/api-interface/eventos.interface';
import type { Ubicacion } from '../../../interfaces/api-interface/ubicacion.interface';
import type { Paginado } from '../../../interfaces/common';

const ITEMS_PER_PAGE = 10;

function formatearFecha(fecha: Date | string): string {
  const texto = String(fecha);
  const valor = /^\d{4}-\d{2}-\d{2}$/.test(texto)
    ? new Date(`${texto}T00:00:00`)
    : new Date(fecha);
  if (Number.isNaN(valor.getTime())) return texto;
  return valor.toLocaleDateString('es-SV', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}

export const AdministracionEventos = () => {
  const [eventosPaginados, setEventosPaginados] = useState<Paginado<EventoDetalle>>();
  const [ubicaciones, setUbicaciones] = useState<Ubicacion[]>([]);
  const [cargando, setCargando] = useState(true);
  const [cargandoUbicaciones, setCargandoUbicaciones] = useState(true);
  const [errorCarga, setErrorCarga] = useState(false);
  const [paginaActual, setPaginaActual] = useState(1);
  const [fechaDesde, setFechaDesde] = useState('');
  const [fechaHasta, setFechaHasta] = useState('');
  const [filtroEstado, setFiltroEstado] = useState<EVENTO_FILTER_TYPE>(null);
  const [filtroUbicacion, setFiltroUbicacion] = useState<number | null>(null);
  const [recarga, setRecarga] = useState(0);
  const [eventoPorCancelar, setEventoPorCancelar] = useState<EventoDetalle | null>(null);
  const [cancelandoId, setCancelandoId] = useState<number | null>(null);
  const { error: mostrarError, exito: mostrarExito } = useToast();

  useEffect(() => {
    let activo = true;

    ubicacionService
      .getUbicaciones(1, 100)
      .then((resp) => {
        if (activo) setUbicaciones(resp.content);
      })
      .catch((error: unknown) => {
        if (!activo) return;
        mostrarError(
          error instanceof Error
            ? error.message
            : 'No se pudieron cargar las ubicaciones.',
        );
      })
      .finally(() => {
        if (activo) setCargandoUbicaciones(false);
      });

    return () => {
      activo = false;
    };
  }, [mostrarError]);

  useEffect(() => {
    let activo = true;
    setCargando(true);
    setErrorCarga(false);

    const filtros: EventoPaginadoFilters = {
      page: paginaActual,
      limit: ITEMS_PER_PAGE,
      fechaDesde: fechaDesde || null,
      fechaHasta: fechaHasta || null,
      estado: filtroEstado,
      idUbicacion: filtroUbicacion,
    };

    eventoService
      .getEventosPaginado(filtros)
      .then((resp) => {
        if (!activo) return;
        setEventosPaginados(resp);
      })
      .catch((error: unknown) => {
        if (!activo) return;
        setErrorCarga(true);
        mostrarError(
          error instanceof Error
            ? error.message
            : 'No se pudieron cargar los eventos.',
        );
      })
      .finally(() => {
        if (activo) setCargando(false);
      });

    return () => {
      activo = false;
    };
  }, [
    paginaActual,
    fechaDesde,
    fechaHasta,
    filtroEstado,
    filtroUbicacion,
    recarga,
    mostrarError,
  ]);

  function reiniciarPaginacion() {
    setPaginaActual(1);
  }

  async function confirmarCancelacion() {
    if (!eventoPorCancelar || cancelandoId !== null) return;

    const evento = eventoPorCancelar;
    setCancelandoId(evento.idEvento);
    try {
      await eventoService.cancelarEvento(evento.idEvento);
      mostrarExito('Evento cancelado correctamente.');
      setEventoPorCancelar(null);
      setRecarga((actual) => actual + 1);
    } catch (error: unknown) {
      mostrarError(
        error instanceof Error
          ? error.message
          : 'No se pudo cancelar el evento.',
      );
    } finally {
      setCancelandoId(null);
    }
  }

  return (
    <section className="mx-auto w-full max-w-7xl p-4 sm:p-6 lg:p-8" aria-labelledby="admin-events-title">
      <header className="mb-6">
        <p className="text-sm font-semibold uppercase tracking-wide text-blue-700">Gestión del sistema</p>
        <h1 id="admin-events-title" className="mt-1 text-2xl font-bold tracking-tight text-gray-900 sm:text-3xl">
          Administración de eventos
        </h1>
      </header>

      <div className="mb-5 grid grid-cols-1 gap-4 rounded-xl border border-gray-200 bg-white p-4 shadow-sm sm:grid-cols-2 lg:grid-cols-4">
        <div className="grid gap-1.5">
          <label htmlFor="filtro-fecha-desde-eventos" className="text-sm font-medium text-gray-700">Fecha desde</label>
          <input
            id="filtro-fecha-desde-eventos"
            type="date"
            className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
            value={fechaDesde}
            onChange={(event) => {
              setFechaDesde(event.target.value);
              reiniciarPaginacion();
            }}
          />
        </div>
        <div className="grid gap-1.5">
          <label htmlFor="filtro-fecha-hasta-eventos" className="text-sm font-medium text-gray-700">Fecha hasta</label>
          <input
            id="filtro-fecha-hasta-eventos"
            type="date"
            className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
            value={fechaHasta}
            onChange={(event) => {
              setFechaHasta(event.target.value);
              reiniciarPaginacion();
            }}
          />
        </div>
        <div className="grid gap-1.5">
          <label htmlFor="filtro-estado-eventos" className="text-sm font-medium text-gray-700">Estado</label>
          <select
            id="filtro-estado-eventos"
            className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
            value={filtroEstado ?? ''}
            onChange={(event) => {
              const value = event.target.value;
              setFiltroEstado(value === 'Activo' || value === 'Cancelado' ? value : null);
              reiniciarPaginacion();
            }}
          >
            <option value="">Todos los estados</option>
            <option value="Activo">Activos</option>
            <option value="Cancelado">Cancelados</option>
          </select>
        </div>
        <div className="grid gap-1.5">
          <label htmlFor="filtro-ubicacion-eventos" className="text-sm font-medium text-gray-700">Ubicación</label>
          <select
            id="filtro-ubicacion-eventos"
            className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 disabled:cursor-not-allowed disabled:bg-gray-50"
            value={filtroUbicacion ?? ''}
            disabled={cargandoUbicaciones}
            onChange={(event) => {
              setFiltroUbicacion(event.target.value ? Number(event.target.value) : null);
              reiniciarPaginacion();
            }}
          >
            <option value="">{cargandoUbicaciones ? 'Cargando ubicaciones...' : 'Todas las ubicaciones'}</option>
            {ubicaciones.map((ubicacion) => (
              <option key={ubicacion.idUbicacion} value={ubicacion.idUbicacion}>
                {ubicacion.nombre}
              </option>
            ))}
          </select>
        </div>
      </div>

      {cargando ? (
        <Loader />
      ) : errorCarga ? (
        <div className="rounded-xl border border-dashed border-gray-300 bg-white px-6 py-12 text-center">
          <p className="text-sm text-gray-600">No se pudieron cargar los eventos.</p>
          <button
            type="button"
            className="mt-3 rounded-lg bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800"
            onClick={() => setRecarga((actual) => actual + 1)}
          >
            Reintentar
          </button>
        </div>
      ) : !eventosPaginados?.content.length ? (
        <div className="rounded-xl border border-dashed border-gray-300 bg-white px-6 py-12 text-center text-sm text-gray-600">
          No se encontraron eventos con los filtros seleccionados.
        </div>
      ) : (
        <>
          <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
            <Table aria-label="Eventos">
              <Table.Header className="[&>tr>th]:bg-gray-50 [&>tr>th]:text-gray-600 [&>tr>th]:text-xs [&>tr>th]:font-semibold [&>tr>th]:uppercase [&>tr>th]:tracking-wider">
                <Table.Head id="evento">Evento</Table.Head>
                <Table.Head id="fecha">Fecha</Table.Head>
                <Table.Head id="horario">Horario</Table.Head>
                <Table.Head id="ubicacion">Ubicación</Table.Head>
                <Table.Head id="organizador">Organizador</Table.Head>
                <Table.Head id="estado">Estado</Table.Head>
                <Table.Head id="cupos">Cupos disponibles</Table.Head>
                <Table.Head id="opciones">Opciones</Table.Head>
              </Table.Header>
              <Table.Body>
                {eventosPaginados.content.map((evento) => {
                  const activo = evento.estado.toLowerCase() === 'activo';
                  const cancelado = evento.estado.toLowerCase() === 'cancelado';
                  return (
                    <Table.Row key={evento.idEvento} id={String(evento.idEvento)}>
                      <Table.Cell className="font-medium text-gray-900">{evento.nombre}</Table.Cell>
                      <Table.Cell>{formatearFecha(evento.fecha)}</Table.Cell>
                      <Table.Cell>{evento.horaInicio} - {evento.horaFin}</Table.Cell>
                      <Table.Cell>{evento.ubicacionNombre || '—'}</Table.Cell>
                      <Table.Cell>{evento.organizadorNombre || '—'}</Table.Cell>
                      <Table.Cell>
                        <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${
                          cancelado
                            ? 'bg-red-50 text-red-700'
                            : activo
                              ? 'bg-emerald-50 text-emerald-700'
                              : 'bg-gray-100 text-gray-700'
                        }`}>
                          {evento.estado}
                        </span>
                      </Table.Cell>
                      <Table.Cell>{evento.cuposDisponibles} / {evento.capacidadMaxima}</Table.Cell>
                      <Table.Cell>
                        <div className="flex flex-nowrap items-center gap-2 whitespace-nowrap">
                          <Link
                            to={`/evento/${evento.idEvento}`}
                            className="rounded-lg border border-gray-300 px-3 py-2 text-xs font-semibold text-gray-700 transition-colors hover:bg-gray-50"
                          >
                            Ver
                          </Link>

                          {/* // TODO: agregar soporte al api para que  un admin pueda cancelar */}
                          {/* {activo && (
                            <button
                              type="button"
                              className="rounded-lg bg-red-50 px-3 py-2 text-xs font-semibold text-red-700 transition-colors hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-50"
                              onClick={() => setEventoPorCancelar(evento)}
                              disabled={cancelandoId !== null}
                              aria-label={`Cancelar ${evento.nombre}`}
                            >
                              Cancelar
                            </button>
                          )} */}
                        </div>
                      </Table.Cell>
                    </Table.Row>
                  );
                })}
              </Table.Body>
            </Table>
          </div>
          {eventosPaginados.totalPages > 1 && (
            <div className="mt-4 rounded-xl border border-gray-200 bg-white px-4 py-3">
              <span className="mb-3 block text-sm text-gray-600" aria-live="polite">
                Mostrando {(paginaActual - 1) * ITEMS_PER_PAGE + 1}-
                {Math.min(
                  (paginaActual - 1) * ITEMS_PER_PAGE + eventosPaginados.content.length,
                  eventosPaginados.totalElements,
                )} de {eventosPaginados.totalElements} eventos
              </span>
              <PaginationPageDefault
                page={paginaActual}
                total={eventosPaginados.totalPages}
                onPageChange={setPaginaActual}
              />
            </div>
          )}
        </>
      )}
      <Modal
        open={eventoPorCancelar !== null}
        onClose={() => {
          if (cancelandoId === null) setEventoPorCancelar(null);
        }}
        titulo="Cancelar evento"
        ariaLabel="Confirmar cancelación de evento"
        footer={
          <>
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => setEventoPorCancelar(null)}
              disabled={cancelandoId !== null}
            >
              Volver
            </button>
            <button
              type="button"
              className="btn btn-danger"
              onClick={confirmarCancelacion}
              disabled={cancelandoId !== null}
            >
              {cancelandoId !== null ? 'Cancelando...' : 'Sí, cancelar'}
            </button>
          </>
        }
      >
        <p>
          ¿Seguro que deseas cancelar <strong>{eventoPorCancelar?.nombre}</strong>? Esta acción
          notificará a los participantes con reserva.
        </p>
      </Modal>
    </section>
  );
};
