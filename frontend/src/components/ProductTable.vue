<template>
  <!--
    ProductTable — main product listing with server-side pagination,
    search, category filter, and CRUD actions.
  -->
  <div>
    <!-- ── Toolbar ──────────────────────────────────────────────────── -->
    <div class="row g-2 mb-3 align-items-end">
      <!-- Name search -->
      <div class="col-md-4">
        <label for="searchName" class="form-label small text-muted mb-1">Search by name</label>
        <div class="input-group">
          <span class="input-group-text"><i class="bi bi-search"></i></span>
          <input
            id="searchName"
            v-model="searchName"
            type="text"
            class="form-control"
            placeholder="e.g. MacBook…"
            @keyup.enter="applyFilters"
          />
          <button class="btn btn-outline-secondary" @click="applyFilters" title="Search">
            <i class="bi bi-arrow-right"></i>
          </button>
        </div>
      </div>

      <!-- Category filter -->
      <div class="col-md-3">
        <label for="filterCategory" class="form-label small text-muted mb-1">Category</label>
        <select id="filterCategory" v-model="filterCategory" class="form-select" @change="applyFilters">
          <option value="">All categories</option>
          <option v-for="cat in CATEGORIES" :key="cat" :value="cat">{{ cat }}</option>
        </select>
      </div>

      <!-- Page size selector -->
      <div class="col-md-2">
        <label for="pageSize" class="form-label small text-muted mb-1">Per page</label>
        <select id="pageSize" v-model.number="pageSize" class="form-select" @change="changePageSize">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
      </div>

      <!-- Spacer + New Product button -->
      <div class="col-md-3 text-end">
        <button class="btn btn-primary" @click="openCreate">
          <i class="bi bi-plus-circle me-1"></i> New Product
        </button>
      </div>
    </div>

    <!-- ── Table ────────────────────────────────────────────────────── -->
    <div class="table-responsive shadow-sm rounded">
      <table class="table table-striped table-hover align-middle mb-0">
        <thead class="table-dark">
          <tr>
            <th style="width:70px">ID</th>
            <th>Name</th>
            <th>Category</th>
            <th class="text-end">Price (TWD)</th>
            <th class="text-end">Stock</th>
            <th>Created At</th>
            <th style="width:120px" class="text-center">Actions</th>
          </tr>
        </thead>

        <tbody>
          <!-- Loading skeleton -->
          <tr v-if="loading">
            <td colspan="7" class="text-center py-5 text-muted">
              <div class="spinner-border text-primary" role="status"></div>
              <div class="mt-2 small">Loading products…</div>
            </td>
          </tr>

          <!-- Error state -->
          <tr v-else-if="error">
            <td colspan="7" class="text-center py-5">
              <i class="bi bi-exclamation-triangle-fill text-danger fs-2"></i>
              <div class="mt-2 text-danger">{{ error }}</div>
              <button class="btn btn-sm btn-outline-danger mt-2" @click="loadProducts">Retry</button>
            </td>
          </tr>

          <!-- Empty state -->
          <tr v-else-if="products.length === 0">
            <td colspan="7" class="text-center py-5 text-muted">
              <i class="bi bi-inbox fs-2"></i>
              <div class="mt-2">No products found.</div>
            </td>
          </tr>

          <!-- Product rows -->
          <tr v-else v-for="p in products" :key="p.id">
            <td class="text-muted small">{{ p.id }}</td>
            <td>{{ p.name }}</td>
            <td>
              <span class="badge badge-category" :class="categoryBadgeClass(p.category)">
                {{ p.category }}
              </span>
            </td>
            <td class="text-end fw-semibold">{{ formatPrice(p.price) }}</td>
            <td class="text-end">
              <span :class="p.stock === 0 ? 'text-danger' : p.stock < 10 ? 'text-warning' : 'text-success'">
                {{ p.stock }}
              </span>
            </td>
            <td class="text-muted small">{{ formatDate(p.createdAt) }}</td>
            <td class="text-center">
              <button
                class="btn btn-sm btn-outline-primary me-1"
                title="Edit"
                @click="openEdit(p)"
              >
                <i class="bi bi-pencil"></i>
              </button>
              <button
                class="btn btn-sm btn-outline-danger"
                title="Delete"
                @click="confirmDelete(p)"
              >
                <i class="bi bi-trash"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ── Pagination ────────────────────────────────────────────────── -->
    <Pagination
      v-if="!loading && !error"
      :current-page="currentPage"
      :total-pages="totalPages"
      :total-elements="totalElements"
      :page-size="pageSize"
      @page-change="goToPage"
    />

    <!-- ── Product Modal ─────────────────────────────────────────────── -->
    <ProductModal
      ref="modalRef"
      :product="editingProduct"
      @saved="onSaved"
    />

    <!-- ── Toast Notifications ───────────────────────────────────────── -->
    <div class="position-fixed bottom-0 end-0 p-3" style="z-index: 1100">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="toast show align-items-center text-white border-0 mb-2"
        :class="`bg-${toast.type}`"
        role="alert"
      >
        <div class="d-flex">
          <div class="toast-body">{{ toast.message }}</div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" @click="removeToast(toast.id)"></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * ProductTable component.
 *
 * Manages all state for the product listing page:
 *  - server-side pagination (default pageSize = 10)
 *  - name and category filters
 *  - CRUD actions via ProductModal
 *  - delete confirmation and toast feedback
 */
