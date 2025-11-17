## LeetMate Platform

Spring Boot now powers the mentor–mentee workflows defined in `docs/LeetMate_PRD.md`, while the React/Tailwind front-end provides the demo landing page plus a lightweight dashboard for logging in, creating groups/challenges, and submitting code for AI review.

### Backend Features
- **PostgreSQL persistence** with Spring Data JPA (H2 is still used automatically during tests).
- **JWT-based authentication** with mentor/mentee roles, password hashing, and request filtering.
- **Self-service password reset** issuing 1-hour tokens and logging the reset link for local dev.
- **Mentor flows** – create groups, publish challenges inside owned groups.
- **Mentee flows** – join groups, submit code, receive deterministic AI feedback (mocked in `test` profile).
- **Membership tracking** – `group_members` table keeps mentor groups and mentees in sync, preventing duplicate joins and keeping `memberCount` accurate.
- **Global validation & error handling** for clean API responses.
- **Group chat threads** – mentors/mentees create discussion threads, drop messages (supports optional code language metadata), and fetch rolling history for any group they belong to.

### Tech Stack
- Java 17, Spring Boot 3 (Web, Data JPA, Security, WebFlux for the AI client)
- PostgreSQL 15 (local dev) / H2 (tests)
- Maven Wrapper for builds/tests
- React 19 + CRA + Tailwind utility classes on the front-end

### Repository & Code Tour
#### Backend (`backend/`)
- **Entry point & configuration** – `LeetMateApplication` wires up Spring Boot plus `config/*` for security, CORS, OpenAI credentials, the shared `WebClient`, and the optional `SampleDataLoader` seeder that injects demo mentor/mentee/chat data whenever `APP_SEED_DATA_ENABLED=true`.
- **Security** – `config/SecurityConfig.java` configures stateless JWT auth around `/auth/**`, exposes health endpoints, and allows unauthenticated reads for public group/challenge listings. Tokens are issued/validated through `security/JwtService.java`, while `JwtAuthenticationFilter.java` injects principals backed by `UserPrincipal`.
- **HTTP layer** – Controllers under `controller/` stay thin and delegate to services. Highlights: `AuthController` for register/login/reset, `GroupController` for mentor/mentee group flows, `ChallengeController` & `SubmissionController` for coding workflows, and `GroupChatController` for the discussion threads/messages.
- **Domain & persistence** – Entities live in `entity/` (users, study groups, memberships, challenges, submissions/reviews, chat threads/messages, password reset tokens). Repositories in `repository/` rely on Spring Data JPA for pagination queries and membership lookups.
- **Application services** – Core business logic sits in `service/*`:  
  - `AuthService` and `PasswordResetService` manage lifecycle of `User` plus token issuance/logging via `service/notification/LoggingPasswordResetNotifier.java`.  
  - `GroupService` coordinates mentor-owned `StudyGroup`s and `GroupMember` joins/leaves, enforcing role rules and deduping memberships.  
  - `ChallengeService` keeps mentor-only challenge creation and exposes read APIs.  
  - `SubmissionService` persists mentee code, calls an `AiReviewProvider`, and attaches `SubmissionReview` objects that include the computed cyclomatic complexity from `util/CyclomaticComplexityCalculator.java`.  
  - `GroupChatService` enforces access to group threads/messages by checking both mentor ownership and `GroupMember` entries.
- **AI integration** – `ai/ChatGptAiReviewProvider` (active outside the `test` profile) posts to OpenAI’s Chat Completions API using the prompt template defined inside the class, while `ai/MockAiReviewProvider` supplies deterministic data for tests or the `test` profile.
- **Error handling** – `exception/GlobalExceptionHandler.java` normalizes validation, authentication, and domain exceptions into the `ApiErrorResponse` contract so the frontend can display consistent error messages.

