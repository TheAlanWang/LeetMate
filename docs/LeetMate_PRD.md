# LeetMate – Collaborative Coding Mentorship Platform

## 🧠 Overview
**LeetMate** is a group-based mentor–mentee platform that turns solo LeetCode practice into an interactive, community-driven experience.  
Mentors create **LeetCode-style study groups** that mentees can **apply to join**, or mentors can **invite mentees directly**.  
This system maintains quality, matching, and balanced group dynamics.

The long-term vision is a **supportive ecosystem** for continuous, social learning — combining human mentorship, collaborative coding, and AI-powered review.

---

## 🎯 Goals
- Transform isolated LeetCode practice into **structured, social learning**.  
- Empower **mentors** to host and manage curated study groups.  
- Allow **mentees** to apply or be invited into mentor-led groups.  
- Deliver **AI-assisted code review** and community-driven growth.  
- Ensure **high-quality engineering** through testing, docs, and CI/CD.

---

## ⚙️ Core Features

### 1) Group-Based Learning
- Mentors create **study groups** with topic, level, and capacity.  
- **Mentees apply** with a short motivation message and experience level.  
- **Mentors can invite** specific mentees (by username or email).  
- Mentors **review applications** and approve/reject from a dashboard.  
- Once accepted, mentees join the group and access shared resources.  
- Group dashboard includes problems, chat channels, leaderboard, and analytics.

### 2) AI-Assisted Code Review
- Auto-evaluates code for correctness, complexity, and readability.  
- Generates hints, optimization advice, and code-style comments.  
- Summarizes threads and code blocks via “/summary” or “/explain” commands.

### 3) Mentor Tools
- Post daily/weekly coding challenges (manual or scheduled).  
- Review submissions manually or with AI assistance.  
- Pin top solutions, share notes, and export weekly group analytics.

### 4) Mentee Experience
- Apply to join groups based on tags (topic, level, mentor reputation).  
- Submit solutions in Python/Java/C++, with metrics captured automatically.  
- See personal progress, streaks, badges, and AI review feedback.  
- Communicate directly in group channels and threads.

### 5) Group Chat & Collaboration
- Slack-style channels: `#daily-challenge`, `#dp`, `#mock-interview`.  
- Threaded replies, syntax-highlighted code blocks, markdown, and reactions.  
- AI bot assists with debugging, summarizing, or clarifying shared code.  
- Real-time sync (WebSocket/SSE) with offline persistence.

### 6) Application & Invite Workflow
**New flow added**

#### Mentee Application Flow
1. Mentee browses groups → clicks “Apply”.  
2. Fills form: short note + prior experience + availability.  
3. Mentor reviews pending applications in their dashboard.  
4. Mentor **accepts** or **rejects**; accepted mentee auto-joins the group.  
5. Mentee receives notification & email confirmation.

#### Mentor Invite Flow
1. Mentor opens “Invite” tab.  
2. Searches user by name/email or selects from previous mentees.  
3. Sends invite (stored in DB + sent via email/notification).  
4. Mentee can **Accept** or **Decline** the invitation.  
5. On acceptance, membership is auto-created.

Both flows ensure transparency and traceability with application/invite logs.

---

## 👥 Target Segments
- CS students preparing for interviews.  
- Career changers seeking mentorship.  
- Bootcamp graduates needing structured support.  
- Experienced engineers mentoring new learners.  

---

## 🧱 Technical Stack
| Layer | Technology |
|------|------------|
| Backend | **Java Spring Boot 3** |
| API Spec | OpenAPI 3 (springdoc-openapi) |
| DB | PostgreSQL 15 + Liquibase |
| Realtime | Spring WebSocket / SSE |
| Cache | Redis |
| Frontend | React + TypeScript + Next.js |
| Auth | JWT (RSA) + refresh token |
| Storage | AWS S3 |
| CI/CD | GitHub Actions + Docker + Terraform/CDK |
| Observability | Micrometer + Prometheus + Grafana |

---

## 🗃️ Database Design

### Core Tables (Updated)

| Table | Description |
|--------|--------------|
| **users** | id, name, email, role (`MENTOR` / `MENTEE`), bio, exp_level |
| **groups** | id, title, topic, mentor_id, level, description, capacity, is_active |
| **applications** | id, group_id, mentee_id, message, status (`PENDING`/`APPROVED`/`REJECTED`), created_at |
| **invites** | id, mentor_id, mentee_id, group_id, status (`PENDING`/`ACCEPTED`/`DECLINED`), created_at |
| **memberships** | id, group_id, user_id, joined_at |
| **problems** | id, group_id, title, difficulty, tags, description, test_cases(json) |
| **submissions** | id, problem_id, user_id, language, code, verdict, runtime_ms, memory_kb |
| **ai_reviews** | id, submission_id, score, comments, complexity, rubric(json) |
| **channels** | id, group_id, name, kind(`general|problem|dm`) |
| **messages** | id, channel_id, author_id, parent_id, body, code_lang, metadata(json) |
| **reactions** | (message_id, user_id, emoji) PK |
| **attachments** | id, message_id, url, mime, bytes |

### Relationships
```
Users ──< Groups
Users ──< Applications >── Groups
Users ──< Invites >── Groups
Users ──< Memberships >── Groups
Groups ──< Problems ──< Submissions ──< AI_Reviews
Groups ──< Channels ──< Messages ──< Reactions
Messages ──< Attachments
Users ──< Messages
```

---

## 📡 API Design (Updated)

### Group Access
- `POST /groups` (MENTOR) – create group  
- `GET /groups` – browse public groups  
- `GET /groups/{id}` – get group detail  
- `GET /groups/{id}/members` – list members  

### Application Flow
- `POST /groups/{id}/apply` (MENTEE) – submit application  
- `GET /groups/{id}/applications` (MENTOR) – list applications  
- `PATCH /applications/{id}` – approve/reject  
- `GET /users/{id}/applications` (MENTEE) – view submitted apps  

### Invite Flow
- `POST /groups/{id}/invite` (MENTOR) – invite mentee  
- `GET /users/{id}/invites` (MENTEE) – list received invites  
- `PATCH /invites/{id}` – accept/decline  
- `DELETE /invites/{id}` – cancel invite  

On acceptance, system inserts record into `memberships`.

### Notifications
- In-app + email notifications for approved applications or accepted invites.  
- `POST /notifications/test` for dev sandbox verification.

---

## 🧪 Testing Strategy (Updated)

### Applications & Invites
- Unit, Integration, E2E, Contract, and Notification tests.  
- Verify DB integrity, concurrency edge cases, and API contracts.  
- Ensure unique applications per mentee per group.

### Javadoc Additions
Controllers, services, and entities under `application` and `invite` packages must follow Javadoc standards.

---

## 📈 Success Metrics
- **1,000+ users** within 6 months.  
- **70%+ acceptance rate** on valid applications.  
- **p95 chat latency < 200 ms**.  
- **80%+ mentee satisfaction** post-group survey.

---

## 🏁 Summary
LeetMate now supports **mentor-driven membership control** through a dual system:
- **Applications** (mentees apply to join).  
- **Invitations** (mentors hand-pick mentees).  

Together, these features ensure quality mentorship, engagement, and robust testing and documentation for scalable development.
