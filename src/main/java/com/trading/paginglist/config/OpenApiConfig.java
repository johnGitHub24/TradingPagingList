package com.trading.paginglist.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger UI configuration.
 *
 * <p>Provides application-level metadata visible in the Swagger UI
 * at {@code /swagger-ui.html} when the application runs.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Registers a customized {@link OpenAPI} bean with project metadata.
     *
     * @return the configured OpenAPI descriptor
     */
    @Bean
    public OpenAPI tradingPagingListOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TradingPagingList API")
                        .description("RESTful API for product CRUD with server-side pagination. "
                                + "Default page size is 10 items. "
                                + "H2 console available at /h2-console (dev only).")
                        .version("0.1.0-SNAPSHOT")
                        .contact(new Contact()
                                .name("Trading Team")
                                .email("trading@example.com")));
    }
}
