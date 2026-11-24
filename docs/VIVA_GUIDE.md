# Viva guide — simple Hinglish

## 60-second introduction

“Sir, mera project Config-Driven Approval & Workflow Engine for Expense Management hai. Employee expense submit karta hai, Manager review karta hai, aur company ke configured threshold se amount zyada ho toh Finance ka second approval chahiye. React forms aur screens banata hai, Java Spring Boot business rules aur authorization enforce karta hai, aur MySQL data save karta hai. Admin threshold change kar sakta hai, lekin existing request ka route change nahi hota, kyunki `requiresFinance` submission ke waqt save hota hai. Har submission, approval aur rejection ka actor aur time history mein record hota hai. Alpha aur Beta ka data same database mein company ID filters se separate rehta hai. Ye local lab demo hai, production authentication system nahi.”

## Source map

Java paths below are relative to `backend/src/main/java/com/example/expenseapproval/`. Frontend paths start from the repository root. Java tests are in `backend/src/test/java/com/example/expenseapproval/`.

## 1. Project kaunsa problem solve karta hai?

Manual expense tracking mein current status aur kisne decision liya unclear ho sakta hai. Hamara application request ka status aur chronological history ek jagah dikhata hai. Expensive requests ko automatically additional Finance review milta hai.

**Code:** `service/ExpenseService.java`: `createRequest()`, `approve()`, `getHistory()`.

**Sir ka follow-up:** “Kya payment bhi hota hai?” — Nahi, scope reimbursement request approval tak hai; payment integration nahi hai.

## 2. React + Spring Boot kyun, JavaFX kyun nahi?

Project requirement web application ki hai, aur React mujhe pehle se familiar hai. Spring Boot use karke Java mein APIs, database handling aur business rules seekh raha hoon. Professor ne ye frontend/backend combination allow kiya hai; JavaFX desktop UI yahan required nahi hai.

**Code:** `frontend/src/App.jsx`, `ExpenseApprovalApplication.java`, `backend/pom.xml`.

**Sir ka follow-up:** “Java ka actual kaam kya hai?” — Java login, role/company authorization, threshold comparison, workflow aur persistence handle karta hai.

## 3. Frontend aur backend communicate kaise karte hain?

React frontend hai aur Spring Boot backend hai. `api()` browser se `/api/...` par JSON HTTP request bhejta hai; Vite proxy usse localhost:8080 tak forward karta hai. Server JSON DTO response bhejta hai, jise React display karta hai.

**Code:** `frontend/src/api.js`: `api()`; `frontend/vite.config.js`; `controller/ExpenseController.java`.

**Sir ka follow-up:** “Proxy kyun?” — Development mein browser same frontend origin ko call karta hai, isliye session cookies aur API calls simple rehte hain.

## 4. Spring Boot aur core Java mein difference kya hai?

Core Java language aur standard library deta hai: classes, enums, collections, exceptions aur `BigDecimal` jaise types. Spring Boot Java web application start/configure karne mein help karta hai, including embedded Tomcat aur dependency configuration. Hamara business logic phir bhi ordinary Java code hi hai.

**Code:** `ExpenseApprovalApplication.java`: `main()`; `service/ExpenseService.java`; `backend/pom.xml`.

**Sir ka follow-up:** “Server alag se install karna padega?” — Tomcat Spring Boot web starter ke through embedded aata hai; MySQL separately running hona chahiye.

## 5. Employee submit kare toh step by step kya hota hai?

`NewRequestPage.submit()` title, amount aur description POST karta hai. `ExpenseController.create()` input validate karke session user leta hai aur `ExpenseService.createRequest()` ko deta hai. Service Employee role check karke company rule padhti hai, amount compare karti hai aur PENDING_MANAGER request banati hai. Request aur SUBMITTED action transaction mein save hote hain, phir safe DTO response milta hai.

**Code:** `frontend/src/pages/NewRequestPage.jsx`: `submit()`; `controller/ExpenseController.java`: `create()`; `service/ExpenseService.java`: `createRequest()`.

**Sir ka follow-up:** “Browser apna status APPROVED bheje toh?” — Create DTO mein status field nahi hai; server PENDING_MANAGER khud set karta hai.

## 6. REST API, controller, service aur repository kya hain?

REST API HTTP routes se resources par operations expose karti hai, jaise GET request list aur POST new request. Controller HTTP input/output handle karta hai; service role checks aur business decisions karti hai. Repository interface persistence queries provide karta hai. Ye separation code ko padhna aur test karna simple banata hai.

**Code:** `controller/ExpenseController.java`, `service/ExpenseService.java`, `repository/ExpenseRequestRepository.java`.

**Sir ka follow-up:** “Saara logic controller mein kyun nahi?” — HTTP handling aur business rules mix ho jayenge; service ko independently test karna useful hai.

