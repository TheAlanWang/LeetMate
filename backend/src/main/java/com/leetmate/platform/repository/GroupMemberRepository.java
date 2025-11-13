package com.leetmate.platform.repository;

import com.leetmate.platform.entity.GroupMember;
import com.leetmate.platform.entity.StudyGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    boolean existsByGroupIdAndMemberId(UUID groupId, UUID memberId);

    Optional<GroupMember> findByGroupIdAndMemberId(UUID groupId, UUID memberId);

    @Query("""
            select gm.group
            from GroupMember gm
            where gm.member.id = :memberId
            order by gm.joinedAt desc
            """)
    List<StudyGroup> findGroupsByMemberId(UUID memberId);

    @Query("""
            select gm
            from GroupMember gm
            join fetch gm.member
            where gm.group.id = :groupId
            order by gm.joinedAt asc
            """)
    List<GroupMember> findAllByGroupIdOrderByJoinedAtAsc(UUID groupId);
}
