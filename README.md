## LeetMate Platform Backend

This repository now contains a Spring Boot 3 service that implements the core LeetMate mentor–mentee workflows defined in `docs/LeetMate_PRD.md`.

### Features
- Mentor-managed study groups with creation, browsing, and member listings.
- Dual onboarding flows:
  - **Applications** – mentees submit motivation/experience; mentors review & approve.
  - **Invites** – mentors directly invite mentees; acceptances auto-create memberships.
- Lightweight notification sandbox (`POST /notifications/test`) for PRD verification.
- Global error handling plus validation for misuse (role mismatches, duplicate apps, etc.).

### Tech Stack
- Java 17, Spring Boot 3 (Web, Data JPA, Validation)
- H2 in-memory DB (PostgreSQL compatibility mode for local/testing)
- Maven for builds & tests

### Running Locally
```bash
cd backend
mvn spring-boot:run
```

The service will expose the REST API on `http://localhost:8080`.

### Tests
Full integration tests cover the application and invite flows, including membership creation and uniqueness guarantees.
```bash
cd backend
mvn -q test
```

## Frontend (Next.js + Tailwind)

The `frontend/` directory hosts a Next.js 14 app that mirrors the provided landing-page mock with mentor/mentee CTAs, group highlights, and AI callouts.

### Getting started
```bash
cd frontend
npm install
npm run dev
```

Visit `http://localhost:3000` to explore the UI. Use `npm run lint` to keep the TypeScript/Next.js codebase healthy.
