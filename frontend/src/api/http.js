import axios from 'axios'

const baseURL = import.meta.env.VITE_API_URL || import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const http = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 15000
})

/**
 * Request interceptor: attaches the JWT Bearer token to every request if present.
 */
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('sg_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/**
 * Response interceptor: handles 401 by clearing session and forcing login redirect.
 * Returns a rejected promise with a normalized error shape {status, message, path, errors}.
 */
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('sg_token')
      localStorage.removeItem('sg_usuario')
      // Avoid redirect loop if already on /login
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }

    const normalized = {
      status: error.response?.status || 0,
      message:
        error.response?.data?.message ||
        error.response?.data?.error ||
        'Ocurrió un error inesperado. Inténtalo de nuevo.',
      path: error.response?.data?.path || null,
      errors: error.response?.data?.errors || null,
      timestamp: error.response?.data?.timestamp || null
    }
    return Promise.reject(normalized)
  }
)

export default http
