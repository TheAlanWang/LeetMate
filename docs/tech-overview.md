# LeetMate 平台技术说明（前后端概要）

## 后端（backend）
- **框架**：Spring Boot 3 + Java 17，REST API，安全基于 JWT（SecurityConfig）。  
- **主要模块与目录职责**  
  - `controller/`：HTTP 接口聚合，如 `AuthController`、`GroupController`，对应认证、组、讨论等业务。  
  - `service/`：业务逻辑与权限校验，如 `GroupService`（组的创建/更新/加入/列表/成员校验）、`AuthService` 等。  
  - `entity/` & `repository/`：JPA 实体与数据访问层，映射组、用户、线程、消息等。  
  - `dto/`：请求/响应模型（如 `CreateGroupRequest`、`GroupResponse`）。  
  - `config/`：安全、CORS、WebClient 等基础配置。  
  - `security/`：JWT 过滤器、UserPrincipal 等鉴权实现。  
  - `resources/db/migration/`：Flyway SQL 迁移脚本（例如 `V1__baseline_schema.sql`、`V2__chat_messages_parent_id.sql`），建表与约束演进，路径为 `backend/src/main/resources/db/migration`。  
  - `exception/`：统一异常转换。  
  - 控制器：`controller/*` 暴露认证、组、挑战、讨论等接口。  
  - 服务层：`service/*` 承载业务逻辑（如 `GroupService` 负责组的创建/更新/成员加入/列出会员）。  
  - 数据层：JPA 实体与仓库，持久化到 PostgreSQL。  
  - 迁移：Flyway 管理表结构和约束。  
  - 安全：`JwtAuthenticationFilter` 负责鉴权，`AccessDeniedException` 等用于权限校验（例如只有导师可编辑组）。  
- **常用接口**  
  - 认证：`/auth/login`、`/auth/register`。  
  - 组：`POST /groups` 创建；`PUT /groups/{id}`（导师/owner）编辑；`POST /groups/{id}/join|leave` 加入/退出；`GET /groups` 列表；`GET /groups/members/{userId}` 获取用户加入的组。  
  - 讨论：`POST /groups/{id}/threads` 创建线程（需组内成员）；`GET /groups/{id}/threads` 列表；`POST /threads/{id}/messages` 发帖/回复（可带 `parentMessageId`）。  

## 前端（frontend）
- **框架**：React + CRA，路由：MyGroups，Find Groups，Group 详情，Create Group，Login。  
- **状态与上下文**  
  - `App.js` 定义 `AuthContext`（token/user），导航、路由、登录状态。  
  - 加入状态：在列表/详情页根据 `/groups/members/{userId}` 标记已加入，按钮显示 Joined 并禁用。  
- **主要页面**  
  - `GroupListPage.js`（Find Groups）：真实标签渲染，支持标签/名称/描述搜索，分类筛选精简，标签去重，已加入显示 Joined。  
  - `MyGroupsPage.js`：仅显示用户加入的组，创建者显示 “Created by me”，硬编码 “Free” 徽标。  
  - `GroupPage.js`：组详情/讨论中心。  
    - 组信息可由导师编辑（标签使用 `TagSelector` 预设 system_design/algorithms/behavior_question，支持自定义）。  
    - 讨论区展开所有线程与消息，支持多层嵌套回复；长内容支持 Show more/less；成员/导师可创建线程、发帖、回复。  
    - 加入/刷新按钮右对齐，返回跳转 `/my-groups`。  
  - `CreateGroupPage.js`：仅 Mentor；创建组并传递标签数组。  
  - 其他：登录页、导航 Group 下拉（My Group / Find Group / Create Group 仅 Mentor）。  
- **组件**  
  - `components/TagSelector.js`：预设标签多选，可新增自定义，最多 5 个。  
  - Markdown 渲染：`GroupPage.js` 内部简单转义 + 代码块高亮。  

## 现有约束与注意
- 组讨论端要求后端允许“组内成员”创建线程与消息，否则前端会提示失败。  
- MyGroupsPage 的 “Free” 徽标是硬编码，不依赖数据。  
- 长消息截断长度 300 字，可按需调整。  
- 组删除功能尚未实现（菜单仍是占位），如需删除需补充后端 API 并接线。  

## 本地运行
- 后端：`cd backend && ./mvnw spring-boot:run`（需 Postgres，或 test profile 用 H2）。  
- 前端：`cd frontend && npm start`，环境变量 `REACT_APP_API_BASE` 指向后端。  

## 清理建议
- 若需继续清理：删除占位操作（如删除组的模板按钮）、移除硬编码徽标、合并重复状态，并确保前后端接口一致。当前暂未大规模重构以免影响现有功能。  
- 保持前后端接口一致（标签字段使用数组），避免混用旧的逗号分隔字符串。  
