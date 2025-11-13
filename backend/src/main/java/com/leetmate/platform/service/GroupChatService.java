package com.leetmate.platform.service;

import com.leetmate.platform.dto.chat.CreateMessageRequest;
import com.leetmate.platform.dto.chat.CreateThreadRequest;
import com.leetmate.platform.dto.chat.MessageResponse;
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

    @Transactional
    public MessageResponse postMessage(UUID threadId, UUID authorId, CreateMessageRequest request) {
        ChatThread thread = chatThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread %s not found".formatted(threadId)));
        ensureGroupAccess(thread.getGroup(), authorId);
        User author = findUser(authorId);
        ChatMessage message = new ChatMessage(
                UUID.randomUUID(),
                thread,
                author,
                request.getContent(),
                request.getCodeLanguage(),
                Instant.now()
        );
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
                message.getCreatedAt()
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
