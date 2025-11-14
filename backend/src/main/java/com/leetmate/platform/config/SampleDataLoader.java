package com.leetmate.platform.config;

import com.leetmate.platform.entity.ChatMessage;
import com.leetmate.platform.entity.ChatThread;
import com.leetmate.platform.entity.GroupMember;
import com.leetmate.platform.entity.StudyGroup;
import com.leetmate.platform.entity.User;
import com.leetmate.platform.entity.UserRole;
import com.leetmate.platform.repository.ChatMessageRepository;
import com.leetmate.platform.repository.ChatThreadRepository;
import com.leetmate.platform.repository.GroupMemberRepository;
import com.leetmate.platform.repository.StudyGroupRepository;
import com.leetmate.platform.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app.seed-data", name = "enabled", havingValue = "true")
public class SampleDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);
    private static final String MENTOR_EMAIL = "mentor9@test.com";
    private static final String MENTEE_EMAIL = "mentee9@test.com";

    private final UserRepository userRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PasswordEncoder passwordEncoder;

    public SampleDataLoader(UserRepository userRepository,
                            StudyGroupRepository studyGroupRepository,
                            GroupMemberRepository groupMemberRepository,
                            ChatThreadRepository chatThreadRepository,
                            ChatMessageRepository chatMessageRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.chatThreadRepository = chatThreadRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmailIgnoreCase(MENTOR_EMAIL)) {
            log.info("Sample data already present. Skipping seed.");
            return;
        }

        log.info("Seeding demo mentor/mentee, group, and chat data...");
        User mentor = new User(
                UUID.randomUUID(),
                "Mentor Demo",
                MENTOR_EMAIL,
                passwordEncoder.encode("password"),
                UserRole.MENTOR,
                Instant.now());
        User mentee = new User(
                UUID.randomUUID(),
                "Mentee Demo",
                MENTEE_EMAIL,
                passwordEncoder.encode("password"),
                UserRole.MENTEE,
                Instant.now());
        userRepository.saveAll(List.of(mentor, mentee));

        StudyGroup group = new StudyGroup(
                UUID.randomUUID(),
                mentor,
                "Daily Challenge Lab",
                "Practice problems and receive mentor feedback.",
                List.of("daily", "python"),
                Instant.now());
        studyGroupRepository.save(group);

        GroupMember membership = new GroupMember(
                UUID.randomUUID(),
                group,
                mentee,
                Instant.now());
        groupMemberRepository.save(membership);

        ChatThread thread = new ChatThread(
                UUID.randomUUID(),
                group,
                mentor,
                "Daily Challenge – Two Sum",
                "Share your approaches and get tips.",
                Instant.now());
        chatThreadRepository.save(thread);

        ChatMessage mentorMessage = new ChatMessage(
                UUID.randomUUID(),
                thread,
                mentor,
                "Try solving Two Sum today. Pay attention to hash maps!",
                "text",
                Instant.now());
        ChatMessage menteeMessage = new ChatMessage(
                UUID.randomUUID(),
                thread,
                mentee,
                "Here's my Python solution:\n\n```python\n# two sum code...\n```",
                "python",
                Instant.now());
        chatMessageRepository.save(mentorMessage);
        chatMessageRepository.save(menteeMessage);

        log.info("Demo data ready. Mentor login: {} / password", MENTOR_EMAIL);
    }
}
