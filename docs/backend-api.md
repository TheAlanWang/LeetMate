# LeetMate Backend API Reference

> Scope: the current Spring Boot service under `backend/` (default base URL `http://localhost:8080`). All endpoints expect `application/json`, timestamps follow ISO-8601 UTC (for example `2024-05-30T12:45:02Z`), and IDs are UUID strings.

## 1. Authentication & Global Conventions

- **Authorization header**: Protected endpoints require `Authorization: Bearer <JWT>`. Tokens are issued by `/auth/login` or `/auth/register`.
- **Roles**: `MENTOR` and `MENTEE`. Method-level `@PreAuthorize` guards enforce who can call each endpoint.
- **CORS**: Allowed origins include `http://localhost:*`, `http://127.0.0.1:*`, `http://0.0.0.0:*`, and `http://*.local:*`.
- **Pagination schema** (`PageResponse<T>`):

| Field | Type | Description |
| --- | --- | --- |
| `content` | `T[]` | Items in the current page |
| `page` | `number` | Zero-based page index |
| `size` | `number` | Page size requested |
| `totalElements` | `number` | Total items in the dataset |
| `totalPages` | `number` | Total number of pages |

- **Error contract** (`ApiErrorResponse`):

```json
{
  "timestamp": "2024-05-30T12:45:02Z",
  "status": 400,
  "error": "Bad Request",
  "message": "email: must be valid",
  "path": "/auth/register"
}
```

Typical codes: `400` validation/business errors, `401` unauthenticated, `403` forbidden, `404` missing resource. Other cases (such as bad input) are normalized to `400`.

## 2. Auth Module (`/auth`)

### 2.1 POST `/auth/register`

- **Access**: Public
- **Purpose**: Create either a mentor or mentee account and immediately return a JWT.
- **Request body**

| Field | Type | Required | Validation | Notes |
| --- | --- | --- | --- | --- |
| `name` | string | ✅ | `1-80` chars | Display name |
| `email` | string | ✅ | Must be unique, valid email | Login email (case-insensitive) |
| `password` | string | ✅ | `6-100` chars | Login password |
| `role` | string | ✅ | `MENTOR` or `MENTEE` (case-insensitive) | Account role |

