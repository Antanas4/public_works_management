package org.handler.repository;

import org.handler.model.ProcessingAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessingActionRepository extends JpaRepository<ProcessingAction, Long> {
    Optional<ProcessingAction> findById(Long id);
}
