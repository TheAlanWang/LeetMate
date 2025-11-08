package com.leetmate.platform.membership;

public record MemberResponse(
        Long userId,
        String name,
        String role,
        String joinedAt
) {
    public static MemberResponse fromMembership(Membership membership) {
        return new MemberResponse(
                membership.getUser().getId(),
                membership.getUser().getName(),
                membership.getUser().getRole().name(),
                membership.getJoinedAt().toString()
        );
    }
}
