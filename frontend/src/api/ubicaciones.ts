import type { CrearUbicacionPayload, Ubicacion } from '../interfaces/api-interface/ubicacion.interface';
import type { IHttpClient } from '../interfaces/api-interface/http-adapter/http-client.interface.js';
import httpClient from './http-adapter/axios-http-client.adapter.js';
import type { Paginado } from '../interfaces/common.js';

export class UbicacionService {
  private readonly httpClient: IHttpClient;

  constructor(httpClient: IHttpClient) {
    this.httpClient = httpClient;
  }

  /**
  * GET /api/ubicaciones -> listado de ubicaciones. 
  * */
  async getUbicaciones(page: number, size: number ): Promise<Paginado<Ubicacion>> {

    const resp = await this.httpClient.get<Paginado<Ubicacion>>('/ubicaciones' + `?page=${page}&size=${size}`);
    return resp;
  }

  /**
   * POST /api/ubicaciones (ORGANIZADOR).
   * Body: { nombre, direccion, capacidad }
   */
   async crearUbicacion(data: CrearUbicacionPayload): Promise<Ubicacion> {
     return this.httpClient.post<Ubicacion>('/ubicaciones', data);
   }

   async getUbicacionesPorTipo(page: number, size: number = 200, tipo: string = 'Presencial' ): Promise<Paginado<Ubicacion>> {
     return this.httpClient.get<Paginado<Ubicacion>>(`/ubicaciones` + `?page=${page}&size=${size}&tipo=${tipo}`);
   }
}

export const ubicacionService = new UbicacionService(httpClient);

