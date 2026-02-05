package org.handler.repository;

import org.handler.model.Case;
import org.handler.model.enums.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long>, JpaSpecificationExecutor<Case> {
    Page<Case> findByUserId(Long userId, Pageable pageable);

    Long countByUserId(Long userId);

    Optional<Case> findById(Long id);

    @Query("""
                SELECT c FROM Case c
                WHERE c.status = :status AND (
                    SELECT MAX(pa.createdAt) FROM ProcessingAction pa
                    WHERE pa.caseRef = c
                ) < :threshold
            """)

    List<Case> findCasesWithLastProcessingActionBefore(
            @Param("status") CaseStatus status,
            @Param("threshold") LocalDateTime threshold
    );
}
