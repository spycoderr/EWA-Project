# Implementation progress

- [x] Phase 1 — Setup
- [x] Phase 2 — Database, demo users and session login
- [x] Phase 3 — Employee submission and visibility
- [x] Phase 4 — Manager/Finance workflow
- [x] Phase 5 — Admin threshold and company separation
- [x] Phase 6 — UI polish, focused tests and documentation

Phase entries below record milestone state. Later verification entries resolve earlier pending checks; final status is at the end.

## Phase 1 — Setup

- Created backend Maven/Spring Boot setup, Java 21 compiler target, MySQL environment configuration, Vite React JavaScript frontend and `/api` proxy.
- Added first-run README and `.gitignore`. Workspace was empty; no existing files were removed.
- Environment: Node 24 and JDK 25 available; Maven and MySQL not installed. Preparing temporary tools for verification.
- Run: backend `./mvnw spring-boot:run`; frontend `npm install && npm run dev` (separate terminals; MySQL first).
- Viva: “React screen banata hai; `/api` request Spring Boot tak proxy hoti hai; Java MySQL se data leta hai.”
- Remaining: database entities/login, submissions, approvals, admin settings, tests and final docs.

### Phase 1 verification

- PASS: Maven `compile` (Java 21 release target; host JDK 25), standard Maven 3.9.11 wrapper generated.
- PASS: `npm install`, `npm run build`, Vite serving on 127.0.0.1:5173.
- Initial backend startup failed because no MySQL was running. Downloaded official portable MySQL 8.4.13 to `/private/tmp`; initial sandboxed DB initialization failed with native memory permissions. Retrying outside that process sandbox. No fake database was substituted.

## Phase 2 — Database and session login

- Implemented five entities/repositories, three enums, `DemoDataSeeder`, `AuthService`, `AuthController`, safe DTOs, role helper, and central JSON error handler.
- Added Login page, demo-account picker, session restoration, role navigation and logout.
- PASS: Maven compilation during startup attempt; `npm run build` after login implementation.
- Live login/refresh/logout/idempotency checks pending MySQL availability.
- Run/check: start MySQL then `cd backend && ./mvnw spring-boot:run`; login via Vite with `alpha.employee@demo.com` / `Demo@123`.
- Viva: “Password BCrypt hash hai; browser cookie bhejta hai aur session mein sirf USER_ID hai. Har protected API user ko database se dubara padhti hai.”
- Remaining: employee submissions, workflow actions, admin threshold, final tests/docs.

### Environment resolved

- Official portable MySQL 8.4.13 now runs isolated in `/private/tmp`, bound to 127.0.0.1:3307; database `expense_approval_lab` created.
- Official temporary Corretto JDK 21.0.13 is available for all following Java verification.
- System-wide JDK/MySQL settings were not changed. Default project configuration still uses localhost:3306; verification overrides DB_URL to port 3307.

### Phase 2 verification

- PASS via Vite proxy: all eight demo logins, `/auth/me` session restoration, logout invalidation, anonymous `/me` returns 401, wrong password returns 401; safe user JSON contains no password hash.
- PASS: actual Spring Boot startup and connection to MySQL 8.4.13 on Java 21.
- Phase 1 live proxy check is now PASS as well. Seeder restart persistence will be verified again after rule changes.

## Phase 3 — Employee request submission

- Implemented create/list/detail/history APIs with safe DTOs, Bean Validation, server-derived company/submitter, threshold snapshot, timestamps and transactional SUBMITTED history.
- Added dashboard, new request, request table/local status filter, detail/history pages and INR/date formatting.
- Run/check: `./mvnw spring-boot:run`; Alpha Employee submits ₹3,000 and ₹8,000; both pending Manager, Finance flags false/true.
- Viva: “BigDecimal compareTo se amount threshold ke saath compare hota hai; requiresFinance request par snapshot hai. Employee query company aur user dono se filter hoti hai.”
- Remaining: live Phase 3 checks, reviewer actions, admin rules, final tests/docs.

### Phase 3 verification

- PASS through Vite + real MySQL: ₹3,000=false, ₹8,000=true, exactly ₹5,000=false; all begin PENDING_MANAGER with SUBMITTED history.
- PASS: anonymous create/list 401; Manager create 403; cross-company detail/history 404; zero/negative/over-limit/extra-decimal amounts and blank title 400.
- PASS: spoofed company/user/role/status/requiresFinance JSON fields do not influence server-derived values. Maven wrapper startup works; frontend build passed.

## Phase 4 — Manager and Finance decisions

- Implemented transactional approve/reject, role and stage checks, final-state protection, rejection reasons and action history.
- Added a simple database row lock on the review query to prevent simultaneous decisions on the same step, with no extra tables or workflow abstraction.
- Added stage-specific review controls and rejection confirmation; detail/history refresh after action.
- Run/check: Manager approves low/high; Finance finalizes high; repeat action must return 409.
- Viva: “Role aur status ke simple if checks next status decide karte hain. @Transactional status aur history ko saath commit karta hai.”
- Remaining: live decision checks, admin threshold, tests/docs.

### Phase 4 verification

- PASS via Vite + MySQL: Manager low → APPROVED; high → PENDING_FINANCE; Finance → APPROVED; exact chronological three-event history.
- PASS: Manager and Finance rejection events; blank/null/whitespace rejection reason 400; Employee/Admin action 403; wrong stage and final request 409; cross-company action 404.
- PASS: two simultaneous Manager approval calls produced one success and one 409, with exactly one approval event.
- PASS: frontend build and Java 21 backend restart.

