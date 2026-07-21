package com.trading.paginglist.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ConsoleCharset}.
 *
 * <p>Banner 已改 ASCII+UTF-8；此類別仍保留給需要對齊 Console 的呼叫端。</p>
 */
@DisplayName("ConsoleCharset Unit Tests")
class ConsoleCharsetTest {

    @Test
    @DisplayName("resolve() never returns null")
    void resolve_neverNull() {
        assertThat(ConsoleCharset.resolve()).isNotNull();
    }

    @Test
    @DisplayName("banner path prefers UTF-8 when stdout is UTF-8 (no MS950 fallback for box chars)")
    void resolve_keepsUtf8WhenStdoutIsUtf8() {
        String prevStdout = System.getProperty("stdout.encoding");
        String prevNative = System.getProperty("native.encoding");
        String prevJnu = System.getProperty("sun.jnu.encoding");
        try {
            System.setProperty("stdout.encoding", "UTF-8");
            System.setProperty("native.encoding", "MS950");
            System.setProperty("sun.jnu.encoding", "MS950");

            // Prefer UTF-8 so Unicode/Chinese are not replaced with '?' under MS950
            Charset cs = ConsoleCharset.resolve();
            assertThat(cs).isEqualTo(StandardCharsets.UTF_8);
        } finally {
            restore("stdout.encoding", prevStdout);
            restore("native.encoding", prevNative);
            restore("sun.jnu.encoding", prevJnu);
        }
    }

    private static void restore(String key, String previous) {
        if (previous == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previous);
        }
    }
}
