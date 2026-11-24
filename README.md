# Config-Driven Approval & Workflow Engine for Expense Management

Employees submit expense reimbursement requests and follow their status.
A Manager reviews every request; Finance reviews amounts above a saved company threshold.
An Admin changes that threshold for future submissions, while existing requests keep their original route.
An action history records who submitted, approved or rejected each request and when.
Company Alpha and Company Beta share one MySQL database with company-wise access checks.

## Scope and roles

This is a **local educational Java lab demo**. It covers expense approvals only, with one seeded user per role per company.

| Role | Allowed actions within own company |
| --- | --- |
| EMPLOYEE | Submit an expense; view own requests and history |
| MANAGER | View company requests; approve/reject at Pending Manager |
| FINANCE | View company requests; approve/reject at Pending Finance |
| ADMIN | View company requests/history; read/update company threshold |

With Alpha's initial ₹5,000 threshold:

```text
₹3,000 → Pending Manager → Approved
₹8,000 → Pending Manager → Pending Finance → Approved
                          ↘ correct reviewer can reject → Rejected
```

Exactly equal to the threshold needs Manager only. Approved and Rejected are final. Rejection needs a reason; approval comments are optional. An Admin changing Alpha to ₹10,000 makes a **new** ₹8,000 request Manager-only; an existing ₹8,000 request keeps its Finance route. Beta starts at ₹10,000 independently.

## Stack and architecture

Java 21, Spring Boot 3.5.16, Maven wrapper 3.9.11, Spring Web, Spring Data JPA, Bean Validation, MySQL Connector/J, BCrypt crypto and Spring Boot tests. The frontend uses React JavaScript, React Router, Vite, ordinary CSS and `fetch`. There is no Lombok or full Spring Security configuration.

```text
React page → fetch /api/... → @RestController → @Service → JpaRepository → MySQL
             Vite proxy       HTTP/DTOs        rules       persistence
```

`ExpenseService` contains the approval logic using ordinary `if` statements. `BigDecimal.compareTo()` compares money without binary floating-point rounding. `LocalDateTime` records server-local time consistently; the UI formats that same local time. This localhost project assumes the browser and server use the same timezone; it does not handle international timezone conversion.

The review query holds one database row lock until the transaction finishes. Two simultaneous clicks cannot both approve the same step. No additional concurrency framework or workflow framework is used.

## Java concepts used in the project

| Concept | Actual example |
| --- | --- |
| Classes and encapsulation | `ExpenseRequest` has private fields and a controlled `updateStatus()` method |
| Interfaces | The five repository interfaces extend `JpaRepository`; Spring Data supplies implementations |
| Enums and decision making | `Role`, `RequestStatus`, `ApprovalActionType`, and the `if/else` decisions in `ExpenseService.review()` |
| Collections | `List` variables hold expenses/history; `ArrayList` and enhanced `for` loops build safe response lists |
| Exception handling | `ApiException`, `ApiExceptionHandler`, and Bean Validation return readable errors |
| Database connectivity | JPA/Hibernate uses the MySQL JDBC driver; relationships map to five SQL tables |

In `ExpenseService.listVisibleRequests()` and `getHistory()`, `List` is the collection interface and `ArrayList` is the concrete implementation. The loops convert entity objects to DTOs while keeping the query's display order. No extra collection types or interfaces are added just to demonstrate a syllabus topic.

## Project layout

```text
backend/
  pom.xml, mvnw, mvnw.cmd, .mvn/wrapper/
  src/main/java/com/example/expenseapproval/
    ExpenseApprovalApplication.java
    controller/  service/  repository/  model/  dto/  exception/  config/
  src/main/resources/application.properties, application-example.properties
  src/test/java/com/example/expenseapproval/
frontend/
  package.json, package-lock.json, vite.config.js, index.html
  src/App.jsx, api.js, format.js, main.jsx, styles.css
  src/components/  src/pages/
docs/
  PROGRESS.md  TEST_CASES.md  VIVA_GUIDE.md
```

## Database: exactly five tables

| Table | Purpose |
| --- | --- |
| `companies` | Company name and ID |
| `app_users` | Company, name, unique email, BCrypt password hash and role |
| `approval_rules` | One unique company rule containing `finance_threshold` |
| `expense_requests` | Employee/company, title, description, amount, status, saved `requires_finance`, timestamps |
| `approval_actions` | Request, actor, action enum name, comment and timestamp |

