import type { CrearEventoPayload, EventoDetalle, EventoPaginadoFilters } from '../interfaces/api-interface/eventos.interface.js';
import type { IHttpClient } from '../interfaces/api-interface/http-adapter/http-client.interface.js';
import type { Paginado } from '../interfaces/common.js';
import httpClient from './http-adapter/axios-http-client.adapter.js';

export class EventoService {
  private readonly httpClient: IHttpClient;

  constructor(httpClient: IHttpClient) {
    this.httpClient = httpClient;
  }

  /** GET /api/eventos -> contenido del catálogo público de eventos. */
  async getPublicCatalogo(): Promise<EventoDetalle[]> {
    const hoy = new Date();
    const fechaDesde = [
      hoy.getFullYear(),
      String(hoy.getMonth() + 1).padStart(2, '0'),
      String(hoy.getDate()).padStart(2, '0'),
    ].join('-');
    const response = await this.httpClient.get<Paginado<EventoDetalle>>(
      `/eventos?fechaDesde=${fechaDesde}&estado=Activo`,
    );
    return response.content;
  }

  /** GET /api/eventos/mis-eventos -> listado privado del organizador autenticado. */
  async getMisEventos(): Promise<EventoDetalle[]> {
    return this.httpClient.get<EventoDetalle[]>('/eventos/mis-eventos');
  }

  /** GET /api/eventos/{id} -> detalle público. */
  async getEvento(id: number): Promise<EventoDetalle> {
    return this.httpClient.get<EventoDetalle>(`/eventos/${id}`);
  }

  /** POST /api/eventos -> crear evento (ORGANIZADOR).
   *  Body: EventoRequest
   */
  async crearEvento(data: CrearEventoPayload): Promise<EventoDetalle> {
    return this.httpClient.post<EventoDetalle>('/eventos', data);
  }

  /** PATCH /api/eventos/{id}/cancelar (ORGANIZADOR). */
  async cancelarEvento(id: number): Promise<boolean> {
    const resp = await this.httpClient.patch<EventoDetalle>(`/eventos/${id}/cancelar`);
    return Boolean(resp);
  }

  /** GET /api/eventos -> listado de eventos paginado para Admin. */
  async getEventosPaginado(filters: EventoPaginadoFilters): Promise<Paginado<EventoDetalle>> {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, String(value));
      }
    });

    return this.httpClient.get<Paginado<EventoDetalle>>(`/eventos?${params.toString()}`);
  }

  async getPopularEvents(): Promise<EventoDetalle[]> {
    const resp = await this.httpClient.get<Paginado<EventoDetalle>>('/public/eventos-populares' + '?page=1&limit=10');
    return resp.content;
  }

  async countEventosActivosPaginado(): Promise<number> {
    const resp = await this.httpClient.get<Paginado<EventoDetalle>>('/eventos/activos?page=1&limit=1');
    return resp.totalElements;
  }

}

export const eventoService = new EventoService(httpClient);
