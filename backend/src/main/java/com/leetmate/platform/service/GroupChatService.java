package com.leetmate.platform.service;

import com.leetmate.platform.dto.chat.CreateMessageRequest;
import com.leetmate.platform.dto.chat.CreateThreadRequest;
import com.leetmate.platform.dto.chat.MessageResponse;
import com.leetmate.platform.dto.chat.UpdateMessageRequest;
import com.leetmate.platform.dto.chat.ThreadResponse;
import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.entity.ChatMessage;
import com.leetmate.platform.entity.ChatThread;
import com.leetmate.platform.entity.StudyGroup;
import com.leetmate.platform.entity.User;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.ChatMessageRepository;
import com.leetmate.platform.repository.ChatThreadRepository;
import com.leetmate.platform.repository.GroupMemberRepository;
import com.leetmate.platform.repository.StudyGroupRepository;
import com.leetmate.platform.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
public class GroupChatService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupChatService(ChatThreadRepository chatThreadRepository,
                            ChatMessageRepository chatMessageRepository,
                            StudyGroupRepository studyGroupRepository,
                            GroupMemberRepository groupMemberRepository,
                            UserRepository userRepository) {
        this.chatThreadRepository = chatThreadRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ThreadResponse createThread(UUID groupId, UUID creatorId, CreateThreadRequest request) {
        StudyGroup group = findGroup(groupId);
        User creator = findUser(creatorId);
        ensureGroupAccess(group, creatorId);
        ChatThread thread = new ChatThread(
                UUID.randomUUID(),
                group,
                creator,
                request.getTitle(),
                request.getDescription(),
                Instant.now()
        );
        chatThreadRepository.save(thread);

        if (request.getInitialMessage() != null && !request.getInitialMessage().isBlank()) {
            ChatMessage message = new ChatMessage(
                    UUID.randomUUID(),
                    thread,
                    creator,
                    request.getInitialMessage(),
                    request.getCodeLanguage(),
                    Instant.now()
            );
            chatMessageRepository.save(message);
        }

        return toThreadResponse(thread);
    }

    @Transactional(readOnly = true)
    public PageResponse<ThreadResponse> listThreads(UUID groupId, UUID requesterId, int page, int size) {
        validatePagination(page, size);
        StudyGroup group = findGroup(groupId);
        ensureGroupAccess(group, requesterId);
        PageRequest pageable = PageRequest.of(page, size);
        Page<ThreadResponse> data = chatThreadRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable)
                .map(this::toThreadResponse);
        return new PageResponse<>(
                data.getContent(),
                data.getNumber(),
                data.getSize(),
                data.getTotalElements(),
                data.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public ThreadResponse getThread(UUID threadId, UUID requesterId) {
        ChatThread thread = chatThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread %s not found".formatted(threadId)));
        ensureGroupAccess(thread.getGroup(), requesterId);
        return toThreadResponse(thread);
    }

    @Transactional
    public void deleteThread(UUID threadId, UUID mentorId) {
        ChatThread thread = chatThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread %s not found".formatted(threadId)));
        StudyGroup group = thread.getGroup();
        if (group.getMentor() == null || !group.getMentor().getId().equals(mentorId)) {
            throw new AccessDeniedException("Only the mentor of this group can delete threads");
        }
        chatThreadRepository.delete(thread);
    }

    @Transactional
    public MessageResponse postMessage(UUID threadId, UUID authorId, CreateMessageRequest request) {
        ChatThread thread = chatThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread %s not found".formatted(threadId)));
        ensureGroupAccess(thread.getGroup(), authorId);
        User author = findUser(authorId);
        ChatMessage parent = null;
        if (request.getParentMessageId() != null) {
            parent = chatMessageRepository.findById(request.getParentMessageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent message %s not found".formatted(request.getParentMessageId())));
            if (!parent.getThread().getId().equals(threadId)) {
                throw new AccessDeniedException("Parent message does not belong to this thread");
            }
        }
        ChatMessage message = new ChatMessage(
                UUID.randomUUID(),
                thread,
                author,
                request.getContent(),
                request.getCodeLanguage(),
                Instant.now(),
                parent
        );
        chatMessageRepository.save(message);
        return toMessageResponse(message);
    }

    @Transactional
    public void deleteMessage(UUID threadId, UUID messageId, UUID requesterId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message %s not found".formatted(messageId)));
        if (!message.getThread().getId().equals(threadId)) {
            throw new AccessDeniedException("Message does not belong to this thread");
        }
        ensureGroupAccess(message.getThread().getGroup(), requesterId);
        boolean isMentor = message.getThread().getGroup().getMentor() != null
                && message.getThread().getGroup().getMentor().getId().equals(requesterId);
        boolean isAuthor = message.getAuthor().getId().equals(requesterId);
        if (!isMentor && !isAuthor) {
            throw new AccessDeniedException("Only mentors or message authors can delete messages");
        }
        chatMessageRepository.delete(message);
    }

    @Transactional
    public MessageResponse updateMessage(UUID threadId, UUID messageId, UUID requesterId, UpdateMessageRequest request) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message %s not found".formatted(messageId)));
        if (!message.getThread().getId().equals(threadId)) {
            throw new AccessDeniedException("Message does not belong to this thread");
        }
        ensureGroupAccess(message.getThread().getGroup(), requesterId);
        if (!message.getAuthor().getId().equals(requesterId)) {
            throw new AccessDeniedException("Only the author can edit this message");
        }
        message.setContent(request.getContent());
        chatMessageRepository.save(message);
        return toMessageResponse(message);
    }

    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> listMessages(UUID threadId, UUID requesterId, int page, int size) {
        validatePagination(page, size);
        ChatThread thread = chatThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread %s not found".formatted(threadId)));
        ensureGroupAccess(thread.getGroup(), requesterId);
        PageRequest pageable = PageRequest.of(page, size);
        Page<MessageResponse> data = chatMessageRepository.findByThreadIdOrderByCreatedAtAsc(threadId, pageable)
                .map(this::toMessageResponse);
        return new PageResponse<>(
                data.getContent(),
                data.getNumber(),
                data.getSize(),
                data.getTotalElements(),
                data.getTotalPages()
        );
    }

    private ThreadResponse toThreadResponse(ChatThread thread) {
        return new ThreadResponse(
                thread.getId(),
                thread.getGroup().getId(),
                thread.getTitle(),
                thread.getDescription(),
                thread.getCreatedAt(),
                thread.getCreatedBy().getId(),
                thread.getCreatedBy().getName()
        );
    }

    private MessageResponse toMessageResponse(ChatMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getThread().getId(),
                message.getAuthor().getId(),
                message.getAuthor().getName(),
                message.getAuthor().getRole().name(),
                message.getContent(),
                message.getCodeLanguage(),
                message.getCreatedAt(),
                message.getParent() != null ? message.getParent().getId() : null
        );
    }

    private void ensureGroupAccess(StudyGroup group, UUID userId) {
        if (group.getMentor() != null && group.getMentor().getId().equals(userId)) {
            return;
        }
        boolean isMember = groupMemberRepository.existsByGroupIdAndMemberId(group.getId(), userId);
        if (!isMember) {
            throw new AccessDeniedException("You are not part of this group");
        }
    }

    private StudyGroup findGroup(UUID groupId) {
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group %s not found".formatted(groupId)));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(userId)));
    }

    private void validatePagination(int page, int size) {
        Assert.isTrue(page >= 0, "page must be greater or equal to 0");
        Assert.isTrue(size > 0 && size <= MAX_PAGE_SIZE, "size must be between 1 and " + MAX_PAGE_SIZE);
    }
}
