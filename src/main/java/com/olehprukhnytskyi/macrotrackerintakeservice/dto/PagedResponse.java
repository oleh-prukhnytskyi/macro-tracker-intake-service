package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> data;
    private Pagination pagination;
}