## 7. Important annotations ka matlab kya hai?

`@RestController` class ko JSON HTTP endpoints ke liye mark karta hai aur `@Service` business class ko Spring-managed object banata hai. `@Entity` Java object ko database table se map karta hai. `@Repository` persistence stereotype hai; hamare interfaces `JpaRepository` extend karte hain aur Spring Data unka implementation automatically register karta hai, isliye explicit `@Repository` nahi likha. `@Transactional` method ke database operations ko ek unit mein commit ya rollback karta hai.

**Code:** `controller/ExpenseController.java`, `service/ExpenseService.java`, `model/ExpenseRequest.java`, `repository/ExpenseRequestRepository.java`.

**Sir ka follow-up:** “`new ExpenseService(...)` controller mein kyun nahi?” — Spring constructor injection se required service object deta hai; tests mein hum constructor se mocks de sakte hain.

## 8. Spring Data JPA, JDBC aur SQL ka relation kya hai?

JPA Java objects aur relational tables ki mapping ka standard hai; Hibernate yahan uska implementation hai. Spring Data JPA `JpaRepository` se common queries ka code reduce karta hai. Hibernate database operations ke liye SQL generate karta hai, aur MySQL JDBC driver database connection/communication handle karta hai. JPA use karne ka matlab SQL aur JDBC concepts disappear hona nahi hai.

**Code:** `repository/ExpenseRequestRepository.java`, `model/ExpenseRequest.java`, `backend/pom.xml`.

**Sir ka follow-up:** “Kya humne custom SQL likhi?” — Review method mein JPQL likhi hai, jo Java entity/field names use karti hai; Hibernate usse SQL banata hai.

## 9. `@ManyToOne` kya hai? Relationships kaunsi hain?

ManyToOne ka matlab many rows ek related row ko refer kar sakti hain, jaise multiple users ek company ke ho sakte hain. ExpenseRequest company aur submitter ko refer karta hai; ApprovalAction expense aur actor ko refer karta hai. ApprovalRule ka Company ke saath OneToOne relation hai, kyunki har company ka ek rule hai. Relationships one-way rakhe hain, isliye unnecessary bidirectional collections nahi hain.

**Code:** `model/AppUser.java`, `ApprovalRule.java`, `ExpenseRequest.java`, `ApprovalAction.java`.

**Sir ka follow-up:** “Foreign key kahan hai?” — `@JoinColumn(name = "company_id")` jaisi mapping corresponding database FK column banati hai.

## 10. Kaunse OOP concepts naturally use hue?

Encapsulation private entity fields aur getters mein dikh rahi hai; status update `ExpenseRequest.updateStatus()` se hota hai. Repository interfaces persistence operations ka abstraction provide karte hain, aur services constructor injection se un interfaces par depend karti hain. Relationships object references se association show karti hain. `ApiException extends RuntimeException` actual inheritance example hai; hamne unnecessary inheritance tree ya formal State/Strategy class hierarchy nahi banayi.

**Code:** `model/ExpenseRequest.java`: `updateStatus()`; `repository/ExpenseRequestRepository.java`; `exception/ApiException.java`.

**Sir ka follow-up:** “Polymorphism ka actual example?” — Spring Data ka runtime-generated repository implementation interface ke through use hota hai; har status ki separate subclass yahan nahi hai.

## 11. Money ke liye BigDecimal kyun?

`double` binary floating-point hai, isliye decimal money values mein rounding surprise aa sakta hai. `BigDecimal` decimal values represent karta hai aur MySQL mein amount DECIMAL(12,2) hai. Threshold comparison `compareTo()` se hota hai; `equals()` scale bhi compare karta hai, isliye numeric threshold check ke liye suitable nahi hai.

**Code:** `model/ExpenseRequest.java`: `amount`; `service/ExpenseService.java`: `createRequest()`.

**Sir ka follow-up:** “5000.0 aur 5000.00 compareTo?” — Numeric value equal hai, isliye result 0 hoga aur Finance required nahi hoga.

## 12. Enum kya hai? Status kaise handle hota hai?

Enum fixed allowed values ka Java type hai, jaise RequestStatus ke chaar states. Role aur ApprovalActionType bhi enums hain, isliye spelling-based arbitrary strings business logic mein avoid hoti hain. `@Enumerated(EnumType.STRING)` names save karta hai; explicit column definition unhe VARCHAR mein rakhti hai. Service current status aur role dekhkar valid next status choose karti hai.

**Code:** `model/Role.java`, `RequestStatus.java`, `ApprovalActionType.java`; `service/ExpenseService.java`: `review()`.

**Sir ka follow-up:** “Ordinal kyun nahi?” — Enum order change karne se numeric ordinals ka meaning badal sakta hai; saved names readable aur stable hote hain.

## 13. Config-driven exactly kaise hai?

