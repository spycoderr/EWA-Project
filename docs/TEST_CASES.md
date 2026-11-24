# Test cases and actual results

Latest verification: **24 November 2026 (Asia/Kolkata)** using Java 21.0.13.1, Spring Boot 3.5.16, MySQL 8.4.13 and Node 24.22.0. Current local demo tools/data are in Git-ignored `.tools/`; project defaults still target localhost MySQL port 3306. The original scenario matrix also retains detailed checks from 28 October; the fresh rehearsal results are listed below.

## Automated tests

From `backend/`, run `./mvnw test` (or `mvn test`). No database is needed for these focused tests.

| Test class | Executed cases | Actual result |
| --- | ---: | --- |
| `ExpenseServiceTest` | 26 | PASS |
| `RuleServiceTest` | 4 | PASS |
| `AuthControllerTest` | 4 | PASS |
| `InputValidationTest` | 12 | PASS |
| `ApiValidationTest` | 6 | PASS |
| **Total** | **52** | **0 failures, 0 errors, 0 skipped** |

Parameterized inputs count as separate executed cases. Auth tests use standalone MockMvc with real BCrypt and mocked persistence; service tests use Mockito repositories. These are not full database integration tests. Persistence, schema and row-lock behavior were separately checked against the running MySQL app.

## Required scenario matrix

| Scenario | Login/User | Steps | Expected Result | Actual/Status |
| --- | --- | --- | --- | --- |
| 1. Correct and incorrect login | All eight demo users | POST login; wrong password/unknown email | Safe user JSON on success; generic 401 on invalid credentials | PASS — all eight through live proxy; generic unknown-email response also in AuthControllerTest |
| 2. Unauthenticated create/list | No session | GET list; POST valid create body | 401; no expense saved | PASS — live HTTP + AuthControllerTest |
| 3. Employee cannot review/read another employee | Alpha Employee; second Employee fixture | POST approve/reject; GET another employee detail/history | Review 403; invisible detail/history 404 | PASS — review live HTTP; second same-company employee tested with Mockito fixture, without adding another seeded account |
| 4. ₹3,000 below ₹5,000 | Alpha Employee | Submit ₹3,000 | PENDING_MANAGER, requiresFinance=false, SUBMITTED history | PASS — live MySQL + parameterized service test |
| 5. ₹8,000 above ₹5,000 | Alpha Employee | Submit ₹8,000 | PENDING_MANAGER, requiresFinance=true | PASS — live MySQL + service test |
| 6. Manager approves low request | Alpha Manager | Approve ₹3,000 | APPROVED; MANAGER_APPROVED history with actor | PASS — live HTTP + service test |
| 7. Manager approves high request | Alpha Manager | Approve ₹8,000 | PENDING_FINANCE; not final | PASS — live HTTP + service test |
| 8. Finance approves high request | Alpha Finance | Approve at Finance stage | APPROVED; three chronological events | PASS — live HTTP + service test |
| 9. Correct reviewer rejects | Alpha Manager/Finance | Reject at matching stage with comment | REJECTED; correct action/actor/reason | PASS — both roles live HTTP + service tests |
| 10. Missing rejection reason | Alpha Manager | Reject with null, empty, whitespace comment | 400; no status/history change | PASS — live HTTP + parameterized service test |
| 11. Wrong role/stage/final status | Employee/Admin/Manager/Finance | Review at wrong role/stage; repeat finalized action | 403 role; 409 stage/final; no extra history | PASS — live HTTP + service tests; approved and rejected terminal cases tested |
| 12. Admin threshold snapshot | Alpha Admin then Employee/Manager | Change 5000→10000; compare old/new ₹8,000; approve both | Old Finance route persists; new Manager-only route | PASS — live MySQL through actual transitions + snapshot service test |
| 13. Company separation | All Alpha roles; Beta fixtures | Read Beta ID/history; Manager approve/reject Beta; update rule with spoofed companyId | Invisible requests 404; own lists/rule only; Beta unchanged | PASS — live HTTP for all role reads, Manager actions and Admin rule scoping; Java tests also cover query scope |
| 14. Invalid submissions | Alpha Employee | Amount 0, -1, 1000001, 0.001; blank title | 400; no invalid expense | PASS — live HTTP + DTO validation tests; length/null/boundary cases also automated |
| 15. Restart/idempotency | Server restart; log in again | Preserve changed Alpha threshold, 9 fixture expenses and history; inspect seed counts | 2 companies, 8 users, 2 rules; saved requests/rules retained | PASS — actual backend restart; Alpha stayed 10000, 9 requests retained, snapshots and three-event history intact |

