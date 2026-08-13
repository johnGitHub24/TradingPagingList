# TradingPagingList — Testing Guide

驗證入口（Gate）：`.\scripts\check.ps1`（`gradlew check`＝unit + integration）。  
前端 Vite `:5174`／npm **不**在 Gate 內。

## Test Layers

| Layer | Class | Tool | Tag |
|-------|-------|------|-----|
| Unit | `ProductServiceTest` | JUnit 5 + Mockito | (none — runs by default) |
| Integration | `integration/ProductControllerIntegrationTest` | @SpringBootTest + MockMvc | `integration` |

---

## Case IDs（單元 ↔ 整合成對）

同一 Case ID 兩層描述同一契約。PRODUCT-006 涵蓋 get／update／delete 找不到。

| Case ID | 行為 | Unit | Integration |
|---------|------|------|-------------|
| PRODUCT-001 | create 合法請求，回傳已指派 id | `createProduct_validRequest_returnsCreatedProduct` | POST → HTTP 201 |
| PRODUCT-002 | get 既有 id | `getProduct_existingId_returnsResponse` | GET `/{id}` → HTTP 200 |
| PRODUCT-003 | list／分頁（預設 page=0、size=10） | `listProducts_noFilter_returnsPageResponse` | GET `/` → HTTP 200 + page meta |
| PRODUCT-004 | update 覆寫可變欄位 | `updateProduct_existingId_returnsUpdatedProduct` | PUT `/{id}` → HTTP 200 |
| PRODUCT-005 | delete 既有 id | `deleteProduct_existingId_deletesSuccessfully` | DELETE `/{id}` → HTTP 204 |
| PRODUCT-006 | 找不到 → 404 | `ResourceNotFoundException`（get／update／delete） | HTTP 404（get／update／delete） |

掃描：`eos-minimal/hooks/scan-paired-tests.ps1 -ProjectRoot . -WarnOnly`

---

## Running Unit Tests

Unit tests use Mockito stubs and never touch the database. They are the
default `test` task and are excluded from the `integrationTest` task.

```bash
./gradlew test
```

Report: `build/reports/tests/test/index.html`

### What is tested (ProductServiceTest)

- PRODUCT-003 `listProducts()` — no filter, name filter, category filter, combined filter,
  empty result, blank name treated as absent
- PRODUCT-002／006 `getProduct()` — happy path, not found → ResourceNotFoundException
- PRODUCT-001 `createProduct()` — happy path, entity saved with correct fields
- PRODUCT-004／006 `updateProduct()` — happy path, not found → ResourceNotFoundException
- PRODUCT-005／006 `deleteProduct()` — happy path, not found → ResourceNotFoundException,
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

- PRODUCT-003 `GET /api/v1/products` — HTTP 200, correct `page`/`size`/`totalElements`/`content.length`
- `GET /api/v1/products?size=5` — custom page size respected
- `GET /api/v1/products?page=1` — second page returned
- `GET /api/v1/products?name=MacBook` — name filter applied
- `GET /api/v1/products?category=ELECTRONICS` — returns only 10 electronics
- `GET /api/v1/products?size=200` — capped at 100
- `GET` name + category combined filter
- `GET` `sortBy=price&sortDir=asc` — ascending price order
- `GET` last page — `last=true`
- PRODUCT-001 `POST /api/v1/products` — HTTP 201 with new product body
- `POST` blank name → HTTP 422 with `fieldErrors.name`
- `POST` negative price → HTTP 422 with `fieldErrors.price`
- `POST` negative stock / null category → HTTP 422
- PRODUCT-002／006 `GET /api/v1/products/{id}` — HTTP 200 for existing, 404 for missing
- PRODUCT-004／006 `PUT /api/v1/products/{id}` — HTTP 200 with updated fields；missing id → 404
- PRODUCT-005／006 `DELETE /api/v1/products/{id}` — HTTP 204; subsequent GET returns 404；missing id → 404
- `GET /actuator/health` — UP

### Encoding / Console (unit)

- `ConsoleCharset` — 尊重 stdout.encoding（含 UTF-8），不再降級 MS950
- `StartupInfoLogger` — ASCII banner + Vite :5174 啟動提示（`npm run dev`）

---

## Running All Tests Together

```powershell
.\scripts\check.ps1
```

等同 `.\gradlew.bat check`（unit + integration）。

---

## H2 Console (dev only)

While the app is running, visit:
```
http://localhost:8091/h2-console
```

JDBC URL: `jdbc:h2:mem:paginglist`  
Username: `sa`  
Password: (leave blank)
