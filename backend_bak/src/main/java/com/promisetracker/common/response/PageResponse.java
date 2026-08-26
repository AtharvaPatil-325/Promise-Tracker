package com.promisetracker.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <T> PageResponse<T> fromPage(Page<T> pageObj) {
        return PageResponse.<T>builder()
                .content(pageObj.getContent())
                .page(pageObj.getNumber())
                .size(pageObj.getSize())
                .totalElements(pageObj.getTotalElements())
                .totalPages(pageObj.getTotalPages())
                .last(pageObj.isLast())
                .build();
    }
}
