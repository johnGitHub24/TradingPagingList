package com.trading.paginglist.product.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic pagination wrapper returned by all list endpoints.
 *
 * <p>Wraps a Spring Data {@link Page} into a plain serializable record so
 * the API response shape is stable and independent of the Spring Data
 * internal structure.</p>
 *
 * <p>Example JSON response:</p>
 * <pre>{@code
 * {
 *   "content": [...],
 *   "page": 0,
 *   "size": 10,
 *   "totalElements": 100,
 *   "totalPages": 10,
 *   "first": true,
 *   "last": false
 * }
 * }</pre>
 *
 * @param <T> the type of element in the page content list
 */
public record PageResponse<T>(
        /** Slice of domain objects for the current page. */
        List<T> content,

        /** Zero-based current page index. */
        int page,

        /** Maximum number of elements per page. */
        int size,

        /** Total number of elements across all pages. */
        long totalElements,

        /** Total number of pages given the current page size. */
        int totalPages,

        /** {@code true} if this is the first page. */
        boolean first,

        /** {@code true} if this is the last page. */
        boolean last
) {

    /**
     * Builds a {@link PageResponse} from a Spring Data {@link Page}.
     *
     * @param <T>  the content element type
     * @param page the Spring Data page to convert; must not be {@code null}
     * @return a populated {@code PageResponse} record
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
