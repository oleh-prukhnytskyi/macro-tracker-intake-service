package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeResponseDto {
    private Long id;
    private String foodName;
    private int amount;
    private LocalDate date;
    private Nutriments nutriments;
}
