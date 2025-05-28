package com.olehprukhnytskyi.macrotrackerintakeservice.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.Data;

@Data
@Entity
public class Intake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String foodId;

    @Embedded
    private Nutriments nutriments = new Nutriments();
    private String foodName;

    private LocalDate date = LocalDate.now();
    private int amount;
}
