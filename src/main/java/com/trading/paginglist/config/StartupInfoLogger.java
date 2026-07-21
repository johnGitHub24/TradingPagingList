package com.trading.paginglist.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 【職責】應用就緒後於 Console 印出常用 URL（health／Swagger／H2／前台），方便本機啟動。
 * 【技巧】聽 {@link ApplicationReadyEvent}；開關來自 {@code startup.info.*}；
 *         banner 僅用 ASCII（無 Unicode 框線／避免 MS950 把字元換成 {@code ?}），
 *         並以 UTF-8 bytes 寫出；前端需另開 Vite（見 {@code scripts/start-frontend.ps1}）。
 * 【概念】開發便利輸出，不是業務邏輯；本專案前端為 Vite（:5174）。
 * 【邊界】不負責啟動前端、不驗證 URL 是否可連。
 */
@Component
public class StartupInfoLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupInfoLogger.class);

    private static final String RULE =
            "+----------------------------------------------------------------------+";

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        if (!env.getProperty("startup.info.enabled", Boolean.class, true)) {
            return;
        }

        String project = env.getProperty("startup.info.project-name", "TradingPagingList");
        String port = env.getProperty("server.port", "8091");
        String base = "http://localhost:" + port;
        String frontend = env.getProperty("startup.info.frontend", "none");
        boolean auth = env.getProperty("startup.info.auth", Boolean.class, false);
        boolean h2 = env.getProperty("startup.info.h2", Boolean.class, true);
        boolean apiDocs = env.getProperty("startup.info.api-docs", Boolean.class, true);

        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add(RULE);
        lines.add(pad("|  " + project + " backend ready -- links"));
        lines.add(RULE);
        lines.add(pad("|  [Backend API / Tools]"));
        lines.add(pad("|    Health        " + base + "/actuator/health"));
        lines.add(pad("|    Info          " + base + "/actuator/info"));
        if (apiDocs) {
            lines.add(pad("|    Swagger UI    " + base + "/swagger-ui.html"));
            lines.add(pad("|    OpenAPI JSON  " + base + "/v3/api-docs"));
        }
        if (h2) {
            lines.add(pad("|    H2 Console    " + base + "/h2-console"));
            String jdbc = env.getProperty("spring.datasource.url", "jdbc:h2:mem:paginglist");
            lines.add(pad("|    H2 JDBC       " + jdbc));
            lines.add(pad("|    H2 user/pass  sa / (empty)"));
        }

        if (!"none".equalsIgnoreCase(frontend)) {
            lines.add(RULE);
            if ("static".equalsIgnoreCase(frontend)) {
                lines.add(pad("|  [Frontend] same-port static"));
                lines.add(pad("|    Home          " + base + env.getProperty("startup.info.home-path", "/")));
                for (String path : extraPaths(env)) {
                    lines.add(pad("|    Extra         " + base + path));
                }
            } else if ("vite".equalsIgnoreCase(frontend)) {
                String feBase = "http://localhost:" + env.getProperty("startup.info.frontend-port", "5174");
                lines.add(pad("|  [Frontend Vue] start Vite separately (required)"));
                lines.add(pad("|    Home          " + feBase + env.getProperty("startup.info.home-path", "/")));
                lines.add(pad("|    How to start  .\\scripts\\start-frontend.ps1"));
                lines.add(pad("|    Or full stack .\\scripts\\start-all.ps1"));
                if (auth) {
                    lines.add(pad("|    Login         " + feBase + env.getProperty("startup.info.login-path", "/login")));
                }
            }
            if (auth) {
                lines.add(pad("|    Default user  "
                        + env.getProperty("startup.info.default-user", "admin")
                        + " / "
                        + env.getProperty("startup.info.default-pass", "admin123")));
            }
        }

        lines.add(RULE);
        lines.add("");
        printLines(lines);
        log.info("{} ready — frontend={} | {}", project, frontend, base + "/actuator/health");
    }

    /** Pads a line to match RULE width (72 chars inside the outer +). */
    private static String pad(String content) {
        final int inner = RULE.length() - 1; // up to last '+'
        if (content.length() >= inner) {
            return content.substring(0, inner) + "|";
        }
        return content + " ".repeat(inner - content.length()) + "|";
    }

    /**
     * 【職責】以 UTF-8 寫出 banner（僅 ASCII 內容，任何 Console 皆可讀）。
     * 【技巧】{@code write(byte[])} 不做二次轉碼；測試替換 {@code System.out} 仍可捕捉。
     */
    static void printLines(List<String> lines) {
        String nl = System.lineSeparator();
        try {
            for (String line : lines) {
                System.out.write((line + nl).getBytes(StandardCharsets.UTF_8));
            }
            System.out.flush();
        } catch (Exception e) {
            for (String line : lines) {
                System.out.println(line);
            }
        }
    }

    private static List<String> extraPaths(Environment env) {
        String first = env.getProperty("startup.info.extra-paths[0]");
        if (first != null && !first.isBlank()) {
            List<String> paths = new ArrayList<>();
            for (int i = 0; ; i++) {
                String p = env.getProperty("startup.info.extra-paths[" + i + "]");
                if (p == null || p.isBlank()) {
                    break;
                }
                paths.add(p.startsWith("/") ? p : "/" + p);
            }
            return paths;
        }
        String raw = env.getProperty("startup.info.extra-paths");
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith("/") ? s : "/" + s)
                .toList();
    }
}
