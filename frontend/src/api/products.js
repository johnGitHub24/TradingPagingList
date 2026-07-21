/**
 * Products API client.
 *
 * All functions return Promises. The Vite dev-server proxy forwards
 * /api/* to http://localhost:8091, so these paths work without
 * any CORS configuration during development.
 */
import axios from 'axios'

const BASE = '/api/v1/products'

/**
 * Fetch a paginated, optionally filtered list of products.
 *
 * @param {Object} params
 * @param {number} params.page      - zero-based page index (default 0)
 * @param {number} params.size      - items per page (default 10)
 * @param {string} params.sortBy    - field to sort by (default 'createdAt')
 * @param {string} params.sortDir   - 'asc' | 'desc' (default 'desc')
 * @param {string} [params.name]    - optional name substring filter
 * @param {string} [params.category]- optional category filter
 * @returns {Promise<PageResponse>}
 */
export function fetchProducts(params = {}) {
  return axios.get(BASE, { params }).then((r) => r.data)
}

/**
 * Fetch a single product by its primary key.
 *
 * @param {number} id - product id
 * @returns {Promise<ProductResponse>}
 */
export function fetchProduct(id) {
  return axios.get(`${BASE}/${id}`).then((r) => r.data)
}

/**
 * Create a new product.
 *
 * @param {ProductRequest} payload - { name, category, price, stock }
 * @returns {Promise<ProductResponse>}
 */
export function createProduct(payload) {
  return axios.post(BASE, payload).then((r) => r.data)
}

/**
 * Update an existing product's mutable fields.
 *
 * @param {number} id              - product id to update
 * @param {ProductRequest} payload - { name, category, price, stock }
 * @returns {Promise<ProductResponse>}
 */
export function updateProduct(id, payload) {
  return axios.put(`${BASE}/${id}`, payload).then((r) => r.data)
}

/**
 * Permanently delete a product.
 *
 * @param {number} id - product id to delete
 * @returns {Promise<void>}
 */
export function deleteProduct(id) {
  return axios.delete(`${BASE}/${id}`)
}
