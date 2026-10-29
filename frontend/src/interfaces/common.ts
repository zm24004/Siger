// Tipos compartidos usados en varios módulos del frontend SIGER.

export type Rol = 'PARTICIPANTE' | 'ORGANIZADOR' | 'ADMINISTRADOR';

export type EstadoEvento = 'ACTIVO' | 'CANCELADO';

export type EstadoReservacion =
  | 'PENDIENTE'
  | 'CONFIRMADA'
  | 'Pausada'
  | 'CANCELADA';

export type TipoNotificacion = 'info' | 'exito' | 'advertencia' | 'error';

/** Respuesta genérica de los endpoints REST (integracion con el backend). */
export interface ApiSuccess<T = unknown> {
  message?: string;
  protocolo?: string;
  data?: T;
}

/** Respuesta genérica de error de la interceptor de http.js */
export interface ApiError {
  status: number;
  message: string;
  path: string | null;
  errors: unknown[] | null;
  timestamp: string | null;
}

export interface Paginado<T> {
  content:       T[];
  page:          number;
  limit:         number;
  totalElements: number;
  totalPages:    number;
}