Relationships are one-way JPA references: user → company; rule → company; expense → company and submitter; action → expense and actor. There are no bidirectional collections. The API returns DTO records, never entities or password hashes. Monetary columns are `DECIMAL(12,2)`; enum names use `VARCHAR`. Foreign keys, uniqueness, not-null and amount/threshold check constraints support the Java checks.

## First run

Prerequisites: **JDK 21**, Node.js LTS **22.12+** (Node 24 also works), npm and a locally installed/running **MySQL Server 8.4+**. Maven need not be installed: the committed standard wrapper downloads Maven on first use. Internet is needed for the first Maven/npm dependency downloads. Docker is not required.

Check Java and Node:

```bash
java -version
node --version
```

If multiple JDKs are installed, select JDK 21 using your OS's `JAVA_HOME` setting. On macOS with an installed JDK 21:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

Start MySQL using its installer/service, then connect to it:

```bash
mysql -u root -p
```

Run this SQL:

```sql
CREATE DATABASE IF NOT EXISTS expense_approval_lab;
```

Terminal 1, from this project folder:

```bash
cd backend
export DB_USERNAME=root
export DB_PASSWORD='YOUR_LOCAL_MYSQL_PASSWORD'
./mvnw spring-boot:run
# If you prefer an installed Maven: mvn spring-boot:run
```

Terminal 2, from this project folder:

```bash
cd frontend
npm install
npm run dev
```

Open Vite's printed local URL, usually [http://localhost:5173](http://localhost:5173). Sign in with one of the accounts below. The backend listens on loopback port 8080. Vite proxies `/api` to `http://localhost:8080`, so cookies stay same-origin without custom CORS. Use the frontend URL when demonstrating the app.

Default configuration:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/expense_approval_lab}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

If MySQL uses a different port or database, set `DB_URL`, e.g. `export DB_URL='jdbc:mysql://localhost:3307/expense_approval_lab'`. Environment variables are read by Spring; `.env` files are **not loaded automatically**. Do not commit a personal database password. `application-example.properties` is a reference, not an additional hidden setup step.

On Windows use `mvnw.cmd spring-boot:run` and set environment variables in your terminal (PowerShell: `$env:DB_PASSWORD='YOUR_LOCAL_MYSQL_PASSWORD'`).

### Starting again in this Mac workspace

For the submission rehearsal, optional official JDK 21 and MySQL binaries/data were restored under the Git-ignored `.tools/` folder instead of `/private/tmp`. On this already-prepared Mac, run `bash scripts/start-backend.sh` from the project root, then `cd frontend && npm run dev` in another terminal. The helper uses local MySQL on loopback port 3307 and preserves existing records. It does not download/install tools or reset data.

The `.tools/` binaries and database are not uploaded to GitHub. On another machine, follow the normal prerequisites and database setup above; the helper can also use an explicitly configured `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`.

If startup reports a communications-link/dialect error, check that MySQL is running, the database exists and credentials/port are correct. If Vite cannot reach `/api`, check the backend startup log and port 8080. Stop each server with Ctrl+C.

## Demo accounts

All passwords are **`Demo@123`**. These are fake localhost demo accounts, created only if absent.

| Company | Role | Email |
| --- | --- | --- |
| Alpha | EMPLOYEE | `alpha.employee@demo.com` |
| Alpha | MANAGER | `alpha.manager@demo.com` |
| Alpha | FINANCE | `alpha.finance@demo.com` |
| Alpha | ADMIN | `alpha.admin@demo.com` |
| Beta | EMPLOYEE | `beta.employee@demo.com` |
| Beta | MANAGER | `beta.manager@demo.com` |
| Beta | FINANCE | `beta.finance@demo.com` |
| Beta | ADMIN | `beta.admin@demo.com` |

`DemoDataSeeder` creates 2 companies, 8 users and 2 rules without overwriting existing records. Restarting preserves thresholds, requests and history. There is no signup, account management or seeded expense data.

## API overview

All bodies and responses are JSON. Protected endpoints derive the current user from `HttpSession.USER_ID`; the browser cannot choose company, actor, status, route or timestamp. Role/company checks run in Java even when buttons are hidden in React.

