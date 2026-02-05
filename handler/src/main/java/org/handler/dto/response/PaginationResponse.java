package org.handler.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PaginationResponse<T> {
    private Collection<T> items;
    private Integer totalPages;
    private long totalElements;
    private Integer size;
    private Integer pageNumber;
}
