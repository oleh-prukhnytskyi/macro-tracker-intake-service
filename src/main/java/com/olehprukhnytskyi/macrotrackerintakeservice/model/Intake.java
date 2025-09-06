package com.olehprukhnytskyi.macrotrackerintakeservice.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Intake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String foodId;

    @Builder.Default
    @Embedded
    private Nutriments nutriments = new Nutriments();
    private String foodName;

    @Builder.Default
    private LocalDate date = LocalDate.now();
    private int amount;
}
