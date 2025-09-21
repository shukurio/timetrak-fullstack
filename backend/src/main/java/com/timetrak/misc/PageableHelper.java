package com.timetrak.misc;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableHelper {

    public static Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy
        );
        return PageRequest.of(page, size, sort);
    }
}