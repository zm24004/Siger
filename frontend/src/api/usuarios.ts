import type { Usuario, UsuarioPaginadoParams } from '../interfaces/api-interface/usuario.interface.js';
import type { Paginado } from '../interfaces/common.js';
import type { IHttpClient } from '../interfaces/api-interface/http-adapter/http-client.interface.js';
import httpClient from './http-adapter/axios-http-client.adapter.js';

export class UsuarioService {
  private readonly httpClient: IHttpClient;

  constructor(httpClient: IHttpClient) {
    this.httpClient = httpClient;
  }

  /** GET /api/usuarios (ADMINISTRADOR). */
  async getUsuarios(filters: UsuarioPaginadoParams): Promise<Paginado<Usuario>> {
    const params = new URLSearchParams();

    // Agrega solo los valores definidos (ignora undefined/null)
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, String(value));
      }
    });

    return this.httpClient.get<Paginado<Usuario>>(`/usuarios?${params.toString()}`);
  }

  /** PATCH /api/usuarios/{id}/estado (ADMINISTRADOR).
   *  Body: { estado } — 'ACTIVO' | 'INACTIVO'
   */
  async cambiarEstado(idUsuario: number, estado: boolean): Promise<Usuario> {
    return this.httpClient.patch<Usuario>(`/usuarios/${idUsuario}/estado`, { estado });
  }

  async countActiveUsers(): Promise<number> {
    const resp = await this.httpClient.get<Paginado<Usuario>>('/usuarios?estado=true&page=1&limit=1');
    return resp.totalElements;
  }

  async countInActiveUsers(): Promise<number> {
    const resp = await this.httpClient.get<Paginado<Usuario>>('/usuarios?estado=false&page=1&limit=1');
    return resp.totalElements;
  }

  async countOrganizerUsers(): Promise<number> {
    const resp = await this.httpClient.get<Paginado<Usuario>>('/usuarios?estado=true&page=1&limit=1&rol=ORG');
    return resp.totalElements;
  }

  async countParticipantUsers(): Promise<number> {
    const resp = await this.httpClient.get<Paginado<Usuario>>('/usuarios?estado=true&page=1&limit=1&rol=PAR');
    return resp.totalElements;
  }
}

export const usuarioService = new UsuarioService(httpClient);
