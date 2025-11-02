package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CacheablePage<T> implements Serializable {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public static <T> CacheablePage<T> fromPage(Page<T> page) {
        return new CacheablePage<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }
}