import { ref, reactive, onMounted } from 'vue'
import { fetchProducts, deleteProduct } from '../api/products.js'
import Pagination   from './Pagination.vue'
import ProductModal from './ProductModal.vue'

const CATEGORIES = ['ELECTRONICS', 'CLOTHING', 'FOOD', 'SPORTS', 'BOOKS']

// ── State ─────────────────────────────────────────────────────────────────

const products      = ref([])
const loading       = ref(false)
const error         = ref(null)
const currentPage   = ref(0)
const pageSize      = ref(10)          // default 10 items per page
const totalElements = ref(0)
const totalPages    = ref(0)
const searchName    = ref('')
const filterCategory= ref('')
const editingProduct= ref(null)
const modalRef      = ref(null)

/** Array of { id, message, type } objects for transient toast notifications. */
const toasts = reactive([])
let toastCounter = 0

// ── Lifecycle ─────────────────────────────────────────────────────────────

onMounted(loadProducts)

// ── Data Loading ──────────────────────────────────────────────────────────

/**
 * Fetches the current page from the backend, applying name/category filters.
 * Updates all pagination state from the PageResponse.
 */
async function loadProducts() {
  loading.value = true
  error.value   = null

  const params = {
    page:    currentPage.value,
    size:    pageSize.value,
    sortBy:  'createdAt',
    sortDir: 'desc'
  }
  if (searchName.value.trim())   params.name     = searchName.value.trim()
  if (filterCategory.value)      params.category = filterCategory.value

  try {
    const data = await fetchProducts(params)
    products.value      = data.content
    totalElements.value = data.totalElements
    totalPages.value    = data.totalPages
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load products. Is the backend running?'
  } finally {
    loading.value = false
  }
}

// ── Pagination ────────────────────────────────────────────────────────────

/** Navigates to a specific zero-based page index. */
function goToPage(page) {
  currentPage.value = page
  loadProducts()
}

/** Resets to page 0 when page size changes. */
function changePageSize() {
  currentPage.value = 0
  loadProducts()
}

/** Applies current filter values and resets to first page. */
function applyFilters() {
  currentPage.value = 0
  loadProducts()
}

// ── CRUD Actions ──────────────────────────────────────────────────────────

/** Opens the modal in create mode. */
function openCreate() {
  editingProduct.value = null
  modalRef.value?.open()
}

/** Opens the modal in edit mode with the given product pre-populated. */
function openEdit(product) {
  editingProduct.value = product
  modalRef.value?.open()
}

/** Asks for confirmation, then deletes the product and reloads the list. */
async function confirmDelete(product) {
  if (!confirm(`Delete "${product.name}"? This cannot be undone.`)) return

  try {
    await deleteProduct(product.id)
    showToast(`"${product.name}" deleted`, 'warning')

    // If we deleted the last item on a non-first page, go back one page
    if (products.value.length === 1 && currentPage.value > 0) {
      currentPage.value--
    }
    await loadProducts()
  } catch (err) {
    showToast(err.response?.data?.message || 'Failed to delete product', 'danger')
  }
}

/** Called after the modal emits 'saved'; reloads the list. */
async function onSaved() {
  showToast('Product saved successfully', 'success')
  await loadProducts()
}

// ── Helpers ───────────────────────────────────────────────────────────────

/**
 * Maps a product category string to a Bootstrap badge color class.
 *
 * @param {string} category - the category enum value
 * @returns {string} Bootstrap bg-* class
 */
function categoryBadgeClass(category) {
  const map = {
    ELECTRONICS: 'bg-primary',
    CLOTHING:    'bg-info text-dark',
    FOOD:        'bg-success',
    SPORTS:      'bg-warning text-dark',
    BOOKS:       'bg-secondary'
  }
  return map[category] || 'bg-dark'
}

/**
 * Formats a price number as a localized TWD string.
 *
 * @param {number} price
 * @returns {string} e.g. "1,299.00"
 */
function formatPrice(price) {
  return Number(price).toLocaleString('zh-TW', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/**
 * Formats an ISO date-time string as a short locale date.
 *
 * @param {string} iso - ISO 8601 date-time string from the API
 * @returns {string} e.g. "2024/3/15"
 */
function formatDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('zh-TW')
}

/**
 * Displays a transient toast notification.
 *
 * @param {string} message - text to display
 * @param {'success'|'danger'|'warning'|'info'} type - Bootstrap color variant
 */
function showToast(message, type = 'info') {
  const id = ++toastCounter
  toasts.push({ id, message, type })
  setTimeout(() => removeToast(id), 3500)
}

/** Removes a toast by its unique id. */
function removeToast(id) {
  const idx = toasts.findIndex((t) => t.id === id)
  if (idx !== -1) toasts.splice(idx, 1)
}
</script>
