package com.leetmate.platform.repository;

import com.leetmate.platform.entity.Submission;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA persistence for submissions.
 */
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Page<Submission> findByChallenge_IdOrderByCreatedAtDesc(UUID challengeId, Pageable pageable);
}
