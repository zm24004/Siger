import type { CrearEventoPayload, EventoDetalle } from '../interfaces/api-interface/eventos.interface.js';
import type { IHttpClient } from '../interfaces/api-interface/http-adapter/http-client.interface.js';
import type { Paginado } from '../interfaces/common.js';
import httpClient from './http-adapter/axios-http-client.adapter.js';

export class EventoService {
  private readonly httpClient: IHttpClient;

  constructor(httpClient: IHttpClient) {
    this.httpClient = httpClient;
  }

  /** GET /api/eventos -> listado público de eventos. */
  async getPublicCatalogo(): Promise<EventoDetalle[]> {
    return this.httpClient.get<EventoDetalle[]>('/eventos');
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
  async getEventosPaginado(): Promise<Paginado<EventoDetalle>> {
    return this.httpClient.get<Paginado<EventoDetalle>>('/eventos');
  }

  async getPopularEvents(): Promise<EventoDetalle[]> {
    const resp = await this.httpClient.get<Paginado<EventoDetalle>>('/public/eventos-populares' + '?page=1&limit=10');
    return resp.content;
  }

}

export const eventoService = new EventoService(httpClient);
