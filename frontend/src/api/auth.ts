import type { LoginAuthUserResponse, RegisterUserPayload, RecoveryResponse } from '../interfaces/api-interface/auth.interface.js';
import type { IHttpClient } from '../interfaces/api-interface/http-adapter/http-client.interface.js';
import httpClient from './http-adapter/axios-http-client.adapter.js';

export class AuthService {
  private readonly httpClient: IHttpClient;

  constructor(httpClient: IHttpClient) {
    this.httpClient = httpClient;
  }

  /**
   * POST /api/auth/login
   * Body: { email, contrasena }
   *  Response: { token, nombre, rol, idUsuario }
   */
  async loginPost(data: { email: string; contrasena: string }): Promise<LoginAuthUserResponse> {
    return this.httpClient.post<LoginAuthUserResponse>('/auth/login', data);
  }

  /** POST /api/auth/registro
   *  Body: { nombre, apellido, email, telefono, codigoPais, contrasena, rol }
   *  Response: { token, nombre, apellido, rol, idUsuario }
   */
  async registroPost(data: RegisterUserPayload): Promise<LoginAuthUserResponse> {
    return this.httpClient.post<LoginAuthUserResponse>('/auth/registro', data);
  }

  /** POST /api/auth/recuperar-contrasena
   *  Body: { email, nombreOAlias }
   *  Response: { message, protocolo }
   *  El backend SIEMPRE devuelve el mismo mensaje (protocolo de seguridad):
   *  no revela si el email existe o no.
   */
  async recuperarContrasenaPost(data: { email: string; nombreOAlias: string }): Promise<RecoveryResponse> {
    return this.httpClient.post<RecoveryResponse>('/auth/forgot-password', data);
  }
}

export const authService = new AuthService(httpClient);
