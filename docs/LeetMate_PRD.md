# LeetMate – Current Product Requirements

## Overview
LeetMate is a mentor-led study-group platform for interview prep. Mentors create groups and publish coding challenges; mentees join groups, submit solutions, and discuss in threaded chat. AI provides fast code reviews. This PRD reflects what is implemented today.

## Personas & Goals
- **Mentor**: create/manage groups; post challenges; lead discussions.
- **Mentee**: join groups; submit solutions; participate in threads; receive AI feedback.
- Goals: structured social practice, quick feedback loops, low-friction onboarding (JWT auth), and reliable APIs.

## In-Scope Features (implemented)
1) **Authentication**
   - Register/login with role (MENTOR/MENTEE); JWT issued and validated per request.
   - Password reset with time-bound tokens.
2) **Groups & Membership**
   - Mentors create groups with tags/description.
   - Public browse + detail; list mentees in a group.
   - Mentees join/leave; membership count kept in sync.
3) **Challenges**
   - Mentors post challenges under their group (language, difficulty, starter code).
   - Public read/list per group.
4) **Submissions & AI Review**
   - Mentees submit code for a challenge.
   - AI (or mock) review adds summary, suggestions, and cyclomatic complexity.
   - Submissions retrievable by id and per-challenge with pagination.
5) **Discussions**
   - Group-scoped threads; only mentor or enrolled mentees can read/write.
   - Messages support markdown content, optional `codeLanguage`, and threaded replies via `parentMessageId`.

## Out of Scope / Future (not implemented)
- Applications/approvals workflows.
- Mentor invites/notifications.
- Reactions, attachments, analytics, scheduling, leaderboards.
- Real-time transport (WebSocket/SSE) and Redis cache.
- CI/CD pipeline, OpenAPI generation, and migrations beyond simple SQL init.

## Data Model (current tables/entities)
- `users` (`User`, `UserRole`), `study_groups` (`StudyGroup`), `group_members` (`GroupMember`).
- `challenges` (`Challenge`), `submissions` + `submission_reviews` (`Submission`, `SubmissionReview`).
- `chat_threads` (`ChatThread`), `chat_messages` (`ChatMessage` with optional `parent_id` for replies).

## API Surface (current)
- **Auth**: `/auth/register`, `/auth/login`, `/auth/password/forgot`, `/auth/password/reset`.
- **Groups**: `/groups/create`, `/groups`, `/groups/{id}`, `/groups/{id}/join`, `/groups/{id}/leave`, `/groups/mentees/{menteeId}`, `/groups/{groupId}/mentees`.
- **Challenges**: `/groups/{groupId}/challenges`, `/groups/{groupId}/challenges` (GET), `/challenges/{id}`.
- **Submissions**: `/challenges/{id}/submissions`, `/submissions/{id}`, list submissions for a challenge.
- **Discussions**: `/groups/{groupId}/threads` (create/list), `/threads/{threadId}` (get), `/threads/{threadId}/messages` (post/list with replies).

## Success Criteria (current phase)
- Auth + role guard coverage for all protected routes.
- Membership enforcement for discussion access.
- AI review responses consistently returned for submissions.
- Pagination and validation aligned with `PageResponse`/`ApiErrorResponse` contracts.

## Risks / Gaps to address next
- No production migrations; add Flyway/Liquibase to codify schema (including `chat_messages.parent_id`).
- No real-time updates for chat; currently poll-based.
- Tests light on chat/challenge edge cases (e.g., reply validation, ownership checks).
- No notifications/invites/applications despite prior PRD mentions—keep them in the roadmap section only.
