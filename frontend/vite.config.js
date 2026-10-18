import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    // Polyfill for Node.js 'global' so sockjs-client works in the browser.
    global: 'globalThis',
  },
  server: {
    port: 5173,
    proxy: {
      // Optional proxy to avoid CORS during local development.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // WebSocket/STOMP endpoint proxy (SockJS uses HTTP upgrade)
      '/ws-notificaciones': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true
      }
    }
  }
})
