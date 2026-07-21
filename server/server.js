/**
 * BFF (Backend-For-Frontend) Express server.
 *
 * Serves the built Vue application from ../frontend/dist and
 * proxies all /api/* requests to the Spring Boot backend at port 8091.
 *
 * Usage (after building frontend):
 *   cd frontend && npm run build
 *   cd ../server && npm install && npm start
 *
 * The BFF listens on port 3000 by default.
 */

const express   = require('express')
const path      = require('path')
const { createProxyMiddleware } = require('http-proxy-middleware')

const app  = express()
const PORT = process.env.PORT || 3000
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8091'

// Proxy /api/* → Spring Boot backend
app.use(
  '/api',
  createProxyMiddleware({
    target: BACKEND_URL,
    changeOrigin: true,
    logLevel: 'warn'
  })
)

// Serve built Vue dist files
const distDir = path.join(__dirname, '..', 'frontend', 'dist')
app.use(express.static(distDir))

// SPA fallback — Vue Router history mode support
app.get('*', (req, res) => {
  res.sendFile(path.join(distDir, 'index.html'))
})

app.listen(PORT, () => {
  console.log(`BFF server listening on http://localhost:${PORT}`)
  console.log(`Proxying /api/* → ${BACKEND_URL}`)
})
