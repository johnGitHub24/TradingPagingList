# TradingPagingList — Architecture

## Layer Diagram

```
┌────────────────────────────────────────────────────────────┐
│                      Browser (:5174 dev / :3000 prod)      │
│                                                            │
│  Vue 3 + Vite                                              │
│  ├── App.vue                (root layout, nav bar)         │
│  ├── ProductTable.vue       (table + search + pagination)  │
│  ├── ProductModal.vue       (create / edit form modal)     │
│  ├── Pagination.vue         (reusable page-nav component)  │
│  └── api/products.js        (axios calls via Vite proxy)   │
└───────────────────┬────────────────────────────────────────┘
                    │ HTTP/JSON  (Vite proxy → port 8091)
                    │
┌───────────────────▼────────────────────────────────────────┐
│                Spring Boot 3 (:8091)                       │
│                                                            │
│  ProductController                                         │
│       │  (HTTP translation only, no business logic)        │
│       ▼                                                    │
│  ProductService                                            │
│       │  (@Transactional, all business rules here)         │
│       ▼                                                    │
│  ProductRepository  (extends JpaRepository<Product, Long>) │
│       │                                                    │
│       ▼                                                    │
│  H2 in-memory (dev)  /  PostgreSQL (prod)                  │
└────────────────────────────────────────────────────────────┘
```

## Pagination Flow

1. Vue `ProductTable.vue` sends:
   ```
   GET /api/v1/products?page=0&size=10&sortBy=createdAt&sortDir=desc
   ```

2. `ProductController.listProducts()` constructs a Spring `Pageable`:
   ```java
   Pageable pageable = PageRequest.of(page, effectiveSize, sort);
   ```
   Page size is capped at 100 to prevent unbounded queries.

3. `ProductService.listProducts()` selects the correct repository method
   based on which optional filters (name, category) are present:

   | name | category | Repository call |
   |------|----------|-----------------|
   | -    | -        | `findAll(pageable)` |
   | ✓    | -        | `findByNameContainingIgnoreCase(name, pageable)` |
   | -    | ✓        | `findByCategory(category, pageable)` |
   | ✓    | ✓        | `findByNameContainingIgnoreCaseAndCategory(name, category, pageable)` |

4. Spring Data returns a `Page<Product>`. The service maps it to
   `Page<ProductResponse>` and wraps it in `PageResponse.from(page)`.

5. Controller returns HTTP 200 with the `PageResponse` JSON:
   ```json
   {
     "content":       [...],
     "page":          0,
     "size":          10,
     "totalElements": 50,
     "totalPages":    5,
     "first":         true,
     "last":          false
   }
   ```

6. Vue `Pagination.vue` receives `totalPages` and renders the page-number
   buttons. Clicking a button emits `page-change` to `ProductTable.vue`,
   which updates `currentPage` and fires a new API request.

## DTOs

```
ProductRequest  (inbound)  — @Valid fields, written to DB
ProductResponse (outbound) — read-only view, mapped from entity
PageResponse<T> (outbound) — generic pagination wrapper (Java record)
```

## DataSeeder

`DataSeeder.java` (CommandLineRunner) inserts 50 products across 5
categories on first startup. It checks `repository.count() > 0` before
seeding, so restarting the app with a persistent database is safe.

## Security Notes

- No SQL/JPQL string concatenation; all queries use Spring Data derived
  methods or parameterized `@Query`.
- `@CrossOrigin(origins = "*")` is set for local development convenience.
  Replace with an explicit origin whitelist before production deployment.

## Visual maps

| 文件 | 用途 |
|------|------|
| [codeGraphic.html](codeGraphic.html) | Tab：分頁 API／Vue／篩選／全端（圖為主） |
| [testing.md](testing.md) | Case ID PRODUCT-001～006；`check.ps1` Gate |
