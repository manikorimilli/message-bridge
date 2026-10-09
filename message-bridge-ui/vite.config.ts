import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

/** The backend. vercel.json forwards /v1 to the same place in production. */
const API_ORIGIN = 'https://message-bridge.onrender.com'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, './src'),
    },
  },
  server: {
    // Same-origin calls in development: the browser talks to Vite, Vite talks to the API.
    proxy: {
      '/v1': { target: API_ORIGIN, changeOrigin: true },
    },
  },
})
