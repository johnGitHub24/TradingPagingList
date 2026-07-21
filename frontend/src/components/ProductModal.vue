<template>
  <!--
    Create / Edit product modal.
    Controlled externally via v-model:visible.
    Emits 'saved' after a successful API call so the parent can reload.
  -->
  <div
    class="modal fade"
    id="productModal"
    tabindex="-1"
    aria-labelledby="productModalLabel"
    aria-hidden="true"
    ref="modalEl"
  >
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <!-- Header -->
        <div class="modal-header bg-primary text-white">
          <h5 class="modal-title" id="productModalLabel">
            <i class="bi" :class="isEdit ? 'bi-pencil-square' : 'bi-plus-circle'"></i>
            {{ isEdit ? 'Edit Product' : 'New Product' }}
          </h5>
          <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>

        <!-- Body -->
        <div class="modal-body">
          <!-- Server-side error alert -->
          <div v-if="serverError" class="alert alert-danger alert-dismissible fade show" role="alert">
            {{ serverError }}
            <button type="button" class="btn-close" @click="serverError = null"></button>
          </div>

          <form id="product-form" novalidate @submit.prevent="handleSubmit">
            <div class="row g-3">
              <!-- Name -->
              <div class="col-12">
                <label for="pName" class="form-label">
                  Name <span class="text-danger">*</span>
                </label>
                <input
                  id="pName"
                  v-model.trim="form.name"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': errors.name }"
                  maxlength="200"
                  placeholder="e.g. MacBook Pro 16&quot;"
                  required
                />
                <div class="invalid-feedback">{{ errors.name }}</div>
              </div>

              <!-- Category -->
              <div class="col-md-4">
                <label for="pCategory" class="form-label">
                  Category <span class="text-danger">*</span>
                </label>
                <select
                  id="pCategory"
                  v-model="form.category"
                  class="form-select"
                  :class="{ 'is-invalid': errors.category }"
                  required
                >
                  <option value="" disabled>Select category…</option>
                  <option v-for="cat in CATEGORIES" :key="cat" :value="cat">{{ cat }}</option>
                </select>
                <div class="invalid-feedback">{{ errors.category }}</div>
              </div>

              <!-- Price -->
              <div class="col-md-4">
                <label for="pPrice" class="form-label">
                  Price (TWD) <span class="text-danger">*</span>
                </label>
                <div class="input-group" :class="{ 'is-invalid': errors.price }">
                  <span class="input-group-text">$</span>
                  <input
                    id="pPrice"
                    v-model.number="form.price"
                    type="number"
                    class="form-control"
                    :class="{ 'is-invalid': errors.price }"
                    step="0.01"
                    min="0.01"
                    placeholder="0.00"
                    required
                  />
                </div>
                <div class="invalid-feedback d-block">{{ errors.price }}</div>
              </div>

              <!-- Stock -->
              <div class="col-md-4">
                <label for="pStock" class="form-label">
                  Stock <span class="text-danger">*</span>
                </label>
                <input
                  id="pStock"
                  v-model.number="form.stock"
                  type="number"
                  class="form-control"
                  :class="{ 'is-invalid': errors.stock }"
                  min="0"
                  placeholder="0"
                  required
                />
                <div class="invalid-feedback">{{ errors.stock }}</div>
              </div>
            </div>
          </form>
        </div>

        <!-- Footer -->
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
            <i class="bi bi-x-circle"></i> Cancel
          </button>
          <button
            type="submit"
            form="product-form"
            class="btn btn-primary"
            :disabled="saving"
          >
            <span v-if="saving" class="spinner-border spinner-border-sm me-1" role="status"></span>
            <i v-else class="bi bi-check-circle me-1"></i>
            {{ saving ? 'Saving…' : 'Save' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * ProductModal — handles both create and edit modes.
 *
 * @prop  {Object|null} product  - null for create mode; product object for edit mode
 * @emits {void} saved           - fired after a successful create or update API call
 */
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { createProduct, updateProduct } from '../api/products.js'

const CATEGORIES = ['ELECTRONICS', 'CLOTHING', 'FOOD', 'SPORTS', 'BOOKS']

const props = defineProps({
  product: { type: Object, default: null }
})

const emit = defineEmits(['saved'])

const modalEl  = ref(null)
const saving   = ref(false)
const serverError = ref(null)

/** True when editing an existing product. */
const isEdit = computed(() => props.product !== null)

/** Reactive form model. */
const form = reactive({ name: '', category: '', price: null, stock: null })

/** Per-field validation error messages. */
const errors = reactive({ name: '', category: '', price: '', stock: '' })

// Sync form when modal is opened for editing
watch(() => props.product, (p) => {
  if (p) {
    form.name     = p.name
    form.category = p.category
    form.price    = p.price
    form.stock    = p.stock
  } else {
    resetForm()
  }
})

onMounted(() => {
  // Clear errors when modal is closed via Bootstrap events
  modalEl.value?.addEventListener('hidden.bs.modal', () => {
    resetForm()
  })
})

/** Opens the Bootstrap modal programmatically. */
function open() {
  nextTick(() => {
    const bsModal = window.bootstrap?.Modal.getOrCreateInstance(modalEl.value)
    bsModal?.show()
  })
}

/** Closes the Bootstrap modal programmatically. */
function close() {
  const bsModal = window.bootstrap?.Modal.getInstance(modalEl.value)
  bsModal?.hide()
}

/** Clears form fields and error messages. */
function resetForm() {
  form.name     = ''
  form.category = ''
  form.price    = null
  form.stock    = null
  errors.name     = ''
  errors.category = ''
  errors.price    = ''
  errors.stock    = ''
  serverError.value = null
}

/** Client-side validation; returns true if form is valid. */
function validate() {
  let valid = true

  errors.name = form.name ? '' : 'Product name is required'
  if (errors.name) valid = false

  errors.category = form.category ? '' : 'Category is required'
  if (errors.category) valid = false

  if (!form.price || form.price <= 0) {
    errors.price = 'Price must be greater than 0'
    valid = false
  } else {
    errors.price = ''
  }

  if (form.stock === null || form.stock === '' || form.stock < 0) {
    errors.stock = 'Stock must be 0 or greater'
    valid = false
  } else {
    errors.stock = ''
  }

  return valid
}

/** Submits the form — creates or updates depending on mode. */
async function handleSubmit() {
  if (!validate()) return

  saving.value = true
  serverError.value = null

  const payload = {
    name:     form.name,
    category: form.category,
    price:    form.price,
    stock:    form.stock
  }

  try {
    if (isEdit.value) {
      await updateProduct(props.product.id, payload)
    } else {
      await createProduct(payload)
    }
    close()
    emit('saved')
  } catch (err) {
    const data = err.response?.data
    if (data?.fieldErrors) {
      Object.assign(errors, data.fieldErrors)
    } else {
      serverError.value = data?.message || 'An unexpected error occurred. Please try again.'
    }
  } finally {
    saving.value = false
  }
}

defineExpose({ open, close })
</script>