#### Frontend (`frontend/`)
- **Routing & auth context** – `src/App.js` bootstraps React Router routes (`/`, `/login`, `/groups`, `/groups/:groupId`) and exposes an `AuthContext` that stores the JWT + user summary into `localStorage`, wrapping `fetch` helpers around the backend’s `/auth/*` endpoints.
- **Landing page** – Within `App.js`, the `LandingPage`, `SearchBar`, `RoleSelection`, `GroupCard`, and `MentorActions` components combine to show hero content, fetch paginated `/groups` data, and allow mentors with valid tokens to POST to `/groups/create` and `/groups/{id}/challenges`.
- **Authentication UX** – `src/LoginPage.js` shares the `AuthPanel` component that toggles between login/register forms, keeps role selection, offers inline password reset (`/auth/password/forgot` + `/auth/password/reset`), and reuses success/error toasts for navigation back to `/`.
- **Group browsing** – `src/GroupListPage.js` currently ships with static mock data to demonstrate filtering interactions (categories, tags) independent of API availability. Hooks around `useAuth()` illustrate where mentee join requests would call `/groups/{id}/join`.
- **Group details & chat** – `src/GroupPage.js` resolves the `groupId` param, fetches live metadata (falling back to demo content) and sketches the mentor/mentee chat feed with optimistic like/reply interactions that will eventually call `/groups/{id}/threads` and `/threads/{id}/messages` once those endpoints are wired up on the UI.

#### High-level flows
1. **Registration/login** – `AuthController` validates payloads, delegates hashing and persistence to `AuthService`, and issues JWTs so the frontend can store the token inside `AuthContext`.
2. **Group lifecycle** – Mentors authenticated via JWT hit `GroupController#createGroup`, which stores `StudyGroup` rows and allows public `GET /groups` or `GET /groups/{id}` access. Mentees call `/groups/{id}/join` or `/leave`, which insert/delete `GroupMember` entries and keep the denormalized `memberCount` accurate.
3. **Challenges & submissions** – Mentors own `/groups/{id}/challenges`, while mentees post solutions to `/challenges/{id}/submissions`. Each submission is recorded, forwarded to the `AiReviewProvider` for a review summary/suggestions, and enriched with computed complexity before being returned to the client.
4. **Discussions** – `GroupChatService` ensures only mentors or enrolled mentees can create threads or exchange messages. Pagination helpers (`PageResponse<T>`) standardize API responses for both thread and message listings.

### Prerequisites
1. **PostgreSQL (optional if you use Docker Compose)**
   ```bash
   docker run --name leetmate-db \
     -e POSTGRES_PASSWORD=postgres \
     -e POSTGRES_DB=leetmate \
     -p 5432:5432 -d postgres:15
   ```
2. **Environment variables** (can be exported or placed in a `.env` file):
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/leetmate
   export SPRING_DATASOURCE_USERNAME=postgres
   export SPRING_DATASOURCE_PASSWORD=postgres
   export JWT_SECRET=change-me-change-me-change-me-change-me
   export REACT_APP_API_BASE=http://localhost:8080
   ```
   The OpenAI integration stays optional; without `OPENAI_API_KEY` the app falls back to the mock reviewer. The frontend auto-detects the backend host (e.g., if you open `http://localhost:3000` it will call `http://localhost:8080`), so exporting `REACT_APP_API_BASE` is only required when you need to point at a completely different domain.
   Password reset emails are logged with a link template controlled via `PASSWORD_RESET_LINK_TEMPLATE` (defaults to `http://localhost:3000/login?resetToken=%s`), so you can point the link at any frontend route.
   Hibernate runs with `spring.jpa.hibernate.ddl-auto=update`, so your dev data survives backend restarts.

### One Command Demo (Docker)
```bash
docker compose up --build
```
Services started:
- `db` – PostgreSQL 15 (host port `55432`, volume `postgres-data`)
- `backend` – Spring Boot service on `http://localhost:8080`
- `frontend` – CRA dev server on `http://localhost:3000`

Stop everything with `Ctrl+C` then `docker compose down`.

#### Connecting to the Compose database from the host
```bash
psql -h localhost -p 55432 -U postgres -d leetmate
# or if you prefer Docker exec:
docker exec -it leetmate-db psql -U postgres -d leetmate
```
Default password: `postgres` (set in `docker-compose.yml`).

### Running the Backend Manually
```bash
cd backend
./mvnw spring-boot:run
```

If you are using the Compose Postgres (exposed on host port **55432**), start it and override the datasource like this:
```bash
docker-compose up -d db
cd backend
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:55432/leetmate \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
./mvnw spring-boot:run
```
The API listens on `http://localhost:8080`. Run tests with `./mvnw -q test`.

#### Seeding demo data
Want to skip manual setup and get sample mentors/mentees plus a populated chat thread?
```bash
cd backend
APP_SEED_DATA_ENABLED=true ./mvnw spring-boot:run
```
That flag creates `mentor9@test.com` / `mentee9@test.com` (password both `password`), a “Daily Challenge Lab” group, and a ready-to-inspect discussion thread with two starter messages. Use those credentials in Postman to exercise the chat endpoints immediately.

