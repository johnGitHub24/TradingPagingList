package com.trading.paginglist.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】保護 {@link GlobalExceptionHandler}：靜態資源缺失必須回 404，不可被兜底成 500；
 *         領域 404／驗證 422 也需穩定對應。
 */
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("NoResourceFoundException（如 favicon.ico）→ HTTP 404，非 500")
    void noResourceFound_returnsNotFound() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/favicon.ico");

        ResponseEntity<Map<String, Object>> response = handler.handleNoResourceFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
    }

    @Test
    @DisplayName("ResourceNotFoundException → HTTP 404 with message")
    void resourceNotFound_returnsNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Product not found with id: 9");

        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Product not found with id: 9");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException → HTTP 422 with fieldErrors")
    void validationFailed_returns422() throws Exception {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class);
        MethodParameter param = new MethodParameter(method, 0);
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "productRequest");
        binding.addError(new FieldError("productRequest", "name", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, binding);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(422);
        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) response.getBody().get("fieldErrors");
        assertThat(fieldErrors).containsEntry("name", "must not be blank");
    }

    @Test
    @DisplayName("Unexpected Exception → HTTP 500 generic message")
    void genericException_returns500() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGenericException(new RuntimeException("secret internals"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().get("message").toString()).doesNotContain("secret");
    }

    @SuppressWarnings("unused")
    private void dummy(String ignored) {
        // reflection target for MethodParameter
    }
}