- **Response** `AuthResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `token` | string | JWT for subsequent calls |
| `user` | object | `UserSummary` (`id`, `name`, `email`, `role`) |

- **Status codes**: `201 Created` on success, `400` if email already exists or role is invalid.

### 2.2 POST `/auth/login`

- **Access**: Public
- **Purpose**: Authenticate an existing account.
- **Request body**

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `email` | string | ✅ | Registered email |
| `password` | string | ✅ | Password |

- **Response**: Same as register
- **Status codes**: `200 OK`, `401 Unauthorized` for bad credentials.

### 2.3 POST `/auth/password/forgot`

- **Access**: Public
- **Purpose**: Issue a password reset token and email (logged in dev) a link valid for 1 hour.
- **Request body**

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `email` | string | ✅ | Email of the account to reset |

- **Response** `MessageResponse`
- **Status codes**: `202 Accepted` even if the email is unknown (prevents user enumeration).

### 2.4 POST `/auth/password/reset`

- **Access**: Public
- **Purpose**: Update the password using a token from the reset email.
- **Request body**

| Field | Type | Required | Validation | Notes |
| --- | --- | --- | --- | --- |
| `token` | string | ✅ | UUID | Token from the email |
| `newPassword` | string | ✅ | `6-100` chars | Replacement password |

- **Response** `MessageResponse`
- **Status codes**: `200 OK`, `400` when the token is invalid, used, or expired.

## 3. Study Groups (`/groups`)

> `GroupResponse` fields: `id`, `name`, `description`, `tags (string[])`, `memberCount`, `createdAt`, `mentorId`, `mentorName`.

### 3.1 POST `/groups/create`

- **Access**: `MENTOR`
- **Purpose**: Mentor creates a study group. Alias: `POST /groups`.
- **Request** `CreateGroupRequest`

| Field | Type | Required | Validation | Notes |
| --- | --- | --- | --- | --- |
| `name` | string | ✅ | ≤ 80 chars | Group name |
| `description` | string | ✅ | ≤ 400 chars | Group description |
| `tags` | string[] | ✅ | Non-empty, ≤ 5 entries, each ≤ 30 chars | Topic tags |

- **Response**: `201 Created`, returns `GroupResponse`

### 3.2 GET `/groups`

- **Access**: Public
- **Purpose**: Paginated list of groups ordered by creation time (desc).
- **Query params**

| Param | Default | Notes |
| --- | --- | --- |
| `page` | `0` | Zero-based |
| `size` | `20` | Range `1-100` |

- **Response**: `PageResponse<GroupResponse>`

### 3.3 GET `/groups/{groupId}`

- **Access**: Public
- **Purpose**: Fetch details for a single group.
- **Path**: `groupId` (UUID)
- **Response**: `GroupResponse`

### 3.4 POST `/groups/{groupId}/join`

- **Access**: `MENTEE`
- **Purpose**: Join a group. Duplicate joins raise `400`.
- **Path**: `groupId`
- **Response**: Updated `GroupResponse`

### 3.5 POST `/groups/{groupId}/leave`

- **Access**: `MENTEE`
- **Purpose**: Leave a group you previously joined. Missing membership yields `404`.
- **Response**: Updated `GroupResponse`

### 3.6 GET `/groups/mentees/{menteeId}`

- **Access**: Mentor or the mentee themselves
- **Purpose**: List every group the mentee has joined. Non-mentors can only access their own memberships.
- **Response**: `GroupResponse[]`

### 3.7 GET `/groups/{groupId}/mentees`

- **Access**: Public
- **Purpose**: Enumerate mentees following the group.
- **Response items** `GroupMemberResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `id` | UUID | Mentee ID |
| `name` | string | Mentee display name |
| `email` | string | Mentee email |
| `joinedAt` | datetime | Join timestamp |

## 4. Challenges (`/groups/{groupId}/challenges`, `/challenges`)

> `ChallengeResponse` fields: `id`, `groupId`, `title`, `description`, `language` (lowercase), `difficulty` (`EASY/MEDIUM/HARD`), `starterCode`, `createdAt`.

### 4.1 POST `/groups/{groupId}/challenges`

- **Access**: `MENTOR` and must own the group
- **Purpose**: Publish a challenge under a group.
- **Request** `CreateChallengeRequest`

| Field | Type | Required | Validation | Notes |
| --- | --- | --- | --- | --- |
| `title` | string | ✅ | ≤ 120 chars | Challenge title |
| `description` | string | ✅ | Any length | Problem statement |
| `language` | string | ✅ | Regex `java|python|cpp|js` (case-insensitive) | Recommended language |
| `difficulty` | string | ✅ | `EASY/MEDIUM/HARD` | Difficulty |
| `starterCode` | string | ✅ | Any | Starter snippet |

- **Response**: `201 Created`, returns `ChallengeResponse`

### 4.2 GET `/groups/{groupId}/challenges`

- **Access**: Public
- **Purpose**: List group challenges (newest first).
- **Response**: `ChallengeResponse[]`

### 4.3 GET `/challenges/{challengeId}`

- **Access**: Public
- **Purpose**: Fetch a single challenge.
- **Response**: `ChallengeResponse`

## 5. Submissions (`/challenges/{challengeId}/submissions`, `/submissions/{id}`)

> `SubmissionResponse` fields: `id`, `challengeId`, `menteeId`, `menteeName`, `language`, `code`, `creditsAwarded`, `createdAt`, `review`.  
> `review` (`ReviewResponse`) includes `id`, `summary`, `complexity`, `suggestions (string[])`, `createdAt`.

### 5.1 POST `/challenges/{challengeId}/submissions`

- **Access**: `MENTEE`
- **Purpose**: Submit solution code. Backend workflow:
  1. Validate the challenge exists.
  2. Persist submission and award `creditsAwarded = 1`.
  3. Request an AI (or mock) review and cyclomatic complexity score.