### Demo Flow (cURL)
```bash
# 1. Register mentor & mentee
curl -s -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Mentor","email":"mentor@test.com","password":"secret","role":"MENTOR"}' > mentor.json
curl -s -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Mentee","email":"mentee@test.com","password":"secret","role":"MENTEE"}' > mentee.json

# 2. Mentor creates group & challenge
MENTOR_TOKEN=$(jq -r .token mentor.json)
GROUP_ID=$(curl -s -X POST http://localhost:8080/groups \
  -H "Authorization: Bearer $MENTOR_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"DP Mastery","description":"Top-down camp","tags":["dp","java"]}' | jq -r .id)
CHALLENGE_ID=$(curl -s -X POST http://localhost:8080/groups/$GROUP_ID/challenges \
  -H "Authorization: Bearer $MENTOR_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Climb Stairs","description":"经典 DP","language":"java","difficulty":"easy","starterCode":"class Solution {}"}' | jq -r .id)

# 3. Mentee joins & submits code
MENTEE_TOKEN=$(jq -r .token mentee.json)
curl -s -X POST http://localhost:8080/groups/$GROUP_ID/join \
  -H "Authorization: Bearer $MENTEE_TOKEN"
curl -s -X POST http://localhost:8080/challenges/$CHALLENGE_ID/submissions \
  -H "Authorization: Bearer $MENTEE_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"language":"java","code":"class Solution { int climb(int n){ if(n<=2)return n; int a=1,b=2; for(int i=3;i<=n;i++){int c=a+b;a=b;b=c;} return b;} }"}'

# 4. Start a discussion thread inside the group
THREAD_ID=$(curl -s -X POST http://localhost:8080/groups/$GROUP_ID/threads \
  -H "Authorization: Bearer $MENTOR_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Daily Challenge","description":"Share your solutions","initialMessage":"Kick-off prompt for today!"}' \
  | jq -r .id)

# 5. Exchange messages between mentor and mentee
curl -s -X POST http://localhost:8080/threads/$THREAD_ID/messages \
  -H "Authorization: Bearer $MENTOR_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"content":"Try solving Two Sum and post your reasoning."}'
curl -s -X POST http://localhost:8080/threads/$THREAD_ID/messages \
  -H "Authorization: Bearer $MENTEE_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"content":"Here is my Python solution","codeLanguage":"python"}'
curl -s http://localhost:8080/threads/$THREAD_ID/messages \
  -H "Authorization: Bearer $MENTEE_TOKEN" | jq .
```
> ⚠️ Heads-up: calling `/groups/{id}/join` twice with the same mentee now returns `400 Already joined`. Call `/groups/{id}/leave` (or use a different account) before joining again.
```

### Frontend (manual)
```bash
cd frontend
export REACT_APP_API_BASE=http://localhost:8080
npm install
npm start
```
Point the dev server (`http://localhost:3000`) at the backend via `REACT_APP_API_BASE` if you need to override the auto-detected host. The landing page keeps the previous hero/CTA sections while adding:
- Mentor/Mentee auth panel (login/register via API)
- Mentor console to create groups/challenges
- Mentee console to submit solutions and view AI summaries
- Popular groups auto-loaded from `/groups`

### Discussion APIs (threads & replies)
- Create thread: `POST /groups/{groupId}/threads` (MENTOR/MENTEE, must belong to group) with `title`, optional `description`, optional `initialMessage` (markdown), optional `codeLanguage`.
- List threads: `GET /groups/{groupId}/threads?page=&size=` (MENTOR/MENTEE, must belong).
- Get thread: `GET /threads/{threadId}` (MENTOR/MENTEE, must belong).
- Post message or reply: `POST /threads/{threadId}/messages` with `content` (markdown), optional `codeLanguage`, optional `parentMessageId` for replies. Replies must reference a message in the same thread.
- List messages: `GET /threads/{threadId}/messages?page=&size=` returns messages ordered by `createdAt` with `parentMessageId` so the UI can nest replies client-side.

Example (reply):
```bash
curl -X POST http://localhost:8080/threads/$THREAD_ID/messages \
  -H "Authorization: Bearer $MENTEE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Here is my fix\n\n```java\npublic int foo(){return 42;}\n```","parentMessageId":"<PARENT_ID>","codeLanguage":"java"}'
```
