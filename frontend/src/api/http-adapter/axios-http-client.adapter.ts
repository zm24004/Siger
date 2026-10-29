import type { IHttpClient } from '../../interfaces/api-interface/http-adapter/http-client.interface.js';
import http from './axios-instance.js';

export class AxiosHttpClientAdapter implements IHttpClient {
  async get<TResponse>(url: string): Promise<TResponse> {
    const response = await http.get<TResponse>(url);
    return response.data;
  }

  async post<TResponse>(url: string, data?: unknown): Promise<TResponse> {
    const response = await http.post<TResponse>(url, data);
    return response.data;
  }

  async put<TResponse>(url: string, data?: unknown): Promise<TResponse> {
    const response = await http.put<TResponse>(url, data);
    return response.data;
  }

  async patch<TResponse>(url: string, data?: unknown): Promise<TResponse> {
    const response = await http.patch<TResponse>(url, data);
    return response.data;
  }
}

const httpClient = new AxiosHttpClientAdapter();

export default httpClient;