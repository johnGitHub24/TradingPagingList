<template>
  <!-- Root application layout -->
  <div>
    <!-- Navigation bar -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
      <div class="container-fluid">
        <a class="navbar-brand" href="#">
          <i class="bi bi-bar-chart-line-fill me-2 text-primary"></i>
          TradingPagingList
        </a>
        <span class="navbar-text text-secondary small">
          Spring Boot 3 · Vue 3 · Server-Side Pagination
        </span>
      </div>
    </nav>

    <!-- Main content -->
    <div class="container-fluid py-4 px-4">
      <div class="d-flex align-items-center mb-4">
        <h2 class="mb-0 fw-bold">
          <i class="bi bi-box-seam me-2 text-primary"></i>
          Product Management
        </h2>
        <span class="badge bg-primary ms-3">Default 10 / page</span>
      </div>

      <ProductTable />
    </div>
  </div>
</template>

<script setup>
/**
 * App.vue — root component.
 *
 * Provides the navigation bar and page header, then delegates
 * all product management functionality to the ProductTable component.
 */
import ProductTable from './components/ProductTable.vue'
</script>