- **Request** `SubmitSolutionRequest`

| Field | Type | Required | Validation | Notes |
| --- | --- | --- | --- | --- |
| `language` | string | ✅ | Non-empty | Submission language |
| `code` | string | ✅ | ≤ 10,000 chars | Code body |

- **Response**: `201 Created`, returns `SubmissionResponse`

### 5.2 GET `/submissions/{submissionId}`

- **Access**: Any authenticated user
- **Purpose**: Retrieve a single submission including review data.
- **Response**: `SubmissionResponse`

### 5.3 GET `/challenges/{challengeId}/submissions`

- **Access**: Public
- **Purpose**: Paginated submissions for a challenge (newest first).
- **Query params**: `page` default `0`, `size` default `20`, max `100`
- **Response**: `PageResponse<SubmissionResponse>`

> ⚠ Because the payload returns raw `code`, front-end clients should decide if non-authors can see it.

## 6. Group Chat

> `ThreadResponse` fields: `id`, `groupId`, `title`, `description`, `createdAt`, `createdById`, `createdByName`.  
> `MessageResponse` fields: `id`, `threadId`, `authorId`, `authorName`, `authorRole`, `content`, `codeLanguage`, `createdAt`, `parentMessageId`.

All endpoints require authentication. The service also checks whether the caller is the group mentor or a joined mentee; otherwise it responds with `403 You are not part of this group`.

### 6.1 POST `/groups/{groupId}/threads`

- **Access**: `MENTOR` or `MENTEE`
- **Purpose**: Create a discussion thread inside a group, optionally seeding the first message.
- **Request** `CreateThreadRequest`

| Field | Type | Required | Validation | Notes |
| --- | --- | --- | --- | --- |
| `title` | string | ✅ | ≤ 160 chars | Thread title |
| `description` | string | ❌ | ≤ 2,000 chars | Thread description |
| `initialMessage` | string | ❌ | ≤ 8,000 chars | Optional first message |
| `codeLanguage` | string | ❌ | ≤ 30 chars | Language tag for the first message |

- **Response**: `201 Created`, returns `ThreadResponse`

### 6.2 GET `/groups/{groupId}/threads`

- **Access**: `MENTOR` or `MENTEE`
- **Purpose**: Paginated list of threads within a group.
- **Query params**: `page` default `0`, `size` default `20`, max `100`
- **Response**: `PageResponse<ThreadResponse>`

### 6.3 GET `/threads/{threadId}`

- **Access**: `MENTOR` or `MENTEE`
- **Purpose**: Fetch a single thread (membership required for the owning group).
- **Response**: `ThreadResponse`

### 6.4 POST `/threads/{threadId}/messages`

- **Access**: `MENTOR` or `MENTEE`
- **Purpose**: Post a message or reply to a thread.
- **Request** `CreateMessageRequest`

| Field | Type | Required | Validation | Notes |
| --- | --- | --- | --- | --- |
| `content` | string | ✅ | ≤ 8,000 chars | Message body |
| `codeLanguage` | string | ❌ | ≤ 30 chars | Optional language tag |
| `parentMessageId` | UUID | ❌ | Must belong to the same thread | When provided, the new message is a reply |

- **Response**: `201 Created`, returns `MessageResponse`

### 6.5 GET `/threads/{threadId}/messages`

- **Access**: `MENTOR` or `MENTEE`
- **Purpose**: Paginated messages in chronological order.
- **Query params**: `page` default `0`, `size` default `50`, max `100`
- **Response**: `PageResponse<MessageResponse>`

## 7. Debugging Tips

1. **Create two accounts** (mentor + mentee) and store the tokens.
2. **Mentor flow**: `/groups/create`, `/groups/{id}/challenges`.
3. **Mentee flow**: `/groups/{id}/join`, `/challenges/{id}/submissions`, `/threads/...`.
4. **403 diagnostics**: Confirm you send the correct JWT role and that the caller has joined the relevant group.

These endpoints cover the core mentor/mentee workflow from the PRD. Extend the document alongside future surfaces (invites, approvals, notifications, etc.) to keep it authoritative.
