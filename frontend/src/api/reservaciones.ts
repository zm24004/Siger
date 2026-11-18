import type { Reservacion } from '../interfaces/api-interface/reservacion.interface.js';
import type { IHttpClient } from '../interfaces/api-interface/http-adapter/http-client.interface.js';
import httpClient from './http-adapter/axios-http-client.adapter.js';

export class ReservacionService {
  private readonly httpClient: IHttpClient;

  constructor(httpClient: IHttpClient) {
    this.httpClient = httpClient;
  }

  async reservar(idEvento: number): Promise<Reservacion> {
    return this.httpClient.post<Reservacion>(`/reservaciones/evento/${idEvento}`);
  }

  async getMisReservaciones(): Promise<Array<Reservacion>> {
    return this.httpClient.get<Array<Reservacion>>('/reservaciones/mis-reservaciones');
  }

  async getReservacion(codigo: string): Promise<Reservacion> {
    return this.httpClient.get<Reservacion>(`/reservaciones/${codigo}`);
  }

  // Modificar estado de la reserva (participante)
  async pausarReservacion(id: number): Promise<boolean> {
    const resp = await this.httpClient.patch<Reservacion>(`/reservaciones/${id}/pausar`);
    return Boolean(resp);
  }

  async confirmarReservacion(id: number): Promise<boolean> {
    const resp = await this.httpClient.patch<Reservacion>(`/reservaciones/${id}/confirmar`);
    return Boolean(resp);
  }

  async cancelarReservacion(id: number): Promise<boolean> {
    const resp = await this.httpClient.patch<Reservacion>(`/reservaciones/${id}/cancelar`);
    console.log('cancelarReservacion', resp);
    return Boolean(resp);
  }

  // Admin: listar todas + cancelar cualquiera
  async getTodasReservaciones(): Promise<Reservacion[]> {
    return this.httpClient.get<Reservacion[]>('/reservaciones');
  }

  async cancelarComoAdmin(id: number): Promise<boolean> {
    const resp = await this.httpClient.patch<boolean>(`/reservaciones/admin/${id}/cancelar`);
    return Boolean(resp);
  }

}

export const reservacionService = new ReservacionService(httpClient);
