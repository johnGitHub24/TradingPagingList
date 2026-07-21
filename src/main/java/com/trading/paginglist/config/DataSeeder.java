package com.trading.paginglist.config;

import com.trading.paginglist.product.ProductRepository;
import com.trading.paginglist.product.domain.Product;
import com.trading.paginglist.product.domain.ProductCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with 50 sample products on application startup.
 *
 * <p>Only runs when the products table is empty, so restarting the application
 * does not duplicate rows when using a persistent database. With the default
 * in-memory H2 setup the table is always empty at startup.</p>
 *
 * <p>Products span all five {@link ProductCategory} values with varied
 * prices and stock levels to facilitate meaningful pagination testing.</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    /**
     * Inserts 50 seed products if the products table is empty.
     *
     * @param repository the JPA repository used to check count and save entities
     * @return a {@link CommandLineRunner} executed after the application context starts
     */
    @Bean
    public CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                log.info("DataSeeder: products table already populated, skipping seed.");
                return;
            }

            List<Product> products = List.of(
                    // ── ELECTRONICS (10 items) ──────────────────────────────────────
                    product("MacBook Pro 16\"",          ProductCategory.ELECTRONICS, "89900.00", 15),
                    product("iPhone 15 Pro",             ProductCategory.ELECTRONICS, "35900.00", 50),
                    product("Samsung Galaxy S24",        ProductCategory.ELECTRONICS, "28900.00", 40),
                    product("Sony WH-1000XM5 Headphones", ProductCategory.ELECTRONICS, "9900.00", 80),
                    product("iPad Air 5th Gen",          ProductCategory.ELECTRONICS, "19900.00", 30),
                    product("Dell XPS 15 Laptop",        ProductCategory.ELECTRONICS, "62000.00", 10),
                    product("LG 27\" 4K Monitor",        ProductCategory.ELECTRONICS, "14500.00", 25),
                    product("Logitech MX Master 3 Mouse", ProductCategory.ELECTRONICS, "3200.00", 100),
                    product("Mechanical Keyboard TKL",   ProductCategory.ELECTRONICS, "4500.00",  60),
                    product("USB-C Hub 7-in-1",          ProductCategory.ELECTRONICS, "1200.00", 200),

                    // ── CLOTHING (10 items) ─────────────────────────────────────────
                    product("Nike Air Max 270",          ProductCategory.CLOTHING, "3800.00", 120),
                    product("Levi's 511 Slim Jeans",     ProductCategory.CLOTHING,  "2500.00",  90),
                    product("Adidas Ultraboost 22",      ProductCategory.CLOTHING,  "4200.00",  75),
                    product("Uniqlo Ultra Light Down Jacket", ProductCategory.CLOTHING, "1990.00", 60),
                    product("Champion Hoodie",           ProductCategory.CLOTHING,  "1500.00", 150),
                    product("Columbia Hiking Pants",     ProductCategory.CLOTHING,  "2200.00",  45),
                    product("Polo Ralph Lauren T-Shirt", ProductCategory.CLOTHING,  "1200.00", 200),
                    product("New Balance 990v5",         ProductCategory.CLOTHING,  "5500.00",  35),
                    product("Arc'teryx Atom LT Jacket",  ProductCategory.CLOTHING,  "12000.00", 20),
                    product("Merino Wool Socks (3-Pack)", ProductCategory.CLOTHING, "650.00",  300),

                    // ── FOOD (10 items) ─────────────────────────────────────────────
                    product("Organic Green Tea (50 bags)",   ProductCategory.FOOD, "320.00", 500),
                    product("Premium Dark Chocolate 70%",    ProductCategory.FOOD, "180.00", 400),
                    product("Manuka Honey UMF 15+",          ProductCategory.FOOD, "1200.00",  80),
                    product("Cold-Brew Coffee (12 pack)",    ProductCategory.FOOD, "420.00", 200),
                    product("Almond Butter Natural (500g)",  ProductCategory.FOOD, "380.00", 300),
                    product("Himalayan Pink Salt (1kg)",     ProductCategory.FOOD,  "150.00", 600),
                    product("Protein Bar Variety Pack x24",  ProductCategory.FOOD, "960.00", 150),
                    product("Extra Virgin Olive Oil (750ml)", ProductCategory.FOOD, "480.00", 250),
                    product("Japanese Matcha Powder (100g)", ProductCategory.FOOD, "880.00", 120),
                    product("Kombucha Ginger Lemon (6-pack)", ProductCategory.FOOD, "540.00", 180),

                    // ── SPORTS (10 items) ───────────────────────────────────────────
                    product("Yoga Mat Anti-Slip 6mm",        ProductCategory.SPORTS,  "890.00", 200),
                    product("Adjustable Dumbbell Set 5-52lb", ProductCategory.SPORTS, "8500.00",  30),
                    product("Resistance Bands Set",           ProductCategory.SPORTS,  "450.00", 350),
                    product("Pull-Up Bar Doorway",            ProductCategory.SPORTS, "1200.00", 100),
                    product("Jump Rope Speed Cable",          ProductCategory.SPORTS,  "380.00", 250),
                    product("Foam Roller Deep Tissue",        ProductCategory.SPORTS,  "620.00", 180),
                    product("Kettlebell 16kg Cast Iron",      ProductCategory.SPORTS, "1800.00",  70),
                    product("Running Belt Waist Pack",        ProductCategory.SPORTS,  "480.00", 150),
                    product("Swim Goggles Anti-Fog",          ProductCategory.SPORTS,  "350.00", 220),
                    product("Badminton Racket Carbon",        ProductCategory.SPORTS, "2200.00",  90),

                    // ── BOOKS (10 items) ────────────────────────────────────────────
                    product("Clean Code by Robert Martin",     ProductCategory.BOOKS, "950.00",  80),
                    product("Designing Data-Intensive Apps",   ProductCategory.BOOKS, "1200.00", 60),
                    product("The Pragmatic Programmer",        ProductCategory.BOOKS, "880.00",  90),
                    product("Spring Boot in Action",           ProductCategory.BOOKS, "750.00",  70),
                    product("Vue.js 3 Up & Running",           ProductCategory.BOOKS, "720.00",  55),
                    product("Effective Java 3rd Edition",      ProductCategory.BOOKS, "980.00",  85),
                    product("Domain-Driven Design",            ProductCategory.BOOKS, "1100.00", 40),
                    product("Refactoring 2nd Edition",         ProductCategory.BOOKS, "890.00",  65),
                    product("System Design Interview Vol. 2",  ProductCategory.BOOKS, "820.00", 100),
                    product("The Staff Engineer's Path",       ProductCategory.BOOKS, "760.00",  50)
            );

            repository.saveAll(products);
            log.info("DataSeeder: inserted {} seed products.", products.size());
        };
    }

    /**
     * Convenience factory to construct an unsaved {@link Product} entity.
     *
     * @param name     product display name
     * @param category product category enum value
     * @param price    unit price as a decimal string (avoids floating-point imprecision)
     * @param stock    initial stock count
     * @return a transient {@link Product} ready to be saved
     */
    private Product product(String name, ProductCategory category, String price, int stock) {
        return Product.builder()
                .name(name)
                .category(category)
                .price(new BigDecimal(price))
                .stock(stock)
                .build();
    }
}
