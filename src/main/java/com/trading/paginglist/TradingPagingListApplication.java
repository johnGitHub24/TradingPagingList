package com.trading.paginglist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the TradingPagingList Spring Boot application.
 *
 * <p>Demonstrates server-side pagination with Spring Data JPA and a Vue 3 frontend.
 * Starts on port 8091 (dev) with an in-memory H2 database.</p>
 */
@SpringBootApplication
public class TradingPagingListApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingPagingListApplication.class, args);
    }
}