Company ka `financeThreshold` database ke ApprovalRule mein saved hai. `RuleService.updateRule()` Admin ko ye number change karne deta hai. Next submission par `ExpenseService.createRequest()` saved threshold padhta hai, isliye Java source edit/recompile kiye bina approval route change hota hai. Ye simple amount configuration hai, drag-and-drop workflow builder nahi.

**Code:** `model/ApprovalRule.java`; `service/RuleService.java`: `updateRule()`; `service/ExpenseService.java`: `createRequest()`.

**Sir ka follow-up:** “Rules kitne hain?” — Ek amount threshold per company; stages fixed Manager aur optional Finance hain.

## 14. Existing request ka requiresFinance kyun nahi badalta?

Submission ke waqt `requiresFinance` boolean snapshot save hota hai. Manager approval is saved flag ko use karta hai, current rule ko dobara nahi padhta. Isliye ₹5,000 limit par submitted ₹8,000 request ko Finance chahiye, chahe baad mein Admin limit ₹10,000 kar de. Ye existing request ki approval expectation stable rakhta hai.

**Code:** `service/ExpenseService.java`: `createRequest()`, `review()`; test `ExpenseServiceTest.thresholdChangeOnlyAffectsNewRequests()`.

**Sir ka follow-up:** “Purana threshold amount bhi store hai?” — Nahi, brief ke five-table MVP mein sirf routing decision boolean store hai; detail original exact threshold amount claim nahi karta.

## 15. History kaise store aur display hoti hai?

Har create/approve/reject par ApprovalAction row save hoti hai, jisme actor, action type, comment aur server time hota hai. `getHistory()` pehle request visibility check karta hai, phir oldest-first actions return karta hai. Same timestamp ho toh ID secondary ordering deta hai. React `RequestDetailPage` un DTOs ko timeline mein show karta hai.

**Code:** `model/ApprovalAction.java`; `service/ExpenseService.java`: `getHistory()`, `review()`; `frontend/src/pages/RequestDetailPage.jsx`.

**Sir ka follow-up:** “Rejected request mein kya dikhega?” — Submission aur correct reviewer ka rejection event, actor/time aur required reason.

## 16. HttpSession aur BCrypt kya karte hain? Limitations?

HttpSession server-side login state rakhta hai; browser HTTP-only cookie se session ko identify karta hai. Session mein sirf USER_ID save hota hai, aur successful login par session ID rotate hota hai. BCrypt salted password hash verify karta hai; plaintext passwords database mein store nahi hain. Ye local demo hai: complete Spring Security/CSRF protection, production deployment aur HTTPS hardening is implementation ka part nahi hain.

**Code:** `service/AuthService.java`: `login()`, `currentUser()`, `logout()`; `controller/AuthController.java`: `login()`; `config/DemoDataSeeder.java`; `application.properties`.

**Sir ka follow-up:** “Server restart par login rahega?” — Nahi, default in-memory sessions reset hote hain; MySQL requests/rules persist karte hain aur user dobara login karta hai.

## 17. Alpha Beta ka data kaise nahi dekh sakta?

Protected controller session se user load karta hai; company ID browser body se nahi leta. List queries company ID se scoped hain, aur Employee list mein submitter ID bhi hai. Detail/history company scope aur Employee ownership check karte hain; review query bhi company ID use karti hai. Invisible/missing request ko same 404 milta hai.

**Code:** `service/AuthService.java`: `currentUser()`; `service/ExpenseService.java`: `listVisibleRequests()`, `visibleRequest()`, `review()`; `service/RuleService.java`: `ownRule()`.

**Sir ka follow-up:** “UI mein button hide karna enough hai?” — Nahi, user direct HTTP request bhej sakta hai; Java authorization checks necessary hain.

## 18. Exception handling aur HTTP errors kaise work karte hain?

Invalid action par service `ApiException` throw karti hai, jisme HTTP status aur short message hota hai. `ApiExceptionHandler` usse consistent JSON `{message}` mein convert karta hai. Input validation 400, login 401, role 403, invisible request 404 aur wrong/final stage 409 deta hai. Unexpected failures 500 ka generic message dete hain; stack trace frontend response mein nahi jaata.

**Code:** `exception/ApiException.java`, `ApiExceptionHandler.java`; `dto/CreateExpenseRequestDto.java`.

**Sir ka follow-up:** “Validation kahan hoti hai?” — Controller ka `@Valid` DTO annotations check karta hai; rejection reason aur workflow checks service mein hain.

## 19. Approval par Transactional kyun?

Approval mein request status aur history dono change hone chahiye. Agar second database write fail ho toh transaction dono operations rollback kar sakta hai. Isse approved status without matching audit action jaisi partial state avoid hoti hai. Review query ka row lock transaction finish hone tak doosre decision ko wait karata hai.

