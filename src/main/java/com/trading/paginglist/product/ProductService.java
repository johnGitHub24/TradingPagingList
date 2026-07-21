package com.trading.paginglist.product;

import com.trading.paginglist.common.ResourceNotFoundException;
import com.trading.paginglist.product.domain.Product;
import com.trading.paginglist.product.domain.ProductCategory;
import com.trading.paginglist.product.dto.PageResponse;
import com.trading.paginglist.product.dto.ProductRequest;
import com.trading.paginglist.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic layer for product management.
 *
 * <p>Orchestrates all CRUD operations and pagination queries, keeping
 * the {@link ProductController} free of business rules. All write
 * operations are transactional; read-only operations are annotated with
 * {@code readOnly = true} to avoid acquiring unnecessary write locks.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Returns a paginated list of products, optionally filtered by name and/or category.
     *
     * <p>Delegates filtering logic to dedicated repository derived queries so no
     * JPQL string concatenation occurs in this layer.</p>
     *
     * @param name     optional name substring filter (case-insensitive); may be {@code null} or blank
     * @param category optional category filter; may be {@code null}
     * @param pageable Spring Data pageable describing page index, size, and sort order
     * @return a {@link PageResponse} wrapping the current page of {@link ProductResponse} DTOs
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listProducts(String name, ProductCategory category, Pageable pageable) {
        log.debug("Listing products: name={}, category={}, pageable={}", name, category, pageable);

        boolean hasName = name != null && !name.isBlank();
        boolean hasCategory = category != null;

        Page<Product> page;

        if (hasName && hasCategory) {
            page = productRepository.findByNameContainingIgnoreCaseAndCategory(name, category, pageable);
        } else if (hasName) {
            page = productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (hasCategory) {
            page = productRepository.findByCategory(category, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return PageResponse.from(page.map(ProductResponse::from));
    }

    /**
     * Retrieves a single product by its identifier.
     *
     * @param id the product's surrogate primary key
     * @return the matching {@link ProductResponse}
     * @throws ResourceNotFoundException if no product with the given {@code id} exists
     */
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        log.debug("Fetching product id={}", id);
        Product product = findProductOrThrow(id);
        return ProductResponse.from(product);
    }

    /**
     * Persists a new product from the supplied request data.
     *
     * @param request validated inbound DTO; must not be {@code null}
     * @return the {@link ProductResponse} of the newly created product (including generated id)
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product: name={}", request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product id={}", saved.getId());
        return ProductResponse.from(saved);
    }

    /**
     * Updates an existing product with the supplied request data.
     *
     * <p>All mutable fields are replaced; the {@code createdAt} timestamp is
     * never modified.</p>
     *
     * @param id      the surrogate key of the product to update
     * @param request validated inbound DTO with updated values
     * @return the {@link ProductResponse} reflecting the updated state
     * @throws ResourceNotFoundException if no product with the given {@code id} exists
     */
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product id={}", id);
        Product product = findProductOrThrow(id);

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product saved = productRepository.save(product);
        log.info("Updated product id={}", saved.getId());
        return ProductResponse.from(saved);
    }

    /**
     * Permanently removes the product with the given identifier.
     *
     * @param id the surrogate key of the product to delete
     * @throws ResourceNotFoundException if no product with the given {@code id} exists
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product id={}", id);
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
        log.info("Deleted product id={}", id);
    }

    // ── Private Helpers ────────────────────────────────────────────────────

    /**
     * Looks up a {@link Product} by id and throws {@link ResourceNotFoundException}
     * with a descriptive message if the record does not exist.
     *
     * @param id the surrogate key to look up
     * @return the found {@link Product} entity
     */
    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }
}
