import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';
import tailwindcss from '@tailwindcss/vite';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
    define: {
    // Polyfill for Node.js 'global' so sockjs-client works in the browser.
    global: 'globalThis',
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      // Optional proxy to avoid CORS during local development.
      '/api': {
        changeOrigin: true,
        target: 'http://localhost:8080'
      },
      // WebSocket/STOMP endpoint proxy (SockJS uses HTTP upgrade)
      '/ws-notificaciones': {
        changeOrigin: true,
        target: 'http://localhost:8080',
        ws: true
      }
    }
  }
})
