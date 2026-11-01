import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';
import tailwindcss from '@tailwindcss/vite';
import { resolve } from 'path';

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
      // SockJS info and WebSocket transport for the versioned endpoint.
      '/api/v1/ws-notificaciones': {
        changeOrigin: true,
        target: 'http://localhost:8080',
        ws: true
      },
      // Optional proxy to avoid CORS during local development.
      '/api': {
        changeOrigin: true,
        target: 'http://localhost:8080'
      }
    }
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
    },
  },
})
