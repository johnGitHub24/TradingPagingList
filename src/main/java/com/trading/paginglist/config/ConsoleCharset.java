package com.trading.paginglist.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 【職責】解析 Console 輸出字元集。
 * 【技巧】優先 {@code stdout.encoding}；否則 {@code native.encoding}／預設。
 * 【概念】勿在 Windows 把 UTF-8 stdout「降級」成 MS950——Unicode 框線無法編碼會變 {@code ?}。
 * 【邊界】StartupInfoLogger banner 已改純 ASCII，通常直接用 UTF-8 即可。
 */
public final class ConsoleCharset {

    private ConsoleCharset() {
    }

    /**
     * @return non-null charset；有 stdout.encoding 時尊重它（含 UTF-8）
     */
    public static Charset resolve() {
        Charset stdoutCs = charsetOf(
                System.getProperty("stdout.encoding"),
                System.getProperty("sun.stdout.encoding"));
        if (stdoutCs != null) {
            return stdoutCs;
        }
        Charset nativeCs = charsetOf(
                System.getProperty("native.encoding"),
                System.getProperty("sun.jnu.encoding"));
        if (nativeCs != null) {
            return nativeCs;
        }
        if (isWindows()) {
            return StandardCharsets.UTF_8;
        }
        return Charset.defaultCharset();
    }

    private static Charset charsetOf(String... names) {
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            try {
                return Charset.forName(name.trim());
            } catch (Exception ignored) {
                // try next
            }
        }
        return null;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
}
