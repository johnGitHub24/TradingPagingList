package com.trading.paginglist.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.paginglist.product.domain.ProductCategory;
import com.trading.paginglist.product.dto.ProductRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link ProductController}.
 *
 * <p>Runs against a full Spring Boot context with an in-memory H2 database.
 * The DataSeeder populates 50 products on startup, giving predictable
 * pagination totals. Tests are tagged {@code "integration"} so they run
 * only via the {@code integrationTest} Gradle task.</p>
 *
 * <p>Run with: {@code ./gradlew integrationTest}</p>
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = "startup.info.enabled=false")
@DisplayName("ProductController Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/products";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /api/v1/products (pagination) ────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("GET /api/v1/products — returns HTTP 200 with paginated response")
    void listProducts_defaultPagination_returns200WithPageResponse() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(50)))
                .andExpect(jsonPath("$.totalPages").value(greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.content.length()").value(10));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/products?size=5 — returns exactly 5 items per page")
    void listProducts_customPageSize_returnsCorrectPageSize() throws Exception {
        mockMvc.perform(get(BASE_URL).param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalPages").value(greaterThanOrEqualTo(10)));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/products?page=1&size=10 — returns second page")
    void listProducts_page1_returnsSecondPage() throws Exception {
        mockMvc.perform(get(BASE_URL).param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.content.length()").value(10));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/products?name=MacBook — filters by name substring")
    void listProducts_nameFilter_returnsFilteredResults() throws Exception {
        mockMvc.perform(get(BASE_URL).param("name", "MacBook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].name", containsString("MacBook")));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/products?category=ELECTRONICS — filters by category")
    void listProducts_categoryFilter_returnsOnlyElectronics() throws Exception {
        mockMvc.perform(get(BASE_URL).param("category", "ELECTRONICS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.content[0].category").value("ELECTRONICS"));
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/products?size=200 — caps page size at 100")
    void listProducts_oversizedPageSize_capsAt100() throws Exception {
        mockMvc.perform(get(BASE_URL).param("size", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(lessThanOrEqualTo(100)));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/products?name=Mac&category=ELECTRONICS — combined filter")
    void listProducts_nameAndCategoryFilter_returnsIntersection() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("name", "Mac")
                        .param("category", "ELECTRONICS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].name", containsString("Mac")))
                .andExpect(jsonPath("$.content[0].category").value("ELECTRONICS"));
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/v1/products?sortBy=price&sortDir=asc — sorts ascending by price")
    void listProducts_sortByPriceAsc_returnsOrderedPrices() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL)
                        .param("size", "5")
                        .param("sortBy", "price")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andReturn();

        var content = objectMapper.readTree(result.getResponse().getContentAsString()).get("content");
        BigDecimal prev = BigDecimal.ZERO;
        for (int i = 0; i < content.size(); i++) {
            BigDecimal price = content.get(i).get("price").decimalValue();
            assertThat(price).isGreaterThanOrEqualTo(prev);
            prev = price;
        }
    }

    @Test
    @Order(9)
    @DisplayName("GET last page — last=true and content size matches remainder")
    void listProducts_lastPage_setsLastFlag() throws Exception {
        MvcResult first = mockMvc.perform(get(BASE_URL).param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();
        int totalPages = objectMapper.readTree(first.getResponse().getContentAsString())
                .get("totalPages").asInt();
        assertThat(totalPages).isGreaterThanOrEqualTo(1);

        mockMvc.perform(get(BASE_URL)
                        .param("page", String.valueOf(totalPages - 1))
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(totalPages - 1))
                .andExpect(jsonPath("$.last").value(true));
    }

    // ── POST /api/v1/products ─────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("POST /api/v1/products — creates product and returns HTTP 201")
    void createProduct_validRequest_returns201() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Integration Test Product");
        request.setCategory(ProductCategory.SPORTS);
        request.setPrice(new BigDecimal("299.99"));
        request.setStock(5);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Integration Test Product"))
                .andExpect(jsonPath("$.category").value("SPORTS"))
                .andExpect(jsonPath("$.price").value(299.99))
                .andExpect(jsonPath("$.stock").value(5));
    }

    @Test
    @Order(11)
    @DisplayName("POST /api/v1/products — returns HTTP 422 when name is blank")
    void createProduct_blankName_returns422() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("");
        request.setCategory(ProductCategory.FOOD);
        request.setPrice(new BigDecimal("10.00"));
        request.setStock(10);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.name").isNotEmpty());
    }

    @Test
    @Order(12)
    @DisplayName("POST /api/v1/products — returns HTTP 422 when price is negative")
    void createProduct_negativePrice_returns422() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Bad Product");
        request.setCategory(ProductCategory.CLOTHING);
        request.setPrice(new BigDecimal("-1.00"));
        request.setStock(10);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.price").isNotEmpty());
    }

    @Test
    @Order(13)
    @DisplayName("POST /api/v1/products — returns HTTP 422 when stock is negative")
    void createProduct_negativeStock_returns422() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Bad Stock");
        request.setCategory(ProductCategory.FOOD);
        request.setPrice(new BigDecimal("10.00"));
        request.setStock(-1);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.stock").isNotEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("POST /api/v1/products — returns HTTP 422 when category is null")
    void createProduct_nullCategory_returns422() throws Exception {
        String json = """
                {"name":"No Category","category":null,"price":10.00,"stock":1}
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.category").isNotEmpty());
    }

    // ── GET /api/v1/products/{id} ─────────────────────────────────────────

    @Test
    @Order(20)
    @DisplayName("GET /api/v1/products/{id} — returns product when it exists")
    void getProduct_existingId_returns200() throws Exception {
        // Create a product first to get a valid id
        ProductRequest request = new ProductRequest();
        request.setName("Get By Id Product");
        request.setCategory(ProductCategory.BOOKS);
        request.setPrice(new BigDecimal("499.00"));
        request.setStock(20);

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Get By Id Product"));
    }

    @Test
    @Order(21)
    @DisplayName("GET /api/v1/products/{id} — returns HTTP 404 for non-existent id")
    void getProduct_nonExistentId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ── PUT /api/v1/products/{id} ─────────────────────────────────────────

    @Test
    @Order(30)
    @DisplayName("PUT /api/v1/products/{id} — updates and returns HTTP 200")
    void updateProduct_validRequest_returns200() throws Exception {
        // Create product
        ProductRequest create = new ProductRequest();
        create.setName("Before Update");
        create.setCategory(ProductCategory.FOOD);
        create.setPrice(new BigDecimal("100.00"));
        create.setStock(10);

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andReturn();
        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Update it
        ProductRequest update = new ProductRequest();
        update.setName("After Update");
        update.setCategory(ProductCategory.SPORTS);
        update.setPrice(new BigDecimal("250.00"));
        update.setStock(5);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("After Update"))
                .andExpect(jsonPath("$.category").value("SPORTS"))
                .andExpect(jsonPath("$.price").value(250.00))
                .andExpect(jsonPath("$.stock").value(5));
    }

    @Test
    @Order(31)
    @DisplayName("PUT /api/v1/products/{id} — returns HTTP 404 for non-existent id")
    void updateProduct_nonExistentId_returns404() throws Exception {
        ProductRequest update = new ProductRequest();
        update.setName("Ghost");
        update.setCategory(ProductCategory.BOOKS);
        update.setPrice(new BigDecimal("10.00"));
        update.setStock(1);

        mockMvc.perform(put(BASE_URL + "/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ── DELETE /api/v1/products/{id} ──────────────────────────────────────

    @Test
    @Order(40)
    @DisplayName("DELETE /api/v1/products/{id} — deletes product and returns HTTP 204")
    void deleteProduct_existingId_returns204() throws Exception {
        // Create product to delete
        ProductRequest create = new ProductRequest();
        create.setName("To Be Deleted");
        create.setCategory(ProductCategory.CLOTHING);
        create.setPrice(new BigDecimal("50.00"));
        create.setStock(1);

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andReturn();
        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Delete it
        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(41)
    @DisplayName("DELETE /api/v1/products/{id} — returns HTTP 404 for non-existent id")
    void deleteProduct_nonExistentId_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/999999"))
                .andExpect(status().isNotFound());
    }

    // ── Actuator ──────────────────────────────────────────────────────────

    @Test
    @Order(50)
    @DisplayName("GET /actuator/health — returns UP")
    void actuatorHealth_returnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
