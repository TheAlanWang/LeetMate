# LeetMate Backend API 接口文档

> 适用范围：当前 `backend/` Spring Boot 服务（端口默认为 `http://localhost:8080`）。所有请求均使用 `application/json`，时间字段为 ISO-8601 UTC（例如 `2024-05-30T12:45:02Z`），ID 为 UUID 字符串。

## 1. 鉴权与通用约定

- **认证方式**：所有受保护接口需在请求头添加 `Authorization: Bearer <JWT>`。JWT 由 `/auth/login` 或 `/auth/register` 返回。
- **角色**：`MENTOR`（导师）与 `MENTEE`（学员）。方法级 `@PreAuthorize` 会限制不同角色是否可调用。
- **跨域**：默认允许 `http://localhost:*`、`127.0.0.1` 以及 `*.local`。
- **分页统一格式**：接口返回 `PageResponse<T>` 时字段如下：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `content` | `T[]` | 当前页数据 |
| `page` | `number` | 从 0 开始的页码 |
| `size` | `number` | 请求的 page size |
| `totalElements` | `number` | 总记录数 |
| `totalPages` | `number` | 总页数 |

- **错误响应**：所有异常统一包装为 `ApiErrorResponse`：

```json
{
  "timestamp": "2024-05-30T12:45:02Z",
  "status": 400,
  "error": "Bad Request",
  "message": "email: must be valid",
  "path": "/auth/register"
}
```

常见状态码：`400` 校验失败/业务非法；`401` 未登录或 JWT 无效；`403` 权限不足；`404` 资源不存在；`422` 不会单独返回，由 `400` 覆盖。

## 2. 认证模块 (`/auth`)

### 2.1 POST `/auth/register`

- **权限**：公开
- **描述**：注册导师或学员账号，成功后直接返回 JWT。
- **请求体**

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `name` | string | ✅ | `1-80` 字符 | 显示昵称 |
| `email` | string | ✅ | 邮箱格式，唯一 | 登录邮箱，大小写不敏感 |
| `password` | string | ✅ | `6-100` 字符 | 登录密码 |
| `role` | string | ✅ | 枚举 `MENTOR` / `MENTEE`（大小写忽略） | 账号角色 |

- **响应体** `AuthResponse`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `token` | string | JWT，用于后续请求 |
| `user` | object | `UserSummary`：`id`、`name`、`email`、`role` |

- **状态码**：`201 Created` 成功；`400` 邮箱已存在/角色非法。

### 2.2 POST `/auth/login`

- **权限**：公开
- **描述**：已有账号登录。
- **请求体**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `email` | string | ✅ | 注册邮箱 |
| `password` | string | ✅ | 密码 |

- **响应体**：同注册
- **状态码**：`200 OK`；`401 Unauthorized` 凭证错误。

## 3. 学习小组 (`/groups`)

> `GroupResponse` 字段：`id`、`name`、`description`、`tags (string[])`、`memberCount`、`createdAt`、`mentorId`、`mentorName`。

### 3.1 POST `/groups/create`

- **权限**：`MENTOR`（需登录）
- **描述**：导师创建学习小组。
- **请求体** `CreateGroupRequest`

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `name` | string | ✅ | ≤ 80 字符 | 小组名称 |
| `description` | string | ✅ | ≤ 400 字符 | 介绍 |
| `tags` | string[] | ✅ | 不为空，最多 5 个，每个 ≤ 30 字符 | 标签 |

- **响应**：`201 Created`，返回 `GroupResponse`

### 3.2 GET `/groups`

- **权限**：公开
- **描述**：分页浏览所有小组，按创建时间倒序。
- **查询参数**

| 参数 | 默认 | 说明 |
| --- | --- | --- |
| `page` | `0` | 从 0 开始 |
| `size` | `20` | 每页数量，`1-100` |

- **响应**：`PageResponse<GroupResponse>`

### 3.3 GET `/groups/{groupId}`

- **权限**：公开
- **描述**：查看单个小组详情。
- **路径参数**：`groupId` (UUID)
- **响应**：`GroupResponse`

### 3.4 POST `/groups/{groupId}/join`

- **权限**：`MENTEE`
- **描述**：学员加入小组；若重复加入返回 `400`。
- **路径参数**：`groupId`
- **响应**：`GroupResponse`（最新成员数）

### 3.5 POST `/groups/{groupId}/leave`

- **权限**：`MENTEE`
- **描述**：退出已加入的小组；未加入时返回 `404`。
- **响应**：`GroupResponse`

### 3.6 GET `/groups/mentees/{menteeId}`

- **权限**：`MENTOR` 或 查询者本人
- **描述**：列出该学员加入的全部小组。后台会校验：非本人且非导师将返回 `403`。
- **响应**：`GroupResponse[]`

### 3.7 GET `/groups/{groupId}/mentees`

- **权限**：公开
- **描述**：列出指定小组的所有关注学员。
- **响应项** `GroupMemberResponse`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | UUID | 学员 ID |
| `name` | string | 学员姓名 |
| `email` | string | 学员邮箱 |
| `joinedAt` | datetime | 加入时间 |

## 4. 挑战 (`/groups/{groupId}/challenges`, `/challenges`)

> `ChallengeResponse` 字段：`id`、`groupId`、`title`、`description`、`language`（小写）、`difficulty`（`EASY/MEDIUM/HARD`）、`starterCode`、`createdAt`。

### 4.1 POST `/groups/{groupId}/challenges`

