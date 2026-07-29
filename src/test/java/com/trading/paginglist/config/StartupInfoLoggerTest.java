package com.trading.paginglist.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StartupInfoLogger}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StartupInfoLogger Unit Tests")
class StartupInfoLoggerTest {

    @Mock
    private ApplicationReadyEvent event;

    @Mock
    private ConfigurableApplicationContext applicationContext;

    @Mock
    private ConfigurableEnvironment env;

    private final StartupInfoLogger logger = new StartupInfoLogger();

    @Test
    @DisplayName("enabled=false → prints nothing")
    void disabled_printsNothing() {
        when(event.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getEnvironment()).thenReturn(env);
        when(env.getProperty("startup.info.enabled", Boolean.class, true)).thenReturn(false);

        String out = captureStdout(() -> logger.onApplicationEvent(event));

        assertThat(out).doesNotContain("後端已啟動");
        assertThat(out).doesNotContain("localhost");
    }

    @Test
    @DisplayName("vite frontend → prints backend + Vue links on :5174 (UTF-8)")
    void vite_printsFrontendLinks() {
        when(event.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getEnvironment()).thenReturn(env);
        when(env.getProperty("startup.info.enabled", Boolean.class, true)).thenReturn(true);
        when(env.getProperty("startup.info.project-name", "TradingPagingList")).thenReturn("TradingPagingList");
        when(env.getProperty("server.port", "8091")).thenReturn("8091");
        when(env.getProperty("startup.info.frontend", "none")).thenReturn("vite");
        when(env.getProperty("startup.info.auth", Boolean.class, false)).thenReturn(false);
        when(env.getProperty("startup.info.h2", Boolean.class, true)).thenReturn(true);
        when(env.getProperty("startup.info.api-docs", Boolean.class, true)).thenReturn(true);
        when(env.getProperty("spring.datasource.url", "jdbc:h2:mem:paginglist"))
                .thenReturn("jdbc:h2:mem:paginglist");
        when(env.getProperty("startup.info.frontend-port", "5174")).thenReturn("5174");
        when(env.getProperty("startup.info.home-path", "/")).thenReturn("/");

        String out = captureStdout(() -> logger.onApplicationEvent(event));

        assertThat(out).contains("TradingPagingList 後端已啟動");
        assertThat(out).contains("http://localhost:8091/actuator/health");
        assertThat(out).contains("http://localhost:5174/");
        assertThat(out).contains("【前台 Vue】");
        assertThat(out).contains("start-frontend.ps1");
        assertThat(out).contains("H2 Console");
        assertThat(out).contains("╔");
    }

    private static String captureStdout(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(ps);
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
