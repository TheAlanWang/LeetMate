package com.leetmate.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.leetmate.platform.dto.chat.CreateMessageRequest;
import com.leetmate.platform.entity.ChatMessage;
import com.leetmate.platform.entity.ChatThread;
import com.leetmate.platform.entity.StudyGroup;
import com.leetmate.platform.entity.User;
import com.leetmate.platform.entity.UserRole;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.ChatMessageRepository;
import com.leetmate.platform.repository.ChatThreadRepository;
import com.leetmate.platform.repository.GroupMemberRepository;
import com.leetmate.platform.repository.StudyGroupRepository;
import com.leetmate.platform.repository.UserRepository;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class GroupChatServiceTest {

    @Mock
    private ChatThreadRepository chatThreadRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    private GroupChatService service;

    private UUID groupId;
    private UUID threadId;
    private UUID authorId;
    private User mentor;
    private User author;
    private StudyGroup group;
    private ChatThread thread;

    @BeforeEach
    void setUp() {
        service = new GroupChatService(
                chatThreadRepository,
                chatMessageRepository,
                studyGroupRepository,
                groupMemberRepository,
                userRepository,
                notificationService);

        groupId = UUID.randomUUID();
        threadId = UUID.randomUUID();
        authorId = UUID.randomUUID();

        mentor = new User(UUID.randomUUID(), "Mentor", "mentor@test.com", "hash", UserRole.MENTOR, Instant.now());
        author = new User(authorId, "Mentee", "mentee@test.com", "hash", UserRole.MENTEE, Instant.now());
        group = new StudyGroup(groupId, mentor, "G", "Desc", List.of(), Instant.now());
        thread = new ChatThread(threadId, group, mentor, "Title", "Desc", Instant.now());
    }

    @Test
    void postMessage_allowsReplyWithinSameThread() throws Exception {
        UUID parentId = UUID.randomUUID();
        ChatMessage parent = new ChatMessage(parentId, thread, author, "parent", "java", Instant.now());

        when(chatThreadRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(chatMessageRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(groupMemberRepository.existsByGroupIdAndMemberId(groupId, authorId)).thenReturn(true);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateMessageRequest request = buildMessageRequest("child", "java", parentId);

        var response = service.postMessage(threadId, authorId, request);

        assertEquals(threadId, response.getThreadId());
        assertEquals(parentId, response.getParentMessageId());
        assertEquals(authorId, response.getAuthorId());
    }

    @Test
    void postMessage_rejectsParentFromAnotherThread() throws Exception {
        UUID otherThreadId = UUID.randomUUID();
        ChatThread otherThread = new ChatThread(otherThreadId, group, mentor, "Other", null, Instant.now());
        ChatMessage parent = new ChatMessage(UUID.randomUUID(), otherThread, author, "parent", null, Instant.now());

        when(chatThreadRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(chatMessageRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(groupMemberRepository.existsByGroupIdAndMemberId(groupId, authorId)).thenReturn(true);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

        CreateMessageRequest request = buildMessageRequest("child", null, parent.getId());

        assertThrows(AccessDeniedException.class, () -> service.postMessage(threadId, authorId, request));
    }

    @Test
    void postMessage_throwsWhenParentMissing() throws Exception {
        UUID missingParentId = UUID.randomUUID();

        when(chatThreadRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(chatMessageRepository.findById(missingParentId)).thenReturn(Optional.empty());
        when(groupMemberRepository.existsByGroupIdAndMemberId(groupId, authorId)).thenReturn(true);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

        CreateMessageRequest request = buildMessageRequest("child", null, missingParentId);

        assertThrows(ResourceNotFoundException.class, () -> service.postMessage(threadId, authorId, request));
    }

    private CreateMessageRequest buildMessageRequest(String content, String codeLanguage, UUID parentId) throws Exception {
        CreateMessageRequest request = new CreateMessageRequest();
        setField(request, "content", content);
        setField(request, "codeLanguage", codeLanguage);
        setField(request, "parentMessageId", parentId);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
