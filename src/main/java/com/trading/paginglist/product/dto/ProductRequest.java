package com.trading.paginglist.product.dto;

import com.trading.paginglist.product.domain.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Inbound DTO for create and update product requests.
 *
 * <p>All fields are validated with Bean Validation annotations.
 * Controllers must annotate the parameter with {@code @Valid} to trigger
 * constraint checking before the request reaches the service layer.</p>
 */
@Data
public class ProductRequest {

    /**
     * Display name of the product.
     * Required, non-blank, maximum 200 characters.
     */
    @NotBlank(message = "Product name must not be blank")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    /**
     * Category that classifies the product.
     * Must be one of the {@link ProductCategory} enum values.
     */
    @NotNull(message = "Category must not be null")
    private ProductCategory category;

    /**
     * Unit price of the product.
     * Must be a positive value greater than zero.
     */
    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    /**
     * Number of units in stock.
     * Must be zero or a positive integer.
     */
    @NotNull(message = "Stock must not be null")
    @Min(value = 0, message = "Stock must be 0 or greater")
    private Integer stock;
}