## Phase 5 — Admin threshold and company separation

- Implemented Admin-only GET/PUT `/api/rules`, validated threshold DTO and `RuleService` scoped to session company.
- Added Rule Settings form with INR display, save feedback and explanation of snapshot behavior.
- Run/check: change Alpha ₹5,000 → ₹10,000; submit new ₹8,000; compare old snapshot; verify Beta unchanged and cross-company IDs return 404.
- Viva: “RuleService user ki company ka rule update karta hai. Purane requests ka boolean dobara calculate nahi hota.”
- Remaining: live configuration checks, focused Java tests, final documentation/UI checks.

### Phase 5 verification

- PASS: Alpha threshold ₹5,000 → ₹10,000; new ₹8,000 request Manager-only; old ₹8,000 retains Finance route, confirmed by actual Manager transitions.
- PASS: Beta threshold unchanged; Employee/Manager/Finance rule reads and edits 403; threshold zero accepted, negative/over-limit/extra-decimal values 400.
- PASS: all Alpha roles cannot read Beta detail/history; company request lists remain scoped; cross-company Manager approval/rejection 404; supplied companyId ignored.
- PASS: exactly five MySQL tables, 2 companies, 8 users, 2 rules.

## Phase 6 — Polish, tests and documentation

- Implemented responsive plain-CSS UI with forms, status badges, loading/empty/error/success messages, INR formatting, chronological history and rejection confirmation.
- Added `ExpenseServiceTest`, `RuleServiceTest`, `AuthControllerTest`, `InputValidationTest` (JUnit/Mockito/standalone MockMvc, no database dependency).
- PASS: 44 executed Java test cases, 0 failures, 0 errors, 0 skipped on Java 21 (2026-10-28).
- Explicit VARCHAR mappings added after SQL inspection to match the brief instead of Hibernate's default MySQL native enum DDL.
- Final README includes setup, roles, tables, API, eight accounts, demo walkthrough and limitations. Viva and test matrix are being completed.
- Run/check: `cd backend && ./mvnw test`; `cd frontend && npm run build`; manual demo via Vite.
- Viva: “Mockito repositories ka test substitute deta hai, isliye business rules MySQL ke bina test hote hain. Live MySQL checks actual persistence aur HTTP session flow verify karte hain.”
- Remaining verification: final schema/restart persistence and browser submission/review/admin walkthrough; final docs consistency.


### Final verification — 28 October 2026

- PASS: Java 21 Spring Boot starts and connects to real MySQL; committed Maven wrapper downloads/runs Maven correctly.
- PASS: final `./mvnw test` — 44 tests, 0 failures/errors/skips. Final `npm run build` — Vite 7.3.7 production bundle built.
- PASS: Vite dev server serves the app and proxies session-cookie API calls.
- PASS: restart retained 9 verification expenses, history, saved route flags and changed Alpha threshold; seed counts stayed exactly 2 companies / 8 users / 2 rules.
- PASS: after mapping correction, actual MySQL role/status/action columns are VARCHAR(20/30/40); exactly five main tables remain. Boolean flag remains a boolean-compatible MySQL bit column in the already-created verification schema.
- PASS: all protected endpoint types return 401 without a session; long/Unicode wrong passwords return generic 401.
- PASS: browser login, Employee submission of ₹8,000 (request #10), Manager → Pending Finance, Finance → Approved, updated chronological history and disappearance of review buttons at final state.
- PASS: browser Admin threshold update ₹5,000 → ₹10,000 and restoration to ₹5,000; clear success feedback. Browser session expiry redirected to Login.
- PASS: narrow mobile-width UI inspected visually; approval proof saved in `docs/screenshots/approved-mobile.png`. Desktop CSS is implemented; a separate laptop-width visual inspection was not performed in this run.
- PASS: README, 15-scenario TEST_CASES matrix and 22-question Hinglish VIVA_GUIDE reflect the actual implementation. No placeholder actions or additional frameworks added.
- The demo is still local coursework authentication, as documented; this is not a production security review. Fresh installation on another machine and Windows were not tested.
- Temporary verification runtime: Corretto JDK 21.0.13, Maven 3.9.11, MySQL 8.4.13 on loopback:3307, Vite on 127.0.0.1:5173. System-wide Java/MySQL installations were not changed.
- Alpha threshold is back at ₹5,000 and Beta remains ₹10,000. Explicitly labelled verification requests and one browser viva request remain in the temporary test database; no expense fixtures are seeded by the application.
- GitHub delivery target: `spycoderr/EWA-Project`, requested by the user. Remote was inspected and empty before initial delivery; commit hashes are available in repository history.

### Exact verification commands

Normal setup/run/test commands are in README. This host used temporary paths because JDK 21, Maven and MySQL were initially absent:

```bash
cd backend
JAVA_HOME=/private/tmp/java-lab-jdk21/Contents/Home \
MAVEN_USER_HOME=/private/tmp/java-lab-wrapper \
./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/java-lab-m2 test
```

The backend run used the same environment with `DB_URL=jdbc:mysql://localhost:3307/expense_approval_lab` and goal `spring-boot:run`. Frontend commands actually run were `npm install --cache /private/tmp/java-lab-npm-cache`, `npm run dev` and `npm run build`.
