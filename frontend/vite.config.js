import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * Vite configuration for TradingPagingList frontend.
 *
 * Dev server runs on port 5174.
 * All /api/* requests are proxied to the Spring Boot backend on port 8091,
 * so no CORS issues occur during local development.
 */
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8091',
        changeOrigin: true
      }
    }
  }
})
