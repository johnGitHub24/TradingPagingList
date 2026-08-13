# TradingPagingList — Testing Guide

## Test Layers

| Layer | Class | Tool | Tag |
|-------|-------|------|-----|
| Unit | `ProductServiceTest` | JUnit 5 + Mockito | (none — runs by default) |
| Integration | `ProductControllerIntegrationTest` | @SpringBootTest + MockMvc | `integration` |

---

## Running Unit Tests

Unit tests use Mockito stubs and never touch the database. They are the
default `test` task and are excluded from the `integrationTest` task.

```bash
./gradlew test
```

Report: `build/reports/tests/test/index.html`

### What is tested (ProductServiceTest)

- `listProducts()` — no filter, name filter, category filter, combined filter,
  empty result, blank name treated as absent
- `getProduct()` — happy path, not found → ResourceNotFoundException
- `createProduct()` — happy path, entity saved with correct fields
- `updateProduct()` — happy path, not found → ResourceNotFoundException
- `deleteProduct()` — happy path, not found → ResourceNotFoundException,
  delete() not called when not found

---

## Running Integration Tests

Integration tests start a full Spring Boot context with an H2 in-memory
database. The DataSeeder populates 50 products, giving predictable pagination
totals. Each test run is independent because H2 is re-created per context.

```bash
./gradlew integrationTest
```

Report: `build/reports/tests/integrationTest/index.html`

### What is tested (ProductControllerIntegrationTest)

- `GET /api/v1/products` — HTTP 200, correct `page`/`size`/`totalElements`/`content.length`
- `GET /api/v1/products?size=5` — custom page size respected
- `GET /api/v1/products?page=1` — second page returned
- `GET /api/v1/products?name=MacBook` — name filter applied
- `GET /api/v1/products?category=ELECTRONICS` — returns only 10 electronics
- `GET /api/v1/products?size=200` — capped at 100
- `GET` name + category combined filter
- `GET` `sortBy=price&sortDir=asc` — ascending price order
- `GET` last page — `last=true`
- `POST /api/v1/products` — HTTP 201 with new product body
- `POST` blank name → HTTP 422 with `fieldErrors.name`
- `POST` negative price → HTTP 422 with `fieldErrors.price`
- `POST` negative stock / null category → HTTP 422
- `GET /api/v1/products/{id}` — HTTP 200 for existing, 404 for missing
- `PUT /api/v1/products/{id}` — HTTP 200 with updated fields；missing id → 404
- `DELETE /api/v1/products/{id}` — HTTP 204; subsequent GET returns 404
- `DELETE` non-existent id → HTTP 404
- `GET /actuator/health` — UP

### Encoding / Console (unit)

- `ConsoleCharset` — 尊重 stdout.encoding（含 UTF-8），不再降級 MS950
- `StartupInfoLogger` — ASCII banner + Vite :5174 啟動提示（`start-frontend.ps1`）

---

## Running All Tests Together

```bash
./gradlew test integrationTest
```

---

## H2 Console (dev only)

While the app is running, visit:
```
http://localhost:8091/h2-console
```

JDBC URL: `jdbc:h2:mem:paginglist`  
Username: `sa`  
Password: (leave blank)
