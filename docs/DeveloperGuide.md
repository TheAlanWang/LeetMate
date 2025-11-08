# LeetMate Developer Guide

This guide explains the current codebase so you can quickly understand what was implemented from the PRD and how the pieces fit together.

## Repository Structure

```
LeetMate-Platform/
├── backend/           # Spring Boot 3 service (Java 17, Maven)
│   ├── src/main/java/com/leetmate/platform
│   │   ├── application/   # Application flow (apply/approve)
│   │   ├── invite/        # Mentor invite flow
│   │   ├── group/         # Study group CRUD + queries
│   │   ├── membership/    # Membership projection helpers
│   │   ├── notification/  # Dev-only notification stub
│   │   └── user/          # User aggregate + lookup service
│   └── src/test/java/...  # Integration tests (MockMvc)
├── frontend/          # Next.js 14 + Tailwind landing page
└── docs/              # PRD + this guide
```

## Backend Overview

- **Tech**: Spring Boot 3.2, Spring Web, Spring Data JPA, Validation, H2 (PostgreSQL compatibility mode).
- **Domain Model**:
  - `User` (`MENTOR`/`MENTEE`)
  - `StudyGroup` (mentor-owned)
  - `GroupApplication` (`PENDING/APPROVED/REJECTED`) with uniqueness per mentee+group
  - `MentorInvite` (`PENDING/ACCEPTED/DECLINED/CANCELLED`)
  - `Membership` (created automatically when apps/invites succeed)
- **Services**: `ApplicationService`, `InviteService`, `GroupService`, `MembershipService`, `UserService`.
- **Controllers**: REST endpoints exactly as specified in `docs/LeetMate_PRD.md`, plus a dev-only `NotificationController`.
- **Persistence**: `application.yml` configures `jdbc:h2:mem:leetmate` so no external DB is required. Swap the datasource properties for PostgreSQL later.

### Key Endpoints

| Purpose | Method & Path | Notes |
|---------|---------------|-------|
| Create group | `POST /groups` | Mentor-only (validated via role lookup). Returns `GroupResponse`. |
| Browse groups | `GET /groups` / `GET /groups/{id}` | Lists all study groups and details. |
| List members | `GET /groups/{id}/members` | Reads from `MembershipRepository`. |
| Apply | `POST /groups/{id}/apply` | Mentee submits message/experience/availability. |
| Review apps | `GET /groups/{id}/applications?mentorId={mentor}` / `PATCH /applications/{id}` | Mentor approves or rejects; approval creates membership. |
| View own apps | `GET /users/{id}/applications` | Mentee history. |
| Send invite | `POST /groups/{id}/invite` | Mentor → mentee. Ownership validated. |
| Respond invite | `PATCH /invites/{id}` | Accept/decline; acceptance creates membership. |
| Cancel invite | `DELETE /invites/{id}?mentorId={mentor}` | Marks invite as `CANCELLED`. |
| Notifications | `POST /notifications/test` | Stores payloads in memory for sandbox testing. |

### Testing

- `mvn -q test` runs integration suites:
  - `ApplicationFlowIntegrationTest`: ensures duplicate prevention + membership creation on approval.
  - `InviteFlowIntegrationTest`: ensures only owning mentors can invite and acceptances add members.

### Manual Testing Tips

1. `cd backend && mvn spring-boot:run`.
2. Open `http://localhost:8080/h2-console` (JDBC `jdbc:h2:mem:leetmate`, user `sa`) and insert mentors/mentees via SQL.
3. Use curl/Postman to interact with the endpoints listed above.

## Frontend Overview

- **Tech**: Next.js 14 (App Router) + Tailwind CSS.
- **Location**: `frontend/app/page.tsx` renders the hero bar, mentor/mentee CTA buttons, popular group cards, and AI highlight cards mirroring the supplied mock.
- **Components**: `GroupCard`, `AiHighlightCard` in `frontend/components/`.
- **Styling**: Global Tailwind styles in `app/globals.css` with helper classes (`button-primary`, `card`) and theme extensions (`tailwind.config.ts`).

### Frontend Commands

```bash
cd frontend
npm install         # install deps
npm run dev         # start dev server on http://localhost:3000
npm run lint        # Next.js ESLint/TypeScript checks
```

## Next Steps

- Wire the Next.js UI to backend APIs once authentication is in place.
- Switch `application.yml` to a PostgreSQL profile for persistence outside dev/test.
- Expand tests (e.g., for notifications, error cases, pagination) as new features land.
