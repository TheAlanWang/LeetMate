package com.leetmate.platform.invite;

public record InviteResponse(
        Long id,
        Long groupId,
        Long mentorId,
        Long menteeId,
        InviteStatus status
) {

    public static InviteResponse fromEntity(MentorInvite invite) {
        return new InviteResponse(
                invite.getId(),
                invite.getGroup().getId(),
                invite.getMentor().getId(),
                invite.getMentee().getId(),
                invite.getStatus()
        );
    }
}