- **权限**：`MENTOR` 且必须是该组导师
- **描述**：给指定小组新增一道题目。
- **请求体** `CreateChallengeRequest`

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `title` | string | ✅ | ≤120 字符 | 挑战名称 |
| `description` | string | ✅ | 任意长度 | 题面 |
| `language` | string | ✅ | 正则 `java|python|cpp|js`（忽略大小写） | 推荐语言 |
| `difficulty` | string | ✅ | `EASY/MEDIUM/HARD` | 难度 |
| `starterCode` | string | ✅ | 任意 | 起始代码片段 |

- **响应**：`201 Created`，返回 `ChallengeResponse`

### 4.2 GET `/groups/{groupId}/challenges`

- **权限**：公开
- **描述**：列出小组下的所有挑战（按创建时间倒序）。
- **响应**：`ChallengeResponse[]`

### 4.3 GET `/challenges/{challengeId}`

- **权限**：公开
- **描述**：获取题目详情。
- **响应**：`ChallengeResponse`

## 5. 代码提交 (`/challenges/{challengeId}/submissions`, `/submissions/{id}`)

> `SubmissionResponse` 字段：`id`、`challengeId`、`menteeId`、`menteeName`、`language`、`code`、`creditsAwarded`、`createdAt`、`review`。  
> `review` 为 `ReviewResponse`：`id`、`summary`、`complexity`（圈复杂度）、`suggestions (string[])`、`createdAt`。

### 5.1 POST `/challenges/{challengeId}/submissions`

- **权限**：`MENTEE`
- **描述**：提交代码，后台会：
  1. 校验挑战存在；
  2. 保存提交记录并固定奖励 `creditsAwarded = 1`;
  3. 调用 AI（或本地 mock）生成评审与圈复杂度。
- **请求体** `SubmitSolutionRequest`

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `language` | string | ✅ | 非空 | 提交语言 |
| `code` | string | ✅ | ≤ 10,000 字符 | 代码正文 |

- **响应**：`201 Created`，返回 `SubmissionResponse`

### 5.2 GET `/submissions/{submissionId}`

- **权限**：登录即可（任何角色）
- **描述**：查看单条提交（含 AI 评审）。
- **响应**：`SubmissionResponse`

### 5.3 GET `/challenges/{challengeId}/submissions`

- **权限**：公开
- **描述**：列出指定挑战下的所有提交，按创建时间倒序。
- **查询参数**：`page`（默认 0）、`size`（默认 20，最大 100）
- **响应**：`PageResponse<SubmissionResponse>`

> ⚠️ 由于接口会返回完整代码，请在前端自行决定是否对非作者隐藏 `code` 字段。

## 6. 小组讨论 (Group Chat)

> `ThreadResponse` 字段：`id`、`groupId`、`title`、`description`、`createdAt`、`createdById`、`createdByName`。  
> `MessageResponse` 字段：`id`、`threadId`、`authorId`、`authorName`、`authorRole`、`content`、`codeLanguage`、`createdAt`。

所有讨论接口都需要登录，且后台会验证：调用者是该组导师或已加入的学员，否则返回 `403 You are not part of this group`。

### 6.1 POST `/groups/{groupId}/threads`

- **权限**：`MENTOR` / `MENTEE`
- **描述**：在小组内创建讨论串；可带首条消息。
- **请求体** `CreateThreadRequest`

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `title` | string | ✅ | ≤160 字符 | 线程标题 |
| `description` | string | ❌ | ≤2000 字符 | 线程描述 |
| `initialMessage` | string | ❌ | ≤8000 字符 | 如果提供，将作为首条消息写入 |
| `codeLanguage` | string | ❌ | ≤30 字符 | 首条消息代码语言标注 |

- **响应**：`201 Created`，返回 `ThreadResponse`

### 6.2 GET `/groups/{groupId}/threads`

- **权限**：`MENTOR` / `MENTEE`
- **描述**：分页查看小组中的所有讨论串。
- **查询参数**：`page`（默认 0）、`size`（默认 20，最大 100）
- **响应**：`PageResponse<ThreadResponse>`

### 6.3 POST `/threads/{threadId}/messages`

- **权限**：`MENTOR` / `MENTEE`
- **描述**：在讨论串内发送消息。
- **请求体** `CreateMessageRequest`

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `content` | string | ✅ | ≤8000 字符 | 文本内容 |
| `codeLanguage` | string | ❌ | ≤30 字符 | 代码语言标注（可选） |

- **响应**：`201 Created`，返回 `MessageResponse`

### 6.4 GET `/threads/{threadId}/messages`

- **权限**：`MENTOR` / `MENTEE`
- **描述**：分页读取指定讨论串的消息，按时间正序。
- **查询参数**：`page`（默认 0）、`size`（默认 50，最大 100）
- **响应**：`PageResponse<MessageResponse>`

## 7. 调试建议

1. **注册两个账号**：导师与学员，保存返回的 `token`。
2. **导师**：调用 `/groups/create`、`/groups/{id}/challenges` 发布内容。
3. **学员**：先 `/groups/{id}/join`，再 `/challenges/{id}/submissions`、`/threads/...`。
4. **排查权限**：出现 `403` 时确认 `Authorization` 头是否携带正确的角色 JWT，或是否已经加入相关小组。

通过以上接口即可覆盖 PRD 中的核心 Mentor/Mentee 流程，如需扩展（例如邀请、审核等）可在后续迭代中继续补充。
