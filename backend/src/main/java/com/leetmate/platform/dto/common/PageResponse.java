package com.leetmate.platform.dto.common;

import java.util.List;

/**
 * Generic pagination wrapper used by REST responses.
 *
 * @param <T> element type
 */
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    /**
     * Creates a new immutable response.
     *
     * @param content       content list
     * @param page          current page
     * @param size          requested size
     * @param totalElements number of items
     * @param totalPages    total pages
     */
    public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages) {
        this.content = List.copyOf(content);
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    /**
     * @return list content
     */
    public List<T> getContent() {
        return content;
    }

    /**
     * @return current page index
     */
    public int getPage() {
        return page;
    }

    /**
     * @return requested page size
     */
    public int getSize() {
        return size;
    }

    /**
     * @return total number of elements
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * @return total page count
     */
    public int getTotalPages() {
        return totalPages;
    }
}
