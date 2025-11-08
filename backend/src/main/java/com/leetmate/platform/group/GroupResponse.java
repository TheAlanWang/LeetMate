package com.leetmate.platform.group;

public record GroupResponse(
        Long id,
        String title,
        String topic,
        String level,
        Long mentorId,
        String mentorName,
        String description,
        int capacity,
        boolean active
) {

    public static GroupResponse fromEntity(StudyGroup group) {
        return new GroupResponse(
                group.getId(),
                group.getTitle(),
                group.getTopic(),
                group.getLevel(),
                group.getMentor().getId(),
                group.getMentor().getName(),
                group.getDescription(),
                group.getCapacity(),
                group.isActive()
        );
    }
}
