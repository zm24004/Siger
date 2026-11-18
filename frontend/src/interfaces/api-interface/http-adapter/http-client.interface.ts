export interface IHttpClient {
  get<TResponse>(url: string): Promise<TResponse>;
  post<TResponse>(url: string, data?: unknown): Promise<TResponse>;
  put<TResponse>(url: string, data?: unknown): Promise<TResponse>;
  patch<TResponse>(url: string, data?: unknown): Promise<TResponse>;
}
