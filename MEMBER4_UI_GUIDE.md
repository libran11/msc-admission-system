# 第4部分（UI）实现说明

## 1. 我目前完成的内容

### 1.1 前端工程与基础设施
- 新增 `frontend/`（Vue 3 + Vite + TypeScript + Vue Router）。
- 已配置开发代理（`/api` -> `http://localhost:8081`）。
- 新增统一 API 封装与类型定义：
  - `frontend/src/api/client.ts`
  - `frontend/src/api/applications.ts`
  - `frontend/src/api/types.ts`

### 1.2 管理员页面（Admin）
- `frontend/src/views/admin/AdminListView.vue`
  - 查看全部申请（`GET /api/applications`）
  - 按状态筛选（`GET /api/applications/status/{status}`）
- `frontend/src/views/admin/AdminDetailView.vue`
  - 查看单条申请详情（`GET /api/applications/{id}`）
  - 查看审核历史（`GET /api/applications/{id}/reviews`）

### 1.3 审核员页面（Reviewer）
- `frontend/src/views/reviewer/ReviewerListView.vue`
  - 待处理队列（重点展示 `SUBMITTED` / `UNDER_REVIEW`）
- `frontend/src/views/reviewer/ReviewerDetailView.vue`
  - 开始审核：`PUT /api/applications/{id}/start-review`
  - 审核决策：`PUT /api/applications/{id}/review`
    - `APPROVE`
    - `REJECT`
    - `REQUEST_MORE_DOCUMENTS`
  - 状态驱动按钮可用性（避免非法状态操作）

### 1.4 可选演示页面（Applicant）
- `frontend/src/views/applicant/ApplicantCreateView.vue`
  - 创建申请：`POST /api/applications`
- `frontend/src/views/applicant/ApplicantMyView.vue`
  - 查询我的申请：`GET /api/applications/applicant/{applicantId}`
  - 更新材料：`PUT /api/applications/{id}/documents`
  - 提交申请：`PUT /api/applications/{id}/submit`

### 1.5 后端配套调整（为前端联调）
- 新增 `GET /api/applications`（全量列表）：
  - `src/main/java/com/msc/admission/controller/ApplicationController.java`
  - `src/main/java/com/msc/admission/service/ApplicationService.java`
- 新增异常处理器：
  - `src/main/java/com/msc/admission/exception/RestExceptionHandler.java`
  - 将业务非法状态转换为更友好的 400 响应。

### 1.6 本地配置协作方案（多人共用同一仓库）
- 新增 `src/main/resources/application-local.example.yml`
- 本地私有配置 `application-local.yml` 已加入 `.gitignore`，避免把个人数据库密码提交到 GitHub。

---

## 2. 如何使用（本地运行）

## 2.1 环境要求
- JDK 17
- Maven Wrapper（项目已自带）
- Node.js + npm
- MySQL 8.x（或兼容版本）

## 2.2 初始化数据库
1. 启动 MySQL。
2. 执行：

```sql
CREATE DATABASE IF NOT EXISTS msc_admission_system
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

## 2.3 配置本地数据库账号密码
1. 复制：
   - `src/main/resources/application-local.example.yml`
2. 重命名为：
   - `src/main/resources/application-local.yml`
3. 修改其中数据库密码为你本机值（例如你当前是 `123456`）。

## 2.4 启动后端
在项目根目录执行：

```powershell
.\mvnw.cmd spring-boot:run -DskipTests
```

后端默认地址：`http://localhost:8081`

## 2.5 启动前端
在 `frontend/` 目录执行：

```powershell
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

---

## 3. 演示建议流程

1. **Applicant 创建申请**：创建后状态为 `DRAFT`。  
2. **Applicant 提交申请**：状态变 `SUBMITTED`。  
3. **Reviewer 开始审核**：状态变 `UNDER_REVIEW`。  
4. **Reviewer 做出决策**：变为 `ACCEPTED` / `REJECTED` / `NEED_MORE_DOCUMENTS`。  
5. **Admin 查看列表和详情**：核验状态和审核历史。  

---

## 4. 注意事项

- 接口方法请以当前代码为准：`submit/review/start-review/documents` 是 `PUT`。
- 若报 `Unknown database 'msc_admission_system'`：先建库（见 2.2）。
- 若报 `Access denied for user`：检查 `application-local.yml` 中账号密码。
- `application-local.yml` 不要提交到 GitHub（已被 `.gitignore` 忽略）。
- 若依赖下载慢，可使用阿里云 Maven 镜像（你本机已配置在 `C:\Users\张竣凯\.m2\settings.xml`）。

---

## 5. 分支提交与推送（Fork 协作版）

> 本项目协作关系：  
> - 原仓库（upstream）：`git@github.com:libran11/msc-admission-system.git`  
> - 我的 Fork（origin）：`git@github.com:drkai555-gif/msc-admission-system.git`

在项目根目录执行：

```powershell
# 0) 首次检查并修正 remote（只需做一次）
git remote -v
git remote set-url origin git@github.com:drkai555-gif/msc-admission-system.git
git remote set-url upstream git@github.com:libran11/msc-admission-system.git
git remote -v

# 1) 先同步 upstream/main 到本地 main，再同步到自己的 fork
git checkout main
git fetch upstream
git rebase upstream/main
git push origin main

# 2) 基于最新 main 创建功能分支
git checkout -b feat/member4-admin-reviewer-ui

# 3) 检查变更
git status

# 4) 确认本地密码文件不会被提交（应有输出）
git check-ignore -v src/main/resources/application-local.yml

# 5) 提交代码
git add .
git commit -m "feat(ui): implement admin/reviewer frontend, local config workflow and guide"

# 6) 推送到自己的 fork 分支
git push -u origin feat/member4-admin-reviewer-ui
```

### 5.1 创建 Pull Request（PR）

推送后在 GitHub 网页创建 PR，目标应为：

- base repository: `libran11/msc-admission-system`
- base branch: `main`
- head repository: `drkai555-gif/msc-admission-system`
- compare branch: `feat/member4-admin-reviewer-ui`

提交 PR 后，将链接发给组员进行 review。
