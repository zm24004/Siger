import type { IAsistencia } from '../interfaces/api-interface/asistencia.interface.js';
import type { IHttpClient } from '../interfaces/api-interface/http-adapter/http-client.interface.js';
import httpClient from './http-adapter/axios-http-client.adapter.js';

export class AsistenciaService {
  private readonly httpClient: IHttpClient;

  constructor(httpClient: IHttpClient) {
    this.httpClient = httpClient;
  }

  /**
   * POST /api/asistencias/registrar (ORGANIZADOR).
   * Body: { codigoReservacion }
  */
  async registrarAsistencia(codigoReservacion: string): Promise<IAsistencia> {
    return this.httpClient.post<IAsistencia>('/asistencias/registrar', { codigoReservacion });
  }
}

export const asistenciaService = new AsistenciaService(httpClient);

