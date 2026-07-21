package com.trading.paginglist.product;

import com.trading.paginglist.product.domain.ProductCategory;
import com.trading.paginglist.product.dto.PageResponse;
import com.trading.paginglist.product.dto.ProductRequest;
import com.trading.paginglist.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing product CRUD and pagination endpoints.
 *
 * <p>Responsibilities are limited to HTTP translation: parsing request parameters,
 * delegating to {@link ProductService}, and mapping results to HTTP responses.
 * No business logic lives here.</p>
 *
 * <p>Base path: {@code /api/v1/products}</p>
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product CRUD with server-side pagination")
@CrossOrigin(origins = "*")
public class ProductController {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductService productService;

    /**
     * Returns a paginated list of products, optionally filtered by name and/or category.
     *
     * <p>Defaults: page 0, size 10, sorted by {@code createdAt} descending.
     * The {@code size} parameter is capped at {@value MAX_PAGE_SIZE} to prevent
     * unbounded queries.</p>
     *
     * @param name     optional case-insensitive name substring filter
     * @param category optional category enum filter
     * @param page     zero-based page number (default 0)
     * @param size     page size (default 10, max 100)
     * @param sortBy   field to sort by (default {@code createdAt})
     * @param sortDir  sort direction: {@code asc} or {@code desc} (default {@code desc})
     * @return paginated product list wrapped in {@link PageResponse}
     */
    @GetMapping
    @Operation(summary = "List products with pagination and optional filters")
    public ResponseEntity<PageResponse<ProductResponse>> listProducts(
            @Parameter(description = "Filter by name substring (case-insensitive)")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by product category")
            @RequestParam(required = false) ProductCategory category,

            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (max 100)")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        int effectiveSize = Math.min(size, MAX_PAGE_SIZE);
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, effectiveSize, sort);

        PageResponse<ProductResponse> response = productService.listProducts(name, category, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single product by its identifier.
     *
     * @param id the product's primary key
     * @return the product DTO with HTTP 200, or HTTP 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "Product primary key") @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    /**
     * Creates a new product from the request body.
     *
     * @param request validated product creation payload
     * @return the created product DTO with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing product's mutable fields.
     *
     * @param id      the product's primary key
     * @param request validated update payload
     * @return the updated product DTO with HTTP 200, or HTTP 404 if not found
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product primary key") @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * Permanently deletes a product.
     *
     * @param id the product's primary key
     * @return HTTP 204 No Content on success, or HTTP 404 if not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product primary key") @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
