export interface EventoDetalle {
    idEvento: number;
    nombre: string;
    fecha: Date;
    horaInicio: string;
    capacidadMaxima: number;
    estado: string;
    cuposDisponibles: number;
    descripcion: string;
    horaFin: string;
    idUbicacion: number;
    ubicacionNombre: string;
    idOrganizador: number;
    organizadorNombre: string;
}


export interface CrearEventoPayload {
    nombre: string;
    fecha: Date | string;
    horaInicio: string;
    capacidadMaxima: number;
    idUbicacion: number;
    descripcion: string;
    horaFin: string;
}


export type EVENTO_FILTER_TYPE = 'Activo' | 'Cancelado'  | null | undefined;
export interface EventoPaginadoFilters {
    page: number;
    limit: number;
    fechaDesde: string | null | undefined;
    fechaHasta: string  | null | undefined;
    estado: EVENTO_FILTER_TYPE;
    idUbicacion: number  | null | undefined;
}
