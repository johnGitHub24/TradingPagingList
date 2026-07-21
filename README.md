# TradingPagingList

Full-stack CRUD demo with **server-side pagination** — Spring Boot 3 (JPA) backend + Vue 3 (Vite) frontend.

## 文件入口

| 文件 | 說明 |
|------|------|
| [docs/codeGraphic.html](docs/codeGraphic.html) | Tab 式架構圖（圖為主） |
| [docs/architecture.md](docs/architecture.md) | 分層與模組 |
| [CLAUDE.md](CLAUDE.md) | AI／工程薄規則 |

## Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Vue 3 · Vite · Axios · Bootstrap 5 (CDN) |
| Backend | Spring Boot 3.2.2 · Spring Data JPA · H2 (dev) · PostgreSQL (prod) |
| Build | Gradle (Groovy DSL) · Java 21 |

---

## Quick Start

本專案是**前後端分離**：後端 `:8091` 與前端 Vite `:5174` 必須**各開一個行程**。  
只啟動後端時，開 `http://localhost:5174/` 會連不上（這是正常的）。

### 一鍵（建議）

```powershell
cd D:/ClaudeCode/TradingPagingList
.\scripts\start-all.ps1
```

會開兩個視窗；等 Vite 印出 `Local: http://localhost:5174/` 後，用瀏覽器開：

**http://localhost:5174/**

### 分開啟動

```powershell
# 視窗 1 — 後端
.\scripts\bootRun.ps1

# 視窗 2 — 前端（需先有 Node.js；首次會自動 npm install）
.\scripts\start-frontend.ps1
```

或手動：

```bash
# backend
./gradlew bootRun

# frontend
cd frontend
npm install
npm run dev
```

後端：**http://localhost:8091**（DataSeeder 會插入 50 筆樣本商品）  
前端：**http://localhost:5174**（`/api` 由 Vite 代理到 8091）

> Console banner 已改為 ASCII 英文（避免 Windows CP950 把框線／中文變成 `?`）。

Useful URLs while running:

| URL | Description |
|-----|-------------|
| http://localhost:5174/ | Vue 前端（主要操作畫面） |
| http://localhost:8091/swagger-ui.html | Interactive API docs |
| http://localhost:8091/h2-console | H2 database console (dev only) |
| http://localhost:8091/actuator/health | Health check |

### 2. Start the Frontend (dev mode) — 見上方

若你已用 `start-all.ps1`／`start-frontend.ps1`，可略過本節。

```bash
cd D:/ClaudeCode/TradingPagingList/frontend
npm install
npm run dev
```

Frontend available at **http://localhost:5174**.  
Vite proxies all `/api/*` requests to the backend, so no CORS issues.

---

## REST API Endpoints

Base path: `/api/v1/products`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/products` | Paginated product list |
| GET | `/api/v1/products/{id}` | Single product |
| POST | `/api/v1/products` | Create product |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Delete product |

### Pagination Parameters (GET /api/v1/products)

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Zero-based page index |
| `size` | `10` | Items per page (max 100) |
| `sortBy` | `createdAt` | Field to sort by |
| `sortDir` | `desc` | `asc` or `desc` |
| `name` | — | Optional name substring filter (case-insensitive) |
| `category` | — | Optional category filter (`ELECTRONICS`, `CLOTHING`, `FOOD`, `SPORTS`, `BOOKS`) |

### Paginated Response Format

```json
{
  "content": [
    {
      "id": 1,
      "name": "MacBook Pro 16\"",
      "category": "ELECTRONICS",
      "price": 89900.00,
      "stock": 15,
      "createdAt": "2024-03-15T10:30:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 50,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

---

## How Pagination Works

1. **Default page size** is 10 items. The frontend lets users switch to 20 or 50.
2. The controller builds a Spring `Pageable` from the `page`, `size`, `sortBy`,
   and `sortDir` query parameters. Page size is capped at 100 server-side.
3. `ProductService` delegates to the appropriate `ProductRepository` derived
   query method depending on which optional filters are present.
4. Spring Data returns a `Page<Product>` which is mapped to the generic
   `PageResponse<ProductResponse>` record and serialized as JSON.
5. The Vue `Pagination.vue` component renders a sliding window of page buttons
   using `totalPages` from the response, and emits `page-change` events to
   trigger new API calls.

See [docs/architecture.md](docs/architecture.md) for the full layer diagram.

---

## Running Tests

```bash
# Unit tests (fast, no database)
./gradlew test

# Integration tests (full Spring context + H2)
./gradlew integrationTest

# Both
./gradlew test integrationTest
```

Reports: `build/reports/tests/`

See [docs/testing.md](docs/testing.md) for details on what each test covers.

---

## Production Build (BFF Mode)

```bash
# Build the Vue app
cd frontend && npm run build

# Start the Express BFF (serves Vue + proxies /api to Spring Boot)
cd ../server && npm install && npm start
# → http://localhost:3000
```

---

## Project Structure

```
TradingPagingList/
├── src/main/java/com/trading/paginglist/
│   ├── TradingPagingListApplication.java
│   ├── product/
│   │   ├── ProductController.java
│   │   ├── ProductService.java
│   │   ├── ProductRepository.java
│   │   ├── domain/
│   │   │   ├── Product.java
│   │   │   └── ProductCategory.java
│   │   └── dto/
│   │       ├── ProductRequest.java
│   │       ├── ProductResponse.java
│   │       └── PageResponse.java
│   ├── common/
│   │   ├── GlobalExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   └── config/
│       ├── DataSeeder.java
│       └── OpenApiConfig.java
├── src/main/resources/application.yml
├── src/test/java/com/trading/paginglist/product/
│   ├── ProductServiceTest.java
│   └── ProductControllerIntegrationTest.java
├── frontend/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── api/products.js
│       └── components/
│           ├── ProductTable.vue
│           ├── ProductModal.vue
│           └── Pagination.vue
├── server/
│   ├── package.json
│   └── server.js
├── docs/
│   ├── architecture.md
│   └── testing.md
├── build.gradle
├── settings.gradle
└── README.md
```
