## LeetMate — Phase 0 Backend PRD

| Item | Value |
| --- | --- |
| Version | 1.0 |
| Tech stack | Java 17 · Spring Boot · ChatGPT API |
| Storage | In-memory (ConcurrentHashMap) |
| Auth | None |
| Focus | Clean architecture · ChatGPT code review · Comprehensive tests · JavaDoc |

---

### 1. Product Overview
LeetMate is a mentor–mentee learning hub. Mentors create groups and post LeetCode-style challenges; mentees submit code. Every submission earns:
- ✅ ChatGPT review
- ✅ Cyclomatic complexity score
- ✅ +1 credit reward

Phase 0 delivers the backend only (no DB, auth, or premium features). All data sits in memory, and every public surface receives JavaDoc plus automated tests.

---

### 2. Scope Snapshot
| ✅ Included | ❌ Excluded |
| --- | --- |
| Group create/list/details/join/leave | User accounts & login |
| Challenge create/list/details | Mentor/mentee roles |
| Submission flow with AI review + credits | Relational DB / JPA |
| Cyclomatic complexity utility | Messaging, notifications |
| In-memory repositories & REST APIs | Payments / premium features |
| Validation + global error handler | Async processing |
| Unit + integration tests (≥80% line & branch) | Email, profiles, credit tracking |
| JavaDoc on all public types | |

---

### 3. Architecture Overview
```
com.leetmate
 ├─ controller/      REST endpoints
 ├─ service/         business logic
 ├─ repository/      ConcurrentHashMap persistence
 ├─ entity/          aggregates (group/challenge/submission)
 ├─ dto/             request & response payloads
 ├─ ai/              ChatGPT provider + mock profile
 ├─ util/            cyclomatic complexity calculator
 ├─ exception/       global handler + API error model
 ├─ config/          WebClient + OpenAI properties
 └─ LeetMateApplication.java
```
Guidelines: strict layering, DTO/entity separation, AI provider abstraction, test-first mindset, consistent naming, no hardcoded constants.

---

### 4. Data Models
- **Group**
  ```
  {
    "id": "UUID",
    "name": "string",
    "description": "string",
    "tags": ["string"],
    "memberCount": 0,
    "createdAt": "ISO-8601"
  }
  ```
- **Challenge**
  ```
  {
    "id": "UUID",
    "groupId": "UUID",
    "title": "string",
    "description": "string",
    "language": "java|python|cpp|js",
    "difficulty": "EASY|MEDIUM|HARD",
    "starterCode": "string",
    "createdAt": "ISO-8601"
  }
  ```
- **Submission + Review**
  ```
  {
    "id": "UUID",
    "challengeId": "UUID",
    "language": "string",
    "code": "string",
    "creditsAwarded": 1,
    "createdAt": "ISO-8601",
    "review": {
      "id": "UUID",
      "summary": "string",
      "complexity": 5,
      "suggestions": ["string"],
      "createdAt": "ISO-8601"
    }
  }
  ```

---

### 5. REST API Surface
| Area | Endpoint | Notes |
| --- | --- | --- |
| Groups | `POST /groups` | Create (`name`,`description`,`tags[]`) |
| | `GET /groups?page=&size=` | Paginated listing |
| | `GET /groups/{groupId}` | Details |
| | `POST /groups/{groupId}/join` | Increment member count |
| | `POST /groups/{groupId}/leave` | Decrement member count |
| Challenges | `POST /groups/{groupId}/challenges` | Create challenge |
| | `GET /groups/{groupId}/challenges` | List group challenges |
| | `GET /challenges/{challengeId}` | Challenge details |
| Submissions | `POST /challenges/{challengeId}/submissions` | Validate → save → complexity → ChatGPT → +1 credit → return |
| | `GET /submissions/{submissionId}` | Submission details |
| | `GET /challenges/{challengeId}/submissions?page=&size=` | Paginated submissions |

---

### 6. ChatGPT Integration
- Endpoint: `POST https://api.openai.com/v1/chat/completions`
- Model: `gpt-4o-mini`
- Prompt template:
  ```
  You are a senior software engineer.
  Review the following <language> code. Provide:
  1. Concise summary
  2. Potential issues or errors
  3. Suggestions for improvement
  4. Optional performance/complexity notes

  <USER_CODE>
  ```
- Interface: `AiReviewResult review(String language, String code)`
- Production provider: uses WebClient + `OPENAI_API_KEY`
- Test provider: mock bean returning deterministic summary & suggestions (no network)

---

### 7. Repositories
- Plain `ConcurrentHashMap<UUID, T>` implementations
- Methods: `save`, `findById`, `findAll`, `findByGroupId`, `findByChallengeId`, `delete/deleteAll`
- Requirements: thread-safe access, JavaDoc, dedicated unit tests

---

### 8. Cyclomatic Complexity Utility
- Signature: `int calculateComplexity(String code)`
- Counts: `if`, `for`, `while`, `case`, `catch`, `&&`, `||`, `?`
- Must strip comments and string/char literals before counting
- ≥10 unit tests + JavaDoc + pure function (no shared state)

---

### 9. Validation & Error Handling
- Spring Validation: `@NotBlank`, `@NotNull`, `@Pattern`, `@Size`, etc.
- Global error payload:
  ```json
  {
    "timestamp": "...",
    "status": 400,
    "error": "Bad Request",
    "message": "language: must not be blank",
    "path": "/challenges/123/submissions"
  }
  ```
- Deliverables: controller validation, `@RestControllerAdvice` handler, unit tests, JavaDoc.

---

### 10. Testing Strategy (Critical)
- **Unit tests** (MockMvc/services/repositories/AI mock/utilities/exception handler)
- **Integration tests** (MockMvc + Spring context) verifying:
  - Group → challenge → submission chain
  - JSON structure & HTTP codes
  - Credit awarding & review embedding
- Coverage target: ≥80% line AND ≥80% branch

---

### 11. JavaDoc Standards
All public classes/interfaces/methods/DTOs/utilities must include:
- Description of behavior
- Parameter documentation
- Return value explanation
- Declared exceptions (if any)

---

### 12. Non-Functional Requirements
- Consistent naming & packaging
- Pagination on list endpoints
- Logging for key actions
- Configuration via environment variables (API key, etc.)
- Maintainability-first (clean code, no magic numbers)

---

### 13. Phase 1+ Preview (Out of Scope Now)
- PostgreSQL + JPA/Hibernate
- JWT authentication & user accounts
- Mentor/mentee roles, profiles, credit tracking
- Group chat, notifications, email
- Premium groups with payments
- Async AI processing queue
