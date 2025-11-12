package com.leetmate.platform.repository;

import com.leetmate.platform.entity.GroupMember;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    boolean existsByGroupIdAndMemberId(UUID groupId, UUID memberId);

    Optional<GroupMember> findByGroupIdAndMemberId(UUID groupId, UUID memberId);
}
