package com.trading.paginglist.product;

import com.trading.paginglist.product.domain.Product;
import com.trading.paginglist.product.domain.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Product} entities.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD and pagination
 * operations. Custom derived query methods provide name-based search and
 * category filtering, both with pagination support.</p>
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Returns a page of products whose names contain the given string,
     * performing a case-insensitive match.
     *
     * @param name     the substring to search for in product names
     * @param pageable pagination and sorting instructions
     * @return matching products as a {@link Page}
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Returns a page of products that belong to the specified category.
     *
     * @param category the category to filter by
     * @param pageable pagination and sorting instructions
     * @return matching products as a {@link Page}
     */
    Page<Product> findByCategory(ProductCategory category, Pageable pageable);

    /**
     * Returns a page of products filtered by both name substring and category.
     *
     * @param name     the substring to search for (case-insensitive)
     * @param category the category to restrict results to
     * @param pageable pagination and sorting instructions
     * @return matching products as a {@link Page}
     */
    Page<Product> findByNameContainingIgnoreCaseAndCategory(
            String name, ProductCategory category, Pageable pageable);
}
