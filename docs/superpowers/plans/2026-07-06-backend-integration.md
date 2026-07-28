# Backend Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate `/Users/jomatheus/Downloads/backend.zip`, replace frontend runtime mock data with backend-backed services, resolve implementation gaps, and verify backend/frontend quality gates.

**Architecture:** Treat the zip backend as the real API source of truth and sync it into `backend/` while excluding build artifacts and IDE state. Keep frontend data access behind `src/services/*`, mapping backend `ResponseDTO<T>` payloads to existing screen domain types to minimize UI churn. Backend routes use `/auth`, `/children`, `/symptoms`, and `/assessments`.

**Tech Stack:** Spring Boot 3/Java 21/PostgreSQL/Flyway/MapStruct/QueryDSL backend; Expo SDK 54/React Native/TypeScript frontend; Axios currently present, but Expo networking guidance prefers `fetch`, so new request logic should avoid increasing axios coupling where practical.

## Global Constraints

- Do not hardcode secrets; API URL must come from `EXPO_PUBLIC_API_URL` with local fallback only.
- Do not commit or preserve `target/`, `.idea/`, `.m2/`, generated class files, or local env files from the zip.
- Preserve user work; the worktree was clean before integration.
- Backend auth currently expects CPF/CNPJ in `login`, not email.
- Backend API responses are wrapped as `{ data, errors, links }`.
- All backend-protected routes require Bearer JWT except explicitly public auth/docs/health endpoints.
- Tests/checks must be run after each major integration step.

---

### Task 1: Backend Zip Integration

**Files:**
- Modify/create/delete under `backend/` from `/tmp/pji-backend-zip`, excluding `target`, `.idea`, `.m2`.
- Preserve project-level files outside `backend/`.

**Interfaces:**
- Produces: Spring backend controllers for `/auth/login`, `/auth/register/user`, `/children/*`, `/symptoms`, `/assessments/*`.
- Consumes: PostgreSQL via Flyway migrations V1-V5.

- [ ] **Step 1: Run baseline backend build/test command**

Run: `./mvnw test` from `backend/`.
Expected: establishes current failure/pass baseline before replacement.

- [ ] **Step 2: Sync zip source into backend**

Run mechanical copy excluding generated/IDE files: `rsync -a --delete --exclude target --exclude .idea --exclude .m2 /tmp/pji-backend-zip/ backend/`.
Expected: backend tree matches zip source without build artifacts.

- [ ] **Step 3: Run backend tests**

Run: `./mvnw test` from `backend/`.
Expected: pass or expose concrete compile/test failures to fix.

- [ ] **Step 4: Fix root-cause backend failures**

Use systematic debugging. Typical likely areas: Maven annotation processor setup, security route mismatch, Java version/library compatibility, or tests expecting a DB.

### Task 2: Frontend API Contract Mapping

**Files:**
- Modify: `frontend/src/services/api.ts`
- Modify: `frontend/src/services/authService.ts`
- Modify: `frontend/src/services/pediatricService.ts`
- Modify: `frontend/src/types/auth.ts`
- Modify: `frontend/src/types/domain.ts`

**Interfaces:**
- Consumes backend `ResponseDTO<T>`.
- Produces screen-friendly `AuthSession`, `ChildProfile`, `Symptom`, `TriageQuestion`, `TriageResult`, `HistoryItem`, `OrientationCardItem`.

- [ ] **Step 1: Add response unwrapping and typed API helpers**

Expose a helper that unwraps `response.data.data` and throws readable backend errors from `errors[]`.

- [ ] **Step 2: Update auth payloads**

Map frontend `LoginCredentials.login`/`password` to `/auth/login`, and `RegisterPayload.login`/`email`/`name`/`password` to `/auth/register/user`. Store `accessToken` as session token. Since backend login response has no user payload, derive a local user from submitted login/name/email and persist it.

- [ ] **Step 3: Update domain mappings**

Map backend classifications `LOW/MOD/HIGH` to frontend `low/mod/high`; map backend question types `OPTIONS/YESNO`; map options by `optionId` and yes/no answers by `YES/NO/DUNNO`.

### Task 3: Frontend Screen Flow Integration

**Files:**
- Modify: `frontend/src/navigation/RootNavigator.tsx`
- Modify: `frontend/src/screens/LoginScreen.tsx`
- Modify: `frontend/src/screens/RegisterScreen.tsx`
- Modify: `frontend/src/screens/ChildFormScreen.tsx`
- Modify as needed: quiz/result/orientations/history screens.

**Interfaces:**
- Consumes service methods from Task 2.
- Produces user flows backed by API: register/login, list/create child, list symptoms, fetch questionnaire, create assessment, show result/history/orientations.

- [ ] **Step 1: Replace local child creation with `POST /children/{userId}`**

Convert age input to birth date or adjust form to collect birth date, weight, optional CPF/avatar.

- [ ] **Step 2: Fetch questionnaire for selected symptom**

Call `POST /assessments/questionnaire` with selected symptom id and use returned questions for the quiz.

- [ ] **Step 3: Submit answers to backend**

Call `POST /assessments` with `childId` and backend-shaped answers, then map `AssessmentResultResponse` to `TriageResult`.

- [ ] **Step 4: Use backend history/orientation**

Use `/assessments/users/{userId}/history` for history and `/assessments/{assessmentId}/orientation` after a result is available.

### Task 4: Verification and Review

**Files:**
- Test/config files as needed based on discovered project setup.

**Interfaces:**
- Produces verified project state and final report.

- [ ] **Step 1: Run backend tests/build**

Run: `./mvnw test` and, if successful, `./mvnw package -DskipTests` from `backend/`.

- [ ] **Step 2: Run frontend checks**

Run: `npm run typecheck` and `npm run doctor` from `frontend/`.

- [ ] **Step 3: Run security/diff checks**

Search for secrets, inspect `git diff --stat`, and review changed files for auth/input/API risks.

- [ ] **Step 4: Final subagent review**

Dispatch reviewer for correctness/security/missing tests, address blocking findings, then close subagents.
