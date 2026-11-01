import { useEffect, useState } from 'react';
import { CalendarDays, MapPin, Users } from 'lucide-react';
import { eventoService } from '../../../api/eventos';
import { Loader } from '../../../components/Loader';
import { useToast } from '../../../context/ToastContext';
import type { EventoDetalle } from '../../../interfaces/api-interface/eventos.interface';

function formatearFecha(fecha: Date | string): string {
  const valor = new Date(fecha);
  if (Number.isNaN(valor.getTime())) return String(fecha);
  return valor.toLocaleDateString('es-SV', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}

export const AdministracionEventos = () => {
  // const [eventos, setEventos] = useState<EventoDetalle[]>([]);
  // const [cargando, setCargando] = useState(true);
  // const { error: mostrarError } = useToast();

  // useEffect(() => {
  //   let activo = true;

  //   eventoService
  //     .getEventosPaginado()
  //     .then((respuesta) => {
  //       if (activo) setEventos(respuesta.content);
  //     })
  //     .catch((error: unknown) => {
  //       if (!activo) return;
  //       mostrarError(
  //         error instanceof Error ? error.message : 'No se pudieron cargar los eventos.',
  //       );
  //     })
  //     .finally(() => {
  //       if (activo) setCargando(false);
  //     });

  //   return () => {
  //     activo = false;
  //   };
  // }, [mostrarError]);

  return (
    <>
      <h1>Hola mundo</h1>
    </>
    // <section className="mx-auto w-full max-w-7xl p-4 sm:p-6 lg:p-8" aria-labelledby="admin-events-title">
    //   <header className="mb-6">
    //     <p className="text-sm font-semibold uppercase tracking-wide text-blue-700">Gestión del sistema</p>
    //     <h1 id="admin-events-title" className="mt-1 text-2xl font-bold tracking-tight text-gray-900 sm:text-3xl">
    //       Administración de eventos
    //     </h1>
    //     <p className="mt-2 text-sm text-gray-600">Consulta los eventos registrados, su organización y disponibilidad.</p>
    //   </header>

    //   {cargando ? (
    //     <div className="rounded-xl border border-gray-200 bg-white p-8 shadow-sm">
    //       <Loader label="Cargando eventos..." />
    //     </div>
    //   ) : eventos.length === 0 ? (
    //     <div className="rounded-xl border border-dashed border-gray-300 bg-white px-6 py-12 text-center shadow-sm">
    //       <CalendarDays className="mx-auto mb-3 text-gray-400" size={32} aria-hidden="true" />
    //       <h2 className="font-semibold text-gray-900">No hay eventos registrados</h2>
    //       <p className="mt-1 text-sm text-gray-600">Los eventos aparecerán aquí cuando estén disponibles.</p>
    //     </div>
    //   ) : (
    //     <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
    //       <div className="overflow-x-auto">
    //         <table className="w-full min-w-[760px] divide-y divide-gray-200 text-left text-sm">
    //           <thead className="bg-gray-50 text-xs font-semibold uppercase tracking-wider text-gray-600">
    //             <tr>
    //               <th scope="col" className="px-5 py-3">Evento</th>
    //               <th scope="col" className="px-5 py-3">Fecha</th>
    //               <th scope="col" className="px-5 py-3">Ubicación</th>
    //               <th scope="col" className="px-5 py-3">Organizador</th>
    //               <th scope="col" className="px-5 py-3">Disponibilidad</th>
    //               <th scope="col" className="px-5 py-3">Estado</th>
    //             </tr>
    //           </thead>
    //           <tbody className="divide-y divide-gray-100">
    //             {eventos.map((evento) => {
    //               const activo = evento.estado?.toUpperCase() === 'ACTIVO';
    //               return (
    //                 <tr key={evento.idEvento} className="transition-colors hover:bg-gray-50">
    //                   <td className="px-5 py-4">
    //                     <p className="font-semibold text-gray-900">{evento.nombre}</p>
    //                     <p className="mt-1 max-w-xs truncate text-gray-500">{evento.descripcion || 'Sin descripción'}</p>
    //                   </td>
    //                   <td className="whitespace-nowrap px-5 py-4 text-gray-700">
    //                     <div className="flex items-center gap-2">
    //                       <CalendarDays size={16} className="text-gray-400" aria-hidden="true" />
    //                       <span>{formatearFecha(evento.fecha)}</span>
    //                     </div>
    //                     <p className="mt-1 pl-6 text-xs text-gray-500">
    //                       {evento.horaInicio} - {evento.horaFin}
    //                     </p>
    //                   </td>
    //                   <td className="px-5 py-4 text-gray-700">
    //                     <span className="flex items-center gap-2">
    //                       <MapPin size={16} className="shrink-0 text-gray-400" aria-hidden="true" />
    //                       {evento.ubicacionNombre || 'No especificada'}
    //                     </span>
    //                   </td>
    //                   <td className="px-5 py-4 text-gray-700">{evento.organizadorNombre || 'No especificado'}</td>
    //                   <td className="whitespace-nowrap px-5 py-4 text-gray-700">
    //                     <span className="inline-flex items-center gap-1.5">
    //                       <Users size={16} className="text-gray-400" aria-hidden="true" />
    //                       {evento.cuposDisponibles} / {evento.capacidadMaxima}
    //                     </span>
    //                   </td>
    //                   <td className="px-5 py-4">
    //                     <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${
    //                       activo ? 'bg-emerald-50 text-emerald-700' : 'bg-gray-100 text-gray-700'
    //                     }`}>
    //                       {evento.estado || 'SIN ESTADO'}
    //                     </span>
    //                   </td>
    //                 </tr>
    //               );
    //             })}
    //           </tbody>
    //         </table>
    //       </div>
    //     </div>
    //   )}
    // </section>
  );
};
