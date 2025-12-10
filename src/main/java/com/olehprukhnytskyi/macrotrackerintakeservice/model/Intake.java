package com.olehprukhnytskyi.macrotrackerintakeservice.model;

import com.olehprukhnytskyi.util.IntakePeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String foodId;

    @Builder.Default
    @Embedded
    private Nutriments nutriments = new Nutriments();

    private String foodName;

    @Column(nullable = false)
    private LocalDate date;

    private int amount;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IntakePeriod intakePeriod = IntakePeriod.SNACK;
}
