package com.leetmate.platform.application;

public record ApplicationResponse(
        Long id,
        Long groupId,
        Long menteeId,
        String menteeName,
        ApplicationStatus status,
        String message,
        String experienceLevel,
        String availability
) {

    public static ApplicationResponse fromEntity(GroupApplication application) {
        return new ApplicationResponse(
                application.getId(),
                application.getGroup().getId(),
                application.getMentee().getId(),
                application.getMentee().getName(),
                application.getStatus(),
                application.getMessage(),
                application.getExperienceLevel(),
                application.getAvailability()
        );
    }
}
