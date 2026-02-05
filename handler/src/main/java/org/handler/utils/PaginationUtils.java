package org.handler.utils;

import org.handler.dto.request.PaginationRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


public class PaginationUtils {
    public static Pageable getPageable(PaginationRequest paginationRequest) {
        return PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                paginationRequest.getDirection(),
                paginationRequest.getSortField());
    }
}
