package com.leetmate.platform.service;

import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.dto.notification.NotificationCountResponse;
import com.leetmate.platform.dto.notification.NotificationResponse;
import com.leetmate.platform.entity.ChatMessage;
import com.leetmate.platform.entity.ChatThread;
import com.leetmate.platform.entity.GroupMember;
import com.leetmate.platform.entity.Notification;
import com.leetmate.platform.entity.NotificationType;
import com.leetmate.platform.entity.StudyGroup;
import com.leetmate.platform.entity.User;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.ChatMessageRepository;
import com.leetmate.platform.repository.ChatThreadRepository;
import com.leetmate.platform.repository.GroupMemberRepository;
import com.leetmate.platform.repository.NotificationRepository;
import com.leetmate.platform.repository.StudyGroupRepository;
import com.leetmate.platform.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class NotificationService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MESSAGE_PREVIEW_LENGTH = 100;

    private final NotificationRepository notificationRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               GroupMemberRepository groupMemberRepository,
                               StudyGroupRepository studyGroupRepository,
                               ChatThreadRepository chatThreadRepository,
                               ChatMessageRepository chatMessageRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.chatThreadRepository = chatThreadRepository;
        this.userRepository = userRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * Creates notifications for all group members when a new thread is created.
     * Excludes the thread creator from receiving a notification.
     */
    @Transactional
    public void createNewThreadNotification(UUID groupId, UUID threadId, UUID threadCreatorId) {
        StudyGroup group = findGroup(groupId);
        ChatThread thread = findThread(threadId);
        User actor = findUser(threadCreatorId);

        List<Notification> notifications = new ArrayList<>();

        // Get all group members
        List<GroupMember> members = groupMemberRepository.findAllByGroupIdOrderByJoinedAtAsc(groupId);
        for (GroupMember member : members) {
            // Exclude the thread creator
            if (!member.getMember().getId().equals(threadCreatorId)) {
                Notification notification = new Notification(
                    UUID.randomUUID(),
                    member.getMember(),
                    NotificationType.NEW_THREAD,
                    group,
                    thread,
                    null,  // no message for new thread
                    actor,
                    Instant.now()
                );
                notifications.add(notification);
            }
        }

        // Also notify the mentor if they're not already in the members list and not the creator
        if (group.getMentor() != null 
            && !group.getMentor().getId().equals(threadCreatorId)
            && members.stream().noneMatch(m -> m.getMember().getId().equals(group.getMentor().getId()))) {
            Notification notification = new Notification(
                UUID.randomUUID(),
                group.getMentor(),
                NotificationType.NEW_THREAD,
                group,
                thread,
                null,
                actor,
                Instant.now()
            );
            notifications.add(notification);
        }

        // Batch save all notifications
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    /**
     * Creates a notification for the thread creator when someone replies to their thread.
     * Only notifies if the reply author is different from the thread creator.
     */
    @Transactional
    public void createThreadReplyNotification(UUID threadId, UUID replyMessageId, UUID replyAuthorId) {
        ChatThread thread = findThread(threadId);
        ChatMessage message = findMessage(replyMessageId);
        User replyAuthor = findUser(replyAuthorId);
        User threadCreator = thread.getCreatedBy();

        // Don't notify if the reply author is the thread creator
        if (threadCreator.getId().equals(replyAuthorId)) {
            return;
        }

        Notification notification = new Notification(
            UUID.randomUUID(),
            threadCreator,
            NotificationType.THREAD_REPLY,
            thread.getGroup(),
            thread,
            message,
            replyAuthor,
            Instant.now()
        );

        notificationRepository.save(notification);
    }

    /**
     * Get paginated list of notifications for a user.
     */
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(UUID userId, int page, int size, boolean unreadOnly) {
        validatePagination(page, size);
        findUser(userId);  // verify user exists

        PageRequest pageable = PageRequest.of(page, size);
        Page<Notification> notifications;
        
        if (unreadOnly) {
            notifications = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable);
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        Page<NotificationResponse> responsePage = notifications.map(this::toNotificationResponse);
        
        return new PageResponse<>(
            responsePage.getContent(),
            responsePage.getNumber(),
            responsePage.getSize(),
            responsePage.getTotalElements(),
            responsePage.getTotalPages()
        );
    }

    /**
     * Get unread notification count for a user.
     */
    @Transactional(readOnly = true)
    public NotificationCountResponse getUnreadCount(UUID userId) {
        findUser(userId);  // verify user exists
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return new NotificationCountResponse((int) count);
    }

    /**
     * Mark a specific notification as read.
     */
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        findUser(userId);  // verify user exists
        
        int updated = notificationRepository.markAsRead(notificationId, userId);
        if (updated == 0) {
            throw new ResourceNotFoundException(
                "Notification %s not found or does not belong to user".formatted(notificationId));
        }
    }

    /**
     * Mark all notifications as read for a user.
     */
    @Transactional
    public void markAllAsRead(UUID userId) {
        findUser(userId);  // verify user exists
        notificationRepository.markAllAsReadByUserId(userId);
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        StudyGroup group = notification.getGroup();
        ChatThread thread = notification.getThread();
        User actor = notification.getActor();

        NotificationResponse.GroupInfo groupInfo = group != null
            ? new NotificationResponse.GroupInfo(group.getId(), group.getName())
            : null;

        NotificationResponse.ThreadInfo threadInfo = thread != null
            ? new NotificationResponse.ThreadInfo(thread.getId(), thread.getTitle())
            : null;

        NotificationResponse.ActorInfo actorInfo = actor != null
            ? new NotificationResponse.ActorInfo(
                actor.getId(),
                actor.getName(),
                actor.getRole().name())
            : null;

        NotificationResponse.MessagePreview messagePreview = null;
        if (notification.getType() == NotificationType.THREAD_REPLY && notification.getMessage() != null) {
            ChatMessage message = notification.getMessage();
            String preview = message.getContent();
            if (preview.length() > MESSAGE_PREVIEW_LENGTH) {
                preview = preview.substring(0, MESSAGE_PREVIEW_LENGTH) + "...";
            }
            messagePreview = new NotificationResponse.MessagePreview(message.getId(), preview);
        }

        return new NotificationResponse(
            notification.getId(),
            notification.getType().name(),
            notification.isRead(),
            notification.getCreatedAt(),
            groupInfo,
            threadInfo,
            actorInfo,
            messagePreview
        );
    }

    private StudyGroup findGroup(UUID groupId) {
        return studyGroupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group %s not found".formatted(groupId)));
    }

    private ChatThread findThread(UUID threadId) {
        return chatThreadRepository.findById(threadId)
            .orElseThrow(() -> new ResourceNotFoundException("Thread %s not found".formatted(threadId)));
    }

    private ChatMessage findMessage(UUID messageId) {
        return chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message %s not found".formatted(messageId)));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(userId)));
    }

    private void validatePagination(int page, int size) {
        Assert.isTrue(page >= 0, "page must be greater or equal to 0");
        Assert.isTrue(size > 0 && size <= MAX_PAGE_SIZE,
            "size must be between 1 and " + MAX_PAGE_SIZE);
    }
}


