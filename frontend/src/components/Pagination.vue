<template>
  <!--
    Reusable pagination bar component.
    Emits 'page-change' with the new zero-based page index when the user
    clicks a page button, or the previous/next arrows.
  -->
  <nav v-if="totalPages > 1" aria-label="Product pagination" class="d-flex align-items-center gap-3 mt-3">
    <!-- Info label -->
    <span class="text-muted small">
      Showing {{ from }}–{{ to }} of {{ totalElements }} items
    </span>

    <!-- Page buttons -->
    <ul class="pagination pagination-sm mb-0 ms-auto">
      <!-- Previous -->
      <li class="page-item" :class="{ disabled: currentPage === 0 }">
        <button class="page-link" @click="emit('page-change', currentPage - 1)" :disabled="currentPage === 0">
          <i class="bi bi-chevron-left"></i>
        </button>
      </li>

      <!-- Page numbers (show a sliding window of up to 7 pages) -->
      <li
        v-for="p in visiblePages"
        :key="p"
        class="page-item"
        :class="{ active: p === currentPage, disabled: p === '…' }"
      >
        <button
          v-if="p !== '…'"
          class="page-link"
          @click="emit('page-change', p)"
        >{{ p + 1 }}</button>
        <span v-else class="page-link">…</span>
      </li>

      <!-- Next -->
      <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
        <button
          class="page-link"
          @click="emit('page-change', currentPage + 1)"
          :disabled="currentPage === totalPages - 1"
        >
          <i class="bi bi-chevron-right"></i>
        </button>
      </li>
    </ul>
  </nav>
</template>

<script setup>
/**
 * Pagination component props and emits.
 *
 * @prop {number} currentPage    - zero-based index of the currently displayed page
 * @prop {number} totalPages     - total number of pages
 * @prop {number} totalElements  - total number of records across all pages
 * @prop {number} pageSize       - number of items per page
 *
 * @emits {number} page-change   - new zero-based page index when user navigates
 */
import { computed } from 'vue'

const props = defineProps({
  currentPage: { type: Number, required: true },
  totalPages:  { type: Number, required: true },
  totalElements: { type: Number, required: true },
  pageSize:    { type: Number, required: true }
})

const emit = defineEmits(['page-change'])

/** Human-readable first item index (1-based). */
const from = computed(() => props.totalElements === 0 ? 0 : props.currentPage * props.pageSize + 1)

/** Human-readable last item index (1-based). */
const to = computed(() => Math.min((props.currentPage + 1) * props.pageSize, props.totalElements))

/**
 * Returns an array of page indices (numbers) and ellipsis placeholders ('…')
 * to display, showing up to 7 entries in a sliding window.
 *
 * Example for 20 pages at page 10:  [0, '…', 8, 9, 10, 11, 12, '…', 19]
 */
const visiblePages = computed(() => {
  const total = props.totalPages
  const current = props.currentPage

  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i)
  }

  const pages = []

  // Always show first page
  pages.push(0)

  if (current > 3) pages.push('…')

  // Sliding window of 3 pages around current
  const start = Math.max(1, current - 2)
  const end   = Math.min(total - 2, current + 2)
  for (let i = start; i <= end; i++) pages.push(i)

  if (current < total - 4) pages.push('…')

  // Always show last page
  pages.push(total - 1)

  return pages
})
</script>
