package com.olehprukhnytskyi.macrotrackerintakeservice.repository;

import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntakeRepository extends JpaRepository<Intake, Long> {
}
