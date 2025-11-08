package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Cached page wrapper for paginated responses")
public class CacheablePage<T> implements Serializable {
    @Schema(description = "Page content")
    private List<T> content;

    @Schema(description = "Total number of elements")
    private long totalElements;

    @Schema(description = "Total number of pages")
    private int totalPages;

    @Schema(description = "Current page number")
    private int number;

    @Schema(description = "Page size")
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
