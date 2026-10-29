import type { Usuario } from '../interfaces/api-interface/usuario.interface.js';
import type { Paginado } from '../interfaces/common.js';
import type { IHttpClient } from '../interfaces/api-interface/http-adapter/http-client.interface.js';
import httpClient from './http-adapter/axios-http-client.adapter.js';

export class UsuarioService {
  private readonly httpClient: IHttpClient;

  constructor(httpClient: IHttpClient) {
    this.httpClient = httpClient;
  }

  /** GET /api/usuarios (ADMINISTRADOR). */
  async getUsuarios(): Promise<Paginado<Usuario>> {
    return this.httpClient.get<Paginado<Usuario>>('/usuarios');
  }

  /** PATCH /api/usuarios/{id}/estado (ADMINISTRADOR).
   *  Body: { estado } — 'ACTIVO' | 'INACTIVO'
   */
   async cambiarEstado(idUsuario: number, estado: 'ACTIVO' | 'INACTIVO'): Promise<Usuario> {
     return this.httpClient.patch<Usuario>(`/usuarios/${idUsuario}/estado`, { estado });
   }
}

export const usuarioService = new UsuarioService(httpClient);
