package com.olehprukhnytskyi.macrotrackerintakeservice.repository.jpa;

import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IntakeRepository extends JpaRepository<Intake, Long> {
    Page<Intake> findByUserIdAndDate(Long userId, LocalDate date, Pageable pageable);

    Page<Intake> findByUserId(Long userId, Pageable pageable);

    Optional<Intake> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);

    @Transactional
    @Modifying
    @Query(value = """
        DELETE FROM intake 
        WHERE id IN (
            SELECT id FROM intake 
            WHERE user_id = :userId 
            LIMIT :batchSize
        )
            """, nativeQuery = true)
    int deleteBatchByUserId(@Param("userId") Long userId, @Param("batchSize") int batchSize);
}
