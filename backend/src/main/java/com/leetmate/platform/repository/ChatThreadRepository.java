package com.leetmate.platform.repository;

import com.leetmate.platform.entity.ChatThread;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatThreadRepository extends JpaRepository<ChatThread, UUID> {

    Page<ChatThread> findByGroupIdOrderByCreatedAtDesc(UUID groupId, Pageable pageable);
}
