import type {
  Notificacion,
  NotificacionCursor,
  NotificacionPagina,
} from '../interfaces/api-interface/notificaciones.interface.js';
import type { IHttpClient } from '../interfaces/api-interface/http-adapter/http-client.interface.js';
import httpClient from './http-adapter/axios-http-client.adapter.js';

export class NotificacionService {
  private readonly httpClient: IHttpClient;

  constructor(httpClient: IHttpClient) {
    this.httpClient = httpClient;
  }

  /** GET /api/notificaciones/mis (autenticado). */
  async getMis(): Promise<Array<Notificacion>> {
    return this.httpClient.get<Array<Notificacion>>('/notificaciones/mis');
  }

  /** GET /api/notificaciones/mis/pagina (autenticado, cursor por fecha e ID). */
  async getPagina(
    cursor: NotificacionCursor | null = null,
    limit = 20,
  ): Promise<NotificacionPagina> {
    const params = new URLSearchParams({ limit: String(limit) });
    if (cursor) {
      params.set('cursorFecha', cursor.fechaEnvio);
      params.set('cursorId', String(cursor.idNotificacion));
    }
    return this.httpClient.get<NotificacionPagina>(
      `/notificaciones/mis/pagina?${params.toString()}`,
    );
  }

  /** GET /api/notificaciones/mis/no-leidas (autenticado). */
  async getNoLeidas(): Promise<number> {
    const resp = await this.httpClient.get<{noLeidas: number}>('/notificaciones/mis/no-leidas');
    return resp.noLeidas
  }

  /** PATCH /api/notificaciones/{id}/leer */
  async marcarLeida(id: number): Promise<Notificacion> {
    return this.httpClient.patch<Notificacion>(`/notificaciones/${id}/leer`);
  }
}

export const notificacionService = new NotificacionService(httpClient);
