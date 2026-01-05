package com.olehprukhnytskyi.macrotrackerintakeservice.repository.jpa;

import com.olehprukhnytskyi.macrotrackerintakeservice.model.MealTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealTemplateRepository extends JpaRepository<MealTemplate, Long> {
    Optional<MealTemplate> findByIdAndUserId(Long id, Long userId);
}
