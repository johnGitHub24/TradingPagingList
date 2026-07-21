package com.trading.paginglist.product;

import com.trading.paginglist.common.ResourceNotFoundException;
import com.trading.paginglist.product.domain.Product;
import com.trading.paginglist.product.domain.ProductCategory;
import com.trading.paginglist.product.dto.PageResponse;
import com.trading.paginglist.product.dto.ProductRequest;
import com.trading.paginglist.product.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for {@link ProductService}.
 *
 * <p>Uses Mockito to stub the {@link ProductRepository}, keeping tests
 * fast and free of database dependencies. Each public method is covered
 * by at least one happy-path test and one error-path test.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    /** Reusable sample product used across test cases. */
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .category(ProductCategory.ELECTRONICS)
                .price(new BigDecimal("999.99"))
                .stock(50)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── listProducts ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("listProducts()")
    class ListProducts {

        @Test
        @DisplayName("returns paginated results when no filters are supplied")
        void listProducts_noFilter_returnsPageResponse() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
            given(productRepository.findAll(pageable)).willReturn(productPage);

            // when
            PageResponse<ProductResponse> result = productService.listProducts(null, null, pageable);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
            verify(productRepository).findAll(pageable);
        }

        @Test
        @DisplayName("delegates to name-search repository method when name filter is provided")
        void listProducts_withNameFilter_callsNameSearchRepo() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
            given(productRepository.findByNameContainingIgnoreCase("Test", pageable))
                    .willReturn(productPage);

            // when
            PageResponse<ProductResponse> result = productService.listProducts("Test", null, pageable);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).getName()).isEqualTo("Test Product");
            verify(productRepository).findByNameContainingIgnoreCase("Test", pageable);
            verify(productRepository, never()).findAll(pageable);
        }

        @Test
        @DisplayName("delegates to category-filter repository method when category filter is provided")
        void listProducts_withCategoryFilter_callsCategoryRepo() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
            given(productRepository.findByCategory(ProductCategory.ELECTRONICS, pageable))
                    .willReturn(productPage);

            // when
            PageResponse<ProductResponse> result = productService.listProducts(
                    null, ProductCategory.ELECTRONICS, pageable);

            // then
            assertThat(result.content()).hasSize(1);
            verify(productRepository).findByCategory(ProductCategory.ELECTRONICS, pageable);
        }

        @Test
        @DisplayName("delegates to combined filter repository method when both name and category are provided")
        void listProducts_withNameAndCategoryFilter_callsCombinedRepo() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
            given(productRepository.findByNameContainingIgnoreCaseAndCategory(
                    "Test", ProductCategory.ELECTRONICS, pageable)).willReturn(productPage);

            // when
            PageResponse<ProductResponse> result = productService.listProducts(
                    "Test", ProductCategory.ELECTRONICS, pageable);

            // then
            assertThat(result.content()).hasSize(1);
            verify(productRepository).findByNameContainingIgnoreCaseAndCategory(
                    "Test", ProductCategory.ELECTRONICS, pageable);
        }

        @Test
        @DisplayName("returns empty page when no products match filters")
        void listProducts_noMatchingProducts_returnsEmptyPage() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = Page.empty(pageable);
            given(productRepository.findAll(pageable)).willReturn(emptyPage);

            // when
            PageResponse<ProductResponse> result = productService.listProducts(null, null, pageable);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
        }

        @Test
        @DisplayName("treats blank name string as absent filter")
        void listProducts_blankName_treatedAsNoFilter() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
            given(productRepository.findAll(pageable)).willReturn(productPage);

            // when — pass a blank string, not null
            PageResponse<ProductResponse> result = productService.listProducts("   ", null, pageable);

            // then — falls through to unfiltered findAll
            verify(productRepository).findAll(pageable);
            assertThat(result.content()).hasSize(1);
        }
    }

    // ── getProduct ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getProduct()")
    class GetProduct {

        @Test
        @DisplayName("returns ProductResponse when product exists")
        void getProduct_existingId_returnsResponse() {
            // given
            given(productRepository.findById(1L)).willReturn(Optional.of(sampleProduct));

            // when
            ProductResponse response = productService.getProduct(1L);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Test Product");
            assertThat(response.getCategory()).isEqualTo(ProductCategory.ELECTRONICS);
            assertThat(response.getPrice()).isEqualByComparingTo("999.99");
            assertThat(response.getStock()).isEqualTo(50);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product does not exist")
        void getProduct_nonExistentId_throwsResourceNotFoundException() {
            // given
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> productService.getProduct(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // ── createProduct ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("createProduct()")
    class CreateProduct {

        @Test
        @DisplayName("saves and returns the new product with generated id")
        void createProduct_validRequest_returnsCreatedProduct() {
            // given
            ProductRequest request = new ProductRequest();
            request.setName("New Gadget");
            request.setCategory(ProductCategory.ELECTRONICS);
            request.setPrice(new BigDecimal("4999.00"));
            request.setStock(25);

            Product savedProduct = Product.builder()
                    .id(10L)
                    .name("New Gadget")
                    .category(ProductCategory.ELECTRONICS)
                    .price(new BigDecimal("4999.00"))
                    .stock(25)
                    .createdAt(LocalDateTime.now())
                    .build();

            given(productRepository.save(any(Product.class))).willReturn(savedProduct);

            // when
            ProductResponse response = productService.createProduct(request);

            // then
            assertThat(response.getId()).isEqualTo(10L);
            assertThat(response.getName()).isEqualTo("New Gadget");
            assertThat(response.getCategory()).isEqualTo(ProductCategory.ELECTRONICS);
            assertThat(response.getPrice()).isEqualByComparingTo("4999.00");
            verify(productRepository).save(any(Product.class));
        }
    }

    // ── updateProduct ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProduct()")
    class UpdateProduct {

        @Test
        @DisplayName("updates all mutable fields and returns the updated product")
        void updateProduct_existingId_returnsUpdatedProduct() {
            // given
            given(productRepository.findById(1L)).willReturn(Optional.of(sampleProduct));

            ProductRequest request = new ProductRequest();
            request.setName("Updated Name");
            request.setCategory(ProductCategory.BOOKS);
            request.setPrice(new BigDecimal("1299.00"));
            request.setStock(10);

            Product updatedProduct = Product.builder()
                    .id(1L)
                    .name("Updated Name")
                    .category(ProductCategory.BOOKS)
                    .price(new BigDecimal("1299.00"))
                    .stock(10)
                    .createdAt(sampleProduct.getCreatedAt())
                    .build();
            given(productRepository.save(any(Product.class))).willReturn(updatedProduct);

            // when
            ProductResponse response = productService.updateProduct(1L, request);

            // then
            assertThat(response.getName()).isEqualTo("Updated Name");
            assertThat(response.getCategory()).isEqualTo(ProductCategory.BOOKS);
            assertThat(response.getPrice()).isEqualByComparingTo("1299.00");
            assertThat(response.getStock()).isEqualTo(10);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product does not exist")
        void updateProduct_nonExistentId_throwsResourceNotFoundException() {
            // given
            given(productRepository.findById(999L)).willReturn(Optional.empty());
            ProductRequest request = new ProductRequest();
            request.setName("X");
            request.setCategory(ProductCategory.FOOD);
            request.setPrice(BigDecimal.ONE);
            request.setStock(1);

            // when / then
            assertThatThrownBy(() -> productService.updateProduct(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // ── deleteProduct ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProduct {

        @Test
        @DisplayName("deletes the product when it exists")
        void deleteProduct_existingId_deletesSuccessfully() {
            // given
            given(productRepository.findById(1L)).willReturn(Optional.of(sampleProduct));

            // when
            productService.deleteProduct(1L);

            // then
            verify(productRepository).delete(sampleProduct);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product does not exist")
        void deleteProduct_nonExistentId_throwsResourceNotFoundException() {
            // given
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
            verify(productRepository, never()).delete(any());
        }
    }
}
