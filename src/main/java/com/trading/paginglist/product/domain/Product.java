package com.trading.paginglist.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a tradeable product.
 *
 * <p>Persisted to the {@code products} table. Timestamps are managed
 * <br>automatically via {@link CreationTimestamp}.</p>
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable product name.
     * <br>Must not be blank and is limited to 200 characters.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Product category used for grouping and filtering.
     * <br>Stored as a VARCHAR enum string (e.g. "ELECTRONICS").
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductCategory category;

    /**
     * Unit price of the product. Must be positive and supports up to
     * <br>two decimal places (precision = 15, scale = 2).
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /**
     * Number of units currently in stock.
     * <br>Defaults to 0; must be non-negative.
     */
    @Column(nullable = false)
    private Integer stock;

    /**
     * UTC timestamp set once when the record is first persisted.
     * <br>Managed by Hibernate; never updated afterward.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
