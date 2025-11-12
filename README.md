## LeetMate Platform

Spring Boot now powers the mentor–mentee workflows defined in `docs/LeetMate_PRD.md`, while the React/Tailwind front-end provides the demo landing page plus a lightweight dashboard for logging in, creating groups/challenges, and submitting code for AI review.

### Backend Features
- **PostgreSQL persistence** with Spring Data JPA (H2 is still used automatically during tests).
- **JWT-based authentication** with mentor/mentee roles, password hashing, and request filtering.
- **Mentor flows** – create groups, publish challenges inside owned groups.
- **Mentee flows** – join groups, submit code, receive deterministic AI feedback (mocked in `test` profile).
- **Membership tracking** – `group_members` table keeps mentor groups and mentees in sync, preventing duplicate joins and keeping `memberCount` accurate.
- **Global validation & error handling** for clean API responses.

### Tech Stack
- Java 17, Spring Boot 3 (Web, Data JPA, Security, WebFlux for the AI client)
- PostgreSQL 15 (local dev) / H2 (tests)
- Maven Wrapper for builds/tests
- React 19 + CRA + Tailwind utility classes on the front-end

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
The API listens on `http://localhost:8080`. Run tests with `./mvnw -q test`.

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