**Code:** `service/ExpenseService.java`: `approve()`, `reject()`, `review()`; `repository/ExpenseRequestRepository.java`: `findForReview()`.

**Sir ka follow-up:** “Sirf frontend button disable enough hai?” — Nahi, simultaneous tabs/calls possible hain; backend transaction aur row lock same step ko do baar process nahi karte.

## 20. Manager finalized ya Finance-stage request approve kare toh?

`review()` finalized status par 409 Conflict throw karta hai. Manager ke liye PENDING_MANAGER aur Finance ke liye PENDING_FINANCE check hota hai; mismatch bhi 409 hai. Employee/Admin approval attempt 403 deta hai. Error hone par koi new approval history row save nahi hoti.

**Code:** `service/ExpenseService.java`: `review()`; tests `wrongStageCannotChangeStatus()`, `terminalRequestCannotBeChanged()`.

**Sir ka follow-up:** “Rejected request reopen ho sakti hai?” — Nahi, MVP mein Approved aur Rejected final hain; edit/delete/reopen routes nahi hain.

## 21. Future mein kaise extend kar sakte hain?

Future requirement aaye toh receipt handling, additional reviewer rules ya proper production authentication add ki ja sakti hai. Us waqt validation, storage aur authorization design ko naye scope ke hisaab se revise karna padega. Abhi ye features implemented nahi hain; current ExpenseService fixed two-stage flow aur amount threshold handle karta hai.

**Code:** `service/ExpenseService.java`, `service/RuleService.java`; scope in `README.md`.

**Sir ka follow-up:** “Kya abhi arbitrary approval levels configure ho sakte hain?” — Nahi, sirf Finance threshold configure hota hai.

## 22. React, Java aur MySQL ka exact contribution?

React inputs, navigation, loading/errors, status badges aur timeline render karta hai. Java Spring Boot session authentication, role/company checks, validation aur workflow decisions handle karta hai. MySQL users, rules, requests aur action history permanently store karta hai. Frontend ke hidden buttons UX hain; approval authority backend ke paas hai.

**Code:** `frontend/src/pages/RequestDetailPage.jsx`: `decide()`; `service/ExpenseService.java`: `review()`; five entity classes in `model/`.

**Sir ka follow-up:** “Browser refresh se request disappear?” — Nahi, MySQL se reload hoti hai; session expired ho toh pehle login karna padega.

## 23. Collection Framework ka actual use kahan hai?

`ExpenseService.listVisibleRequests()` aur `getHistory()` multiple database records ko `List` mein lete hain. Har method `new ArrayList<>()` banata hai aur enhanced `for` loop se safe DTO objects add karta hai. `List` interface hai aur `ArrayList` uska concrete implementation; order repository query decide karti hai aur loop usse preserve karta hai. Ye actual request list aur history screen ke liye hai, sirf unused collection example nahi hai.

**Code:** `service/ExpenseService.java`: `listVisibleRequests()`, `getHistory()`.

**Sir ka follow-up:** “`List<ExpenseResponseDto> result = new ArrayList<>()` mein do types kyun?” — Variable interface type ka hai, object ArrayList ka hai; `.add()`, `.size()` aur iteration jaise List operations use kar sakte hain.

## Five-minute demo script

| Time | Action | Bolne ke liye line |
| --- | --- | --- |
| Before demo | MySQL, backend and frontend start; Alpha threshold ₹5,000 confirm/reset with Admin | “Seed accounts automatically create hote hain; restart data erase nahi karta.” |
| 0:00–1:00 | Alpha Employee: ₹3,000 stationery and ₹8,000 travel submit | “Dono Manager ke paas jaate hain; route Java ne submission par decide kiya.” |
| 1:00–2:00 | Alpha Manager approves both | “Low amount final approve, high amount Finance ke paas.” |
| 2:00–3:00 | Alpha Finance approves travel; open history | “Teen actors ke actions server timestamps ke saath save hain.” |
| 3:00–4:00 | Alpha Admin ₹5,000 → ₹10,000; Employee creates new ₹8,000 | “Configuration change se new route Manager-only hai; old route unchanged hai.” |
| 4:00–5:00 | Beta Employee list, Beta Admin threshold ₹10,000 | “Same database, lekin session company filters data separate rakhte hain.” |

All demo passwords: `Demo@123`. Sign out between roles. Manager/Finance pending quick action opens the matching local status filter.

## Study order and practice

Pehle `Role`, `RequestStatus`, `ExpenseRequest` padho; phir `ExpenseService.createRequest()` aur `review()` khud trace karo. Uske baad controller → service → repository flow, AuthService, RuleService aur DemoDataSeeder dekho. `ExpenseServiceTest` mein ₹3,000/₹5,000/₹8,000 aur rule snapshot cases run karke explain karo. Tool-assisted work ko apni understanding ke saath honestly present karo; source code ke kisi authorship ka false claim mat karo.