| Method | Path | Access |
| --- | --- | --- |
| POST | `/api/auth/login` | Public; `{email,password}` → safe user info |
| GET | `/api/auth/me` | Logged in → `{id,name,email,role,companyName}` |
| POST | `/api/auth/logout` | Logged in; invalidates session, returns 204 |
| GET | `/api/requests` | Employee's own list; other roles' company list, newest first |
| POST | `/api/requests` | Employee; `{title,description,amount}` → 201 |
| GET | `/api/requests/{id}` | Visible request detail |
| GET | `/api/requests/{id}/history` | Visible request's actions, oldest first |
| POST | `/api/requests/{id}/approve` | Manager/Finance at their stage; optional `{comment}` |
| POST | `/api/requests/{id}/reject` | Manager/Finance at their stage; required `{comment}` |
| GET | `/api/rules` | Admin's own company rule |
| PUT | `/api/rules` | Admin; `{financeThreshold}` |

Expense amounts: ₹0.01–₹10,00,000; thresholds: ₹0–₹10,00,000; both at most two decimal places. Title: required, up to 120 characters. Description and review comment: up to 500 characters. No request edit/delete endpoints exist.

Errors use `{ "message": "..." }`: 400 input validation, 401 missing/expired login or invalid credentials, 403 disallowed role, 404 invisible/missing request, 409 wrong workflow stage/finalized request, 500 generic unexpected failure. Cross-company and another employee's detail/history both return 404.

## Five-minute demo

1. **0:00–1:00:** Alpha Employee submits ₹3,000 stationery and ₹8,000 travel. Both show Pending Manager; detail shows different saved approval routes.
2. **1:00–2:00:** Sign out and sign in as Alpha Manager. Approve both. ₹3,000 is Approved, ₹8,000 is Pending Finance.
3. **2:00–3:00:** Alpha Finance approves ₹8,000. Its history now shows Employee, Manager and Finance with timestamps/comments.
4. **3:00–4:00:** Alpha Admin changes ₹5,000 to ₹10,000. Alpha Employee submits a NEW ₹8,000: Manager-only. Existing travel still shows Manager → Finance.
5. **4:00–5:00:** Beta Employee sees only Beta's list; Beta Admin sees unchanged ₹10,000 threshold. Explain company filtering and final-state protection.

Before repeating the original demo, use Alpha Admin's Rule Settings to set ₹5,000 again; this affects future requests only. For a genuinely fresh demonstration use a new empty database through `DB_URL`; the application does not erase existing data.

## Verification

```bash
cd backend
./mvnw test
# or: mvn test
```

```bash
cd frontend
npm run build
```

JUnit/Mockito tests do not require MySQL. They cover money validation, routes, status transitions, authorization, rule snapshots and session API behavior. Real MySQL + Vite HTTP checks additionally exercised seeding, persistence, company boundaries and simultaneous duplicate decisions. A narrow-width browser walkthrough also verified submission, both approval stages and Admin settings. See [test cases](docs/TEST_CASES.md) and [phase results](docs/PROGRESS.md) for actual outcomes and limits. Read the [Hinglish viva guide](docs/VIVA_GUIDE.md) before presenting.

## Known limitations

This is a controlled local coursework application, **not production authentication**. Login uses a cookie-backed server session with HTTP-only/SameSite=Lax cookies and a 30-minute inactivity timeout. It uses BCrypt hashing but intentionally omits a complete Spring Security/CSRF setup. A deployed product would need full Spring Security, CSRF protection, HTTPS/secure cookies and additional hardening. Do not expose this demo publicly.

Company separation uses simple `company_id` queries in one database; it is not advanced tenant isolation. `ddl-auto=update` is a local development convenience, not a production migration strategy. There is no public registration, receipt upload, procurement, payment, export, notification, chart or dynamic workflow designer. Lists load the small demo dataset without server-side pagination.

## Study these files first

1. `model/Role.java`, `RequestStatus.java` and `ExpenseRequest.java`: Java fields, enums and table relationships.
2. `service/ExpenseService.java`: creation, threshold comparison, visibility and approve/reject logic.
3. `controller/ExpenseController.java` and `dto/CreateExpenseRequestDto.java`: REST mapping and validation.
4. `repository/ExpenseRequestRepository.java`: company/user queries and the review row lock.
5. `service/AuthService.java`, `controller/AuthController.java` and `config/DemoDataSeeder.java`: BCrypt, session login and idempotent seed data.
6. `service/RuleService.java` and the focused tests: saved configuration and boundary cases.

Java paths above are relative to `backend/src/main/java/com/example/expenseapproval/`.
