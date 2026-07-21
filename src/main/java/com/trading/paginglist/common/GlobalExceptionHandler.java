package com.trading.paginglist.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 【職責】全域 REST 例外處理：將領域／框架例外轉成一致的 JSON 錯誤回應。
 * 【技巧】{@code @RestControllerAdvice} + 具體 {@code @ExceptionHandler}；
 *         必須先攔截 {@link NoResourceFoundException}，否則會落入 catch-all 變成 500。
 * 【概念】瀏覽器常自動請求 {@code /favicon.ico}；缺檔時 Spring 6 丟的是「靜態資源找不到」，
 *         屬預期 404，不是伺服器內部錯誤。
 * 【邊界】不決定業務何時拋例外；只負責 HTTP 狀態碼與對外訊息形狀。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 【職責】處理領域資源不存在（例如 Product id 找不到）。
     * 【技巧】對應自訂 {@link ResourceNotFoundException}，回 404。
     * 【概念】與靜態資源 404（{@link NoResourceFoundException}）分開，方便日誌區分業務 vs 資產。
     *
     * @param ex 帶有缺失資源說明的例外
     * @return HTTP 404 與結構化 JSON
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    /**
     * 【職責】處理 Spring MVC 找不到靜態／映射資源（含瀏覽器自動要的 favicon）。
     * 【技巧】攔截 {@link NoResourceFoundException}，回 404 且只打 debug，避免 ERROR 堆疊洗版。
     * 【概念】Spring Boot 3／Spring 6 對缺靜態檔改丟此例外；若被 {@code Exception} 兜底會誤回 500。
     *
     * @param ex 缺少的資源路徑資訊
     * @return HTTP 404 與結構化 JSON
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {
        log.debug("Static resource not found: {}", ex.getResourcePath());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    /**
     * 【職責】處理 {@code @Valid} 請求體驗證失敗。
     * 【技巧】彙整所有 {@link FieldError} 到 {@code fieldErrors} map。
     * 【概念】422 表示語意／約束不通過，與 400 語法錯誤、404 找不到不同。
     *
     * @param ex 驗證失敗細節
     * @return HTTP 422 與欄位錯誤清單
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = buildErrorBody(HttpStatus.UNPROCESSABLE_ENTITY,
                "Validation Failed", "Request body contains invalid fields");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    /**
     * 【職責】兜底未預期例外，回 500。
     * 【技巧】最寬鬆的 {@code Exception.class} 必須最後匹配，不可搶走更具體的 handler。
     * 【概念】對外只回泛用訊息，避免堆疊細節洩漏給客戶端。
     *
     * @param ex 未處理例外
     * @return HTTP 500 泛用錯誤內容
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error", "An unexpected error occurred");
    }

    // ── Private Helpers ────────────────────────────────────────────────────

    /**
     * Builds a simple error response with standard fields.
     *
     * @param status  HTTP status to return
     * @param error   short error label
     * @param message detailed human-readable message
     * @return the wrapped {@link ResponseEntity}
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(buildErrorBody(status, error, message));
    }

    /**
     * Constructs the shared error body map.
     *
     * @param status  HTTP status
     * @param error   short error label
     * @param message detailed human-readable message
     * @return mutable map with standard error fields
     */
    private Map<String, Object> buildErrorBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