## Additional checks

| Scenario | Steps | Expected Result | Actual/Status |
| --- | --- | --- | --- |
| Equality boundary | Submit exactly ₹5,000 with threshold ₹5,000 | Manager-only | PASS — live HTTP and automated test |
| Client spoofing | Create with extra company/user/role/status/requiresFinance keys | Server-derived values take precedence | PASS — live HTTP, saved submitter Alpha Employee and PENDING_MANAGER |
| Threshold bounds | Save 0; reject negative, >1000000 and extra decimals | Zero valid; invalid values 400 | PASS — live HTTP + validation tests |
| Duplicate simultaneous review | Send two concurrent approvals for same Manager step | One success, one 409; one approval history row | PASS — live MySQL row-lock check |
| Frontend build | `npm run build` | Vite generates production bundle | PASS — repeated after feature phases; final result recorded in PROGRESS.md |
| SQL schema | Inspect tables and enum columns | Five tables; VARCHAR role/status/action; DECIMAL money | PASS — real MySQL inspection after final mapping correction |

## Manual UI demonstration

Use the five-minute walkthrough in README/VIVA_GUIDE. Check Login → Employee creation → Requests filter → detail/history → Manager approval → Finance approval → Admin threshold update → Beta isolation. The server was also tested over HTTP for every business transition, so the matrix above does not imply that all those cases were individually clicked in the browser. Browser submission → Manager → Finance approval, chronological history, Admin save/reset, logout and expired-session redirect were verified; the UI was visually inspected at narrow mobile width. Final findings and unverified aspects are recorded in PROGRESS.md.

Before repeating the default demo, set Alpha's rule back to ₹5,000 through Admin Rule Settings. Existing verification expenses remain intentionally labelled “Verification”; none are part of DemoDataSeeder. No test reset/delete endpoint is included. For an empty demo database, create a separate schema and point DB_URL at it rather than deleting existing records.

## Verification commands used in this workspace

The host initially lacked Maven/MySQL/JDK 21, so official portable tools were placed in `/private/tmp`. The same committed Maven wrapper and source were used:

```bash
cd backend
JAVA_HOME=/private/tmp/java-lab-jdk21/Contents/Home \
MAVEN_USER_HOME=/private/tmp/java-lab-wrapper \
./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/java-lab-m2 test
```

For the real running backend, the same environment plus `DB_URL=jdbc:mysql://localhost:3307/expense_approval_lab` was used with `spring-boot:run`. Normal users should follow README and use their own locally installed MySQL/JDK; temporary tool paths are verification details, not project requirements.


## Rehearsal on 24 November 2026

- PASS: 52 focused Java tests, including two list/history order-and-content cases and six readable HTTP validation error cases.
- PASS: frontend `npm run build`; no dependency or database schema changes.
- PASS: real MySQL through Vite — all eight login/me/logout flows, low/high approvals, Manager and Finance rejection, blank rejection reason, role/final-stage/company guards, invalid title/amount/threshold messages, action history, and unchanged old/new approval route snapshots.
- PASS: seven explicitly labelled rehearsal requests retained after stopping both DB/backend and starting with `bash scripts/start-backend.sh`; counts stayed 2 companies, 8 users, 2 rules. Alpha threshold restored to 5000; Beta remained 10000.
- PASS: laptop layout inspected at 1366×900 for Login and approved request detail/history; invalid login error and successful sign-in checked in browser. Temporary viewport override was reset. Proof image: `docs/screenshots/approved-desktop.png`.
- The 28 October concurrency check was not rerun on 24 November; its row-lock implementation is unchanged. Windows was not tested. No project report was created.

Current workspace commands:

```bash
bash scripts/start-backend.sh
# another terminal:
cd frontend
npm run dev
```

Automated test command with the prepared local tools:

```bash
cd backend
JAVA_HOME="$PWD/../.tools/jdk21/Contents/Home" \
MAVEN_USER_HOME="$PWD/../.tools/maven" \
./mvnw -B -ntp "-Dmaven.repo.local=$PWD/../.tools/m2" test
```
