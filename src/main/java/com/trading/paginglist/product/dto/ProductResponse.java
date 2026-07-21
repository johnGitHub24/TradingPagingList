package com.trading.paginglist.product.dto;

import com.trading.paginglist.product.domain.Product;
import com.trading.paginglist.product.domain.ProductCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outbound DTO for a single product resource.
 *
 * <p>Exposes all fields of {@link Product} in a serialization-friendly
 * form, decoupling the API contract from the JPA entity structure.</p>
 */
@Data
@Builder
public class ProductResponse {

    /** Unique identifier of the product. */
    private Long id;

    /** Human-readable product name. */
    private String name;

    /** Category classification of the product. */
    private ProductCategory category;

    /** Unit price of the product. */
    private BigDecimal price;

    /** Number of units currently in stock. */
    private Integer stock;

    /** UTC timestamp when the product record was first created. */
    private LocalDateTime createdAt;

    /**
     * Factory method that maps a {@link Product} entity to a {@link ProductResponse}.
     *
     * @param product the JPA entity to convert; must not be {@code null}
     * @return a fully populated {@code ProductResponse} DTO
     */
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
