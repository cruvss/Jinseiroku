# 🛡️ Jenseiroku — 7-Day Sprint Roadmap (Spring Boot + Angular)

> **Goal:** Ship a fully functional, deployed Jenseiroku in 7 days.
> **Backend:** Spring Boot 3.x (Java 17+) — REST API, Spring Security, Spring Data JPA
> **Frontend:** Angular 18+ — Angular Material / PrimeNG, RxJS, Web Crypto API
> **Database:** PostgreSQL (recommended, but no restriction)
> **Philosophy:** Build the skeleton on Day 1, add muscle each day, polish on Day 7. Every day ends with a working, deployable app.

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| **Frontend** | Angular 18+ (standalone components) + TypeScript | Strong typing, modular architecture, built-in routing & forms |
| **UI Library** | Angular Material or PrimeNG | Polished component library, saves days of UI work |
| **Client Crypto** | Web Crypto API (browser native) | AES-256-GCM encryption, no external dependency |
| **Backend** | Spring Boot 3.x + Java 17+ | Enterprise-grade, massive ecosystem, great for portfolio |
| **Security** | Spring Security + JJWT (io.jsonwebtoken) | JWT filter chain, role-based access, password encoding |
| **Database** | PostgreSQL | Relational integrity, UUID support, JSONB, mature |
| **ORM** | Spring Data JPA + Hibernate | Entity mapping, repositories, migrations via Flyway/Liquibase |
| **Migrations** | Flyway | Version-controlled schema migrations |
| **Object Storage** | MinIO (dev) / AWS S3 (prod) via AWS SDK for Java | Encrypted blob storage |
| **Scheduling** | Spring @Scheduled + Quartz (optional) | Reminder engine, no Redis dependency needed initially |
| **Email** | Spring Mail (JavaMailSender) | Reminder email notifications (post-week-1) |
| **Build Tool** | Maven or Gradle | Dependency management, build lifecycle |
| **Deployment** | Docker + Railway / Render / Fly.io | Containerized deployment |

---

## Project Structure

### Backend (Spring Boot)

```
jenseiroku-api/
├── src/main/java/com/jenseiroku/
│   ├── JenseirokuApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java          (Spring Security filter chain)
│   │   ├── CorsConfig.java
│   │   ├── S3Config.java                (MinIO/S3 client bean)
│   │   └── JwtConfig.java
│   ├── security/
│   │   ├── JwtTokenProvider.java        (generate, validate, parse JWT)
│   │   ├── JwtAuthenticationFilter.java (OncePerRequestFilter)
│   │   └── UserPrincipal.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── UserSession.java
│   │   ├── InboxItem.java
│   │   ├── VaultDocument.java
│   │   ├── Subscription.java
│   │   ├── Task.java
│   │   ├── TaskCompletion.java
│   │   ├── TimelineEvent.java
│   │   ├── ReminderRule.java
│   │   └── ScheduledNotification.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── InboxItemRepository.java
│   │   ├── VaultDocumentRepository.java
│   │   ├── SubscriptionRepository.java
│   │   ├── TaskRepository.java
│   │   ├── TaskCompletionRepository.java
│   │   ├── TimelineEventRepository.java
│   │   ├── ReminderRuleRepository.java
│   │   └── ScheduledNotificationRepository.java
│   ├── dto/
│   │   ├── request/                     (LoginRequest, RegisterRequest, etc.)
│   │   └── response/                    (AuthResponse, ApiResponse, etc.)
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── VaultService.java
│   │   ├── InboxService.java
│   │   ├── SubscriptionService.java
│   │   ├── TaskService.java
│   │   ├── TimelineService.java
│   │   ├── ReminderService.java
│   │   └── StorageService.java          (S3/MinIO operations)
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── VaultController.java
│   │   ├── InboxController.java
│   │   ├── SubscriptionController.java
│   │   ├── TaskController.java
│   │   ├── TimelineController.java
│   │   └── ReminderController.java
│   ├── scheduler/
│   │   └── ReminderScheduler.java       (@Scheduled cron job)
│   └── exception/
│       ├── GlobalExceptionHandler.java  (@RestControllerAdvice)
│       ├── ResourceNotFoundException.java
│       └── UnauthorizedException.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/                    (Flyway migrations)
│       ├── V1__create_users_table.sql
│       ├── V2__create_vault_documents_table.sql
│       ├── V3__create_inbox_items_table.sql
│       ├── V4__create_subscriptions_table.sql
│       ├── V5__create_tasks_table.sql
│       ├── V6__create_timeline_events_table.sql
│       └── V7__create_reminders_tables.sql
└── pom.xml (or build.gradle)
```

### Frontend (Angular)

```
jenseiroku-web/
├── src/
│   ├── app/
│   │   ├── app.component.ts
│   │   ├── app.routes.ts
│   │   ├── core/
│   │   │   ├── guards/
│   │   │   │   └── auth.guard.ts
│   │   │   ├── interceptors/
│   │   │   │   ├── auth.interceptor.ts      (attach JWT to requests)
│   │   │   │   └── error.interceptor.ts     (global error handling)
│   │   │   ├── services/
│   │   │   │   ├── auth.service.ts
│   │   │   │   ├── crypto.service.ts        (Web Crypto API wrappers)
│   │   │   │   ├── inbox.service.ts
│   │   │   │   ├── vault.service.ts
│   │   │   │   ├── subscription.service.ts
│   │   │   │   ├── task.service.ts
│   │   │   │   ├── timeline.service.ts
│   │   │   │   └── reminder.service.ts
│   │   │   └── models/
│   │   │       ├── user.model.ts
│   │   │       ├── inbox-item.model.ts
│   │   │       ├── vault-document.model.ts
│   │   │       ├── subscription.model.ts
│   │   │       ├── task.model.ts
│   │   │       └── timeline-event.model.ts
│   │   ├── features/
│   │   │   ├── auth/
│   │   │   │   ├── login/login.component.ts
│   │   │   │   └── register/register.component.ts
│   │   │   ├── dashboard/
│   │   │   │   └── dashboard.component.ts
│   │   │   ├── inbox/
│   │   │   │   ├── inbox.component.ts
│   │   │   │   └── triage-dialog/triage-dialog.component.ts
│   │   │   ├── vault/
│   │   │   │   ├── vault.component.ts
│   │   │   │   └── upload-dialog/upload-dialog.component.ts
│   │   │   ├── subscriptions/
│   │   │   │   └── subscriptions.component.ts
│   │   │   ├── tasks/
│   │   │   │   └── tasks.component.ts
│   │   │   └── timeline/
│   │   │       └── timeline.component.ts
│   │   └── shared/
│   │       ├── components/
│   │       │   ├── sidebar/sidebar.component.ts
│   │       │   ├── topbar/topbar.component.ts
│   │       │   ├── confirm-dialog/confirm-dialog.component.ts
│   │       │   └── empty-state/empty-state.component.ts
│   │       └── layouts/
│   │           └── dashboard-layout/dashboard-layout.component.ts
│   ├── environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   ├── styles.scss
│   └── index.html
├── angular.json
├── package.json
└── tsconfig.json
```

---

## ⚠️ Scope Rules for 7 Days

### ✅ Full Implementation
- User auth (register, login, JWT with refresh tokens, Spring Security)
- Encrypted Vault (client-side AES-256-GCM, upload, download, image preview)
- Quick Capture Inbox (capture + triage to all modules)
- Subscription Tracker (CRUD + renewal date tracking)
- Tasks & Life Maintenance (one-off + recurring, completion logging)
- Life Timeline (CRUD + chronological view)
- Main Dashboard (aggregated overview)

### ⚡ Simplified
- Smart Reminders → in-app only via `@Scheduled` polling (skip email for now)
- Recovery Key → generate and display at signup (skip full recovery flow)
- File preview → images only (skip PDF preview)
- Envelope encryption → implement fully, skip key rotation UI

### ❌ Cut Entirely (Add After Week 1)
- Email notifications
- MFA / TOTP
- Browser push notifications
- Inbox age nudges
- Task templates API (hardcode templates in Angular)
- Automated tests (write after core is shipped)
- CI/CD pipeline (deploy manually, automate later)

---

## Pre-Day 1: Environment Setup (30 min, Night Before)

- [ ] Install JDK 17+ (or 21), Maven/Gradle
- [ ] Install Node.js 20+ and Angular CLI (`npm install -g @angular/cli`)
- [ ] Install PostgreSQL, create database: `createdb jenseiroku_dev`
- [ ] Install MinIO locally (Docker: `docker run -p 9000:9000 -p 9001:9001 minio/minio server /data --console-address ":9001"`)
- [ ] Create GitHub repo `jenseiroku`
- [ ] Generate Spring Boot project at [start.spring.io](https://start.spring.io):
  - Java 17+, Maven/Gradle, Spring Boot 3.x
  - Dependencies: Spring Web, Spring Security, Spring Data JPA, PostgreSQL Driver, Validation, Lombok
- [ ] Generate Angular project: `ng new jenseiroku-web --style=scss --routing --ssr=false`

---

## Day 1 (Monday): Project Skeleton + Auth System

**Goal:** User can register, login, and land on a protected dashboard. Both Angular and Spring Boot are running.

### Morning — Spring Boot Backend (4 hours)

- [ ] **Project setup:**
  - Add additional Maven dependencies: `jjwt-api`, `jjwt-impl`, `jjwt-jackson`, `flyway-core`, `argon2-jvm` (de.mkammerer), `aws-java-sdk-s3` (or AWS SDK v2)
  - Configure `application.yml`: database URL, JWT secret, token expiry, S3/MinIO credentials
  - Configure `SecurityConfig.java`: stateless session, disable CSRF (API-only), permit auth endpoints, require auth for everything else
- [ ] **Database migration V1 (Flyway):**
  ```sql
  -- V1__create_users_and_sessions.sql
  CREATE TABLE users (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      email VARCHAR(255) UNIQUE NOT NULL,
      password_hash VARCHAR(255) NOT NULL,
      encryption_salt VARCHAR(255) NOT NULL,
      encrypted_kek_verification TEXT,
      recovery_key_hash VARCHAR(255),
      timezone VARCHAR(50) DEFAULT 'UTC',
      created_at TIMESTAMP DEFAULT NOW(),
      updated_at TIMESTAMP DEFAULT NOW()
  );

  CREATE TABLE user_sessions (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      refresh_token_hash VARCHAR(255) UNIQUE NOT NULL,
      device_info JSONB,
      ip_address INET,
      expires_at TIMESTAMP NOT NULL,
      created_at TIMESTAMP DEFAULT NOW()
  );
  ```
- [ ] **Build `User` entity + `UserRepository`**
- [ ] **Build `JwtTokenProvider`:**
  - `generateAccessToken(userId)` — 15 min expiry
  - `generateRefreshToken()` — random UUID, stored hashed in DB
  - `validateToken(token)` — verify signature + expiry
  - `getUserIdFromToken(token)` — extract subject claim
- [ ] **Build `JwtAuthenticationFilter`** (extends `OncePerRequestFilter`):
  - Extract Bearer token from Authorization header
  - Validate and set `SecurityContextHolder` authentication
- [ ] **Build `AuthController` + `AuthService`:**
  - `POST /api/v1/auth/register` — validate, hash password with Argon2id, generate encryption salt, generate recovery key, return recovery key once
  - `POST /api/v1/auth/login` — verify credentials, create session, return access token in body + refresh token in HttpOnly cookie
  - `POST /api/v1/auth/refresh` — validate refresh token cookie, rotate tokens
  - `POST /api/v1/auth/logout` — delete session
- [ ] **Build `GlobalExceptionHandler`** (`@RestControllerAdvice`):
  - Consistent error response DTO: `{ success, error: { code, message } }`

### Afternoon — Angular Frontend (4 hours)

- [ ] **Setup Angular:**
  - Install Angular Material: `ng add @angular/material` (choose a dark/indigo theme)
  - OR install PrimeNG: `npm install primeng primeicons`
  - Setup global SCSS with custom color palette (dark theme, premium feel)
  - Configure `provideHttpClient(withInterceptors([...]))` in `app.config.ts`
- [ ] **Build `AuthService`:**
  - Store access token in a BehaviorSubject (memory only, not localStorage)
  - `login()`, `register()`, `logout()`, `refreshToken()` methods
  - `isAuthenticated$` observable for reactive UI
- [ ] **Build `AuthInterceptor`:**
  - Attach `Authorization: Bearer <token>` to all API requests
  - On 401 response: attempt token refresh, retry original request, redirect to login if refresh fails
- [ ] **Build `AuthGuard`:**
  - `canActivate` checks `AuthService.isAuthenticated$`
  - Redirect to `/login` if not authenticated
- [ ] **Build auth pages:**
  - **Login page** — email + password form, error display, link to register
  - **Register page** — email + password + confirm password, show recovery key in a modal after success ("Save this key! You will NOT see it again.")
- [ ] **Build dashboard shell (`DashboardLayoutComponent`):**
  - Angular Material sidenav (sidebar) with navigation links + icons for all 6 modules
  - Toolbar (topbar) with app name + user email + logout button
  - `<router-outlet>` for main content area
- [ ] **Configure routing (`app.routes.ts`):**
  ```typescript
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: '',
    component: DashboardLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: DashboardComponent },
      { path: 'inbox', component: InboxComponent },
      { path: 'vault', component: VaultComponent },
      { path: 'subscriptions', component: SubscriptionsComponent },
      { path: 'tasks', component: TasksComponent },
      { path: 'timeline', component: TimelineComponent },
    ]
  }
  ```

### Day 1 Deliverable
> ✅ User can register (sees recovery key), login, see a styled dashboard shell with sidebar, and logout. Spring Boot serves auth APIs with JWT. Angular has routing and auth guards.

---

## Day 2 (Tuesday): Encrypted Vault — The Core Differentiator

**Goal:** User can upload files that are encrypted in the browser, stored on the server, and decrypted back for preview/download.

### Morning — Client-Side Crypto Engine in Angular (4 hours)

- [ ] **Build `CryptoService` (`crypto.service.ts`):**
  - `deriveKEK(password: string, salt: string): Promise<CryptoKey>` — use `hash-wasm` or `argon2-browser` npm package for Argon2id → import result as AES key via Web Crypto API
  - `generateDEK(): Promise<CryptoKey>` — `crypto.subtle.generateKey('AES-GCM', 256)`
  - `encryptFile(file: ArrayBuffer, dek: CryptoKey): Promise<{ ciphertext, iv }>` — AES-256-GCM encrypt
  - `decryptFile(ciphertext: ArrayBuffer, dek: CryptoKey, iv: Uint8Array): Promise<ArrayBuffer>` — AES-256-GCM decrypt
  - `wrapDEK(dek: CryptoKey, kek: CryptoKey): Promise<ArrayBuffer>` — `crypto.subtle.wrapKey`
  - `unwrapDEK(wrappedDEK: ArrayBuffer, kek: CryptoKey): Promise<CryptoKey>` — `crypto.subtle.unwrapKey`
- [ ] **KEK session management:**
  - After login, derive KEK from password + salt (salt fetched from user profile)
  - Hold KEK in a private field inside `CryptoService` (memory only)
  - Clear on logout

### Afternoon — Vault Backend + Frontend (4 hours)

- [ ] **Flyway migration V2:**
  ```sql
  -- V2__create_vault_documents.sql
  CREATE TABLE vault_documents (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      file_name_encrypted TEXT NOT NULL,
      category VARCHAR(50) NOT NULL,
      tags_encrypted TEXT,
      notes_encrypted TEXT,
      blob_storage_key VARCHAR(500) NOT NULL,
      encrypted_dek TEXT NOT NULL,
      file_size_bytes BIGINT NOT NULL,
      mime_type VARCHAR(100),
      expiry_date DATE,
      created_at TIMESTAMP DEFAULT NOW(),
      updated_at TIMESTAMP DEFAULT NOW()
  );
  CREATE INDEX idx_vault_docs_user ON vault_documents(user_id);
  CREATE INDEX idx_vault_docs_category ON vault_documents(user_id, category);
  ```
- [ ] **`VaultDocument` entity + `VaultDocumentRepository`**
- [ ] **`StorageService`** — upload/download byte arrays to MinIO/S3 using AWS SDK
- [ ] **`VaultController` + `VaultService`:**
  - `POST /api/v1/vault/documents` — accepts multipart: encrypted blob + JSON metadata (encrypted DEK, file name, category, expiry). Stores blob in S3, metadata in DB.
  - `GET /api/v1/vault/documents` — paginated list, filtered by category, scoped to authenticated user
  - `GET /api/v1/vault/documents/{id}` — metadata + encrypted DEK
  - `GET /api/v1/vault/documents/{id}/download` — returns encrypted blob bytes
  - `DELETE /api/v1/vault/documents/{id}` — soft delete (or hard delete blob + record)
- [ ] **Angular Vault UI (`VaultComponent`):**
  - **Upload dialog:** drag-and-drop zone (or file input) → category dropdown → optional expiry date picker → "Encrypt & Upload" button
  - Upload flow: user selects file → Angular reads as ArrayBuffer → `CryptoService` generates DEK → encrypts file → wraps DEK with KEK → POSTs encrypted blob + wrapped DEK + metadata to API
  - **Document list:** Material table or card grid, filterable by category tabs
  - **Download/preview:** click document → fetch encrypted blob → unwrap DEK → decrypt → show image in a dialog or trigger browser download
  - **Delete:** confirmation dialog → soft delete

### Day 2 Deliverable
> ✅ User can upload files that are AES-256-GCM encrypted in the browser, stored encrypted on the server, listed by category, and decrypted back in the browser for preview. Zero-knowledge: server never sees plaintext.

---

## Day 3 (Wednesday): Quick Capture Inbox + Triage

**Goal:** User can dump notes/files into an inbox in seconds, then triage them into the Vault.

### Morning — Inbox Backend + Core UI (4 hours)

- [ ] **Flyway migration V3:**
  ```sql
  -- V3__create_inbox_items.sql
  CREATE TABLE inbox_items (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      content_type VARCHAR(20) NOT NULL,
      text_content_encrypted TEXT,
      file_storage_key VARCHAR(500),
      encrypted_dek TEXT,
      file_size_bytes BIGINT,
      mime_type VARCHAR(100),
      status VARCHAR(20) DEFAULT 'unprocessed',
      processed_to_type VARCHAR(20),
      processed_to_id UUID,
      captured_at TIMESTAMP DEFAULT NOW(),
      processed_at TIMESTAMP
  );
  CREATE INDEX idx_inbox_user_status ON inbox_items(user_id, status);
  ```
- [ ] **`InboxItem` entity + `InboxItemRepository`**
- [ ] **`InboxController` + `InboxService`:**
  - `POST /api/v1/inbox` — accepts multipart (optional file + optional text). Stores encrypted text/file.
  - `GET /api/v1/inbox` — list unprocessed items for authenticated user, ordered by `captured_at` DESC
  - `DELETE /api/v1/inbox/{id}` — hard delete (purge from S3 too if file exists)
  - `POST /api/v1/inbox/{id}/triage` — accepts `{ target: "vault"|"subscription"|"task"|"timeline", data: {...} }`. Creates the target entity via the respective service, marks inbox item as processed.
- [ ] **Angular Inbox UI (`InboxComponent`):**
  - **Quick Capture bar** at top: text input + file attachment button + "Capture" button. Minimal. Fast.
  - **Inbox list:** cards showing text snippet + file thumbnail (if image) + "captured 2 hours ago" relative time
  - Each card has action buttons: `→ Vault` · `→ Subscription` · `→ Task` · `→ Timeline` · `🗑️`

### Afternoon — Triage Flow: Inbox → Vault (2 hours)

- [ ] **Triage to Vault:**
  - Clicking `→ Vault` opens a Material dialog / slide-over panel
  - Pre-fills with inbox item data (text as notes, file ready to encrypt)
  - User picks: category, expiry date
  - On submit: encrypts file → creates vault document via API → marks inbox item processed
  - Processed items disappear from list

### Evening — Dashboard v1 (2 hours)

- [ ] **`DashboardComponent`:**
  - `GET /api/v1/dashboard` — backend endpoint that aggregates: inbox unprocessed count, vault document count, etc.
  - Card grid layout:
    - 📥 Inbox: "X items need attention"
    - 🔐 Vault: "X documents stored"
    - 💳 Subscriptions: placeholder
    - 📋 Tasks: placeholder
    - ⏳ Timeline: placeholder
    - 🔔 Reminders: placeholder
  - Style cards with Material elevation, icons, and accent colors

### Day 3 Deliverable
> ✅ User can quick-capture notes/files into the Inbox, triage them into the Vault. Dashboard shows live counts.

---

## Day 4 (Thursday): Subscriptions + Tasks

**Goal:** Both modules fully functional with triage integration from Inbox.

### Morning — Subscriptions (3 hours)

- [ ] **Flyway migration V4:**
  ```sql
  -- V4__create_subscriptions.sql
  CREATE TABLE subscriptions (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      name_encrypted TEXT NOT NULL,
      cost_cents INTEGER NOT NULL,
      currency VARCHAR(3) DEFAULT 'USD',
      billing_cycle VARCHAR(20) NOT NULL,
      next_billing_date DATE NOT NULL,
      status VARCHAR(20) DEFAULT 'active',
      linked_document_id UUID REFERENCES vault_documents(id),
      created_at TIMESTAMP DEFAULT NOW(),
      updated_at TIMESTAMP DEFAULT NOW()
  );
  CREATE INDEX idx_subs_user ON subscriptions(user_id);
  CREATE INDEX idx_subs_billing ON subscriptions(user_id, next_billing_date);
  ```
- [ ] **Entity + Repository + Controller + Service:** Full CRUD
- [ ] **Angular `SubscriptionsComponent`:**
  - List view: cards with name, cost/month, next billing date, status badge
  - Add/edit dialog: name, cost, currency, billing cycle, next date, status
  - Summary bar: "Total: $XX.XX/month · $XXX.XX/year"
- [ ] **Triage: Inbox → Subscription:**
  - `→ Subscription` button opens dialog pre-filled from inbox note text
  - On submit: creates subscription, marks inbox item processed

### Afternoon — Tasks (3 hours)

- [ ] **Flyway migration V5:**
  ```sql
  -- V5__create_tasks.sql
  CREATE TABLE tasks (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      title_encrypted TEXT NOT NULL,
      description_encrypted TEXT,
      category VARCHAR(20) NOT NULL,
      is_recurring BOOLEAN DEFAULT FALSE,
      cycle_type VARCHAR(10),
      cycle_interval INTEGER,
      due_date DATE,
      lead_time_days INTEGER DEFAULT 7,
      status VARCHAR(20) DEFAULT 'pending',
      linked_document_id UUID REFERENCES vault_documents(id),
      created_at TIMESTAMP DEFAULT NOW(),
      updated_at TIMESTAMP DEFAULT NOW()
  );
  CREATE INDEX idx_tasks_user ON tasks(user_id);
  CREATE INDEX idx_tasks_due ON tasks(user_id, due_date);

  CREATE TABLE task_completions (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
      completed_at TIMESTAMP DEFAULT NOW(),
      notes_encrypted TEXT
  );
  CREATE INDEX idx_completions_task ON task_completions(task_id);
  ```
- [ ] **Entities + Repositories + `TaskController` + `TaskService`:**
  - CRUD endpoints
  - `POST /api/v1/tasks/{id}/complete` — if one-off: set status=completed. If recurring: insert `TaskCompletion`, calculate next `due_date` (add cycle_interval of cycle_type), keep status=pending.
  - `GET /api/v1/tasks/{id}/completions` — completion history
- [ ] **Angular `TasksComponent`:**
  - Two Material tabs: **To-Do** (one-off) and **Recurring** (life maintenance)
  - To-Do tab: checkbox list, overdue items highlighted red
  - Recurring tab: cards with name, category, next due, last completion date, cycle info
  - Add dialog: title, description, category, due date, toggle "Make recurring" → reveals cycle fields
  - Complete action: checkbox for to-dos, "Mark Done" button for recurring (optional notes field)
  - Hardcoded templates dropdown in the add dialog (Dentist 6mo, Car Service 6mo, etc.)
- [ ] **Triage: Inbox → Task:**
  - `→ Task` button opens dialog with recurring toggle

### Evening — Dashboard Updates (1 hour)

- [ ] Update dashboard aggregation endpoint and cards:
  - Subscriptions card: total monthly cost, next renewal date
  - Tasks card: X overdue, X pending to-dos, next recurring due

### Day 4 Deliverable
> ✅ Subscriptions with cost tracking and Tasks (one-off + recurring) are fully functional. All 3 triage paths work (Vault, Subscription, Task).

---

## Day 5 (Friday): Life Timeline + Reminders + Dashboard

**Goal:** All modules functional. Reminders auto-generate. Dashboard aggregates everything.

### Morning — Timeline (3 hours)

- [ ] **Flyway migration V6:**
  ```sql
  -- V6__create_timeline_events.sql
  CREATE TABLE timeline_events (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      title_encrypted TEXT NOT NULL,
      description_encrypted TEXT,
      event_date DATE NOT NULL,
      end_date DATE,
      category VARCHAR(20) NOT NULL,
      linked_document_ids UUID[],
      created_at TIMESTAMP DEFAULT NOW(),
      updated_at TIMESTAMP DEFAULT NOW()
  );
  CREATE INDEX idx_timeline_user ON timeline_events(user_id);
  CREATE INDEX idx_timeline_date ON timeline_events(user_id, event_date);
  ```
- [ ] **Entity + Repository + Controller + Service:** CRUD
- [ ] **Angular `TimelineComponent`:**
  - Vertical timeline layout using custom CSS or a Material list with left-border styling
  - Events ordered by date DESC with year/month dividers
  - Category color-coded badges (Career=blue, Education=purple, Health=green, etc.)
  - Add dialog: title, description, date, end_date, category, link existing vault docs (multi-select dropdown)
- [ ] **Triage: Inbox → Timeline:**
  - `→ Timeline` opens dialog, pre-fills description from inbox note

### Afternoon — Smart Reminders (3 hours)

- [ ] **Flyway migration V7:**
  ```sql
  -- V7__create_reminders.sql
  CREATE TABLE reminder_rules (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      source_type VARCHAR(20) NOT NULL,
      source_id UUID NOT NULL,
      target_date DATE NOT NULL,
      lead_time_days INTEGER DEFAULT 7,
      reminder_offsets JSONB DEFAULT '[-30, -7, -1]',
      status VARCHAR(20) DEFAULT 'active',
      created_at TIMESTAMP DEFAULT NOW()
  );

  CREATE TABLE scheduled_notifications (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      reminder_rule_id UUID REFERENCES reminder_rules(id) ON DELETE CASCADE,
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      scheduled_for TIMESTAMP NOT NULL,
      channel VARCHAR(20) DEFAULT 'in_app',
      title VARCHAR(500),
      body TEXT,
      status VARCHAR(20) DEFAULT 'pending',
      sent_at TIMESTAMP
  );
  CREATE INDEX idx_notifications_schedule ON scheduled_notifications(status, scheduled_for);
  CREATE INDEX idx_notifications_user ON scheduled_notifications(user_id);
  ```
- [ ] **`ReminderService` — auto-generate reminders:**
  - Hook into `VaultService.create()`: if document has `expiry_date`, create `ReminderRule` with offsets `[-90, -30, -7, -1]`
  - Hook into `SubscriptionService.create()`: create `ReminderRule` with offsets `[-7, -3, -1]` from `next_billing_date`
  - Hook into `TaskService.create()`: create `ReminderRule` based on `lead_time_days`
  - Generate `ScheduledNotification` rows for each offset date
- [ ] **`ReminderScheduler`** (`@Scheduled(fixedRate = 60000)`):
  - Every minute: query `scheduled_notifications` where `scheduled_for <= NOW()` and `status = 'pending'`
  - Mark as `sent` (for now, just flip status — email sending is post-week-1)
- [ ] **`ReminderController`:**
  - `GET /api/v1/reminders/upcoming` — pending + recent notifications for the user
  - `PATCH /api/v1/reminders/{id}/dismiss`
  - `PATCH /api/v1/reminders/{id}/snooze` — accepts days to snooze
- [ ] **Angular notification bell:**
  - Bell icon in the topbar with unread count badge
  - Click opens a dropdown panel listing upcoming/overdue reminders
  - Each item shows source, message, date, dismiss/snooze buttons

### Evening — Dashboard Final Assembly (2 hours)

- [ ] **Backend `DashboardController`:**
  - `GET /api/v1/dashboard` — single endpoint returning aggregated data:
    - `inboxCount`, `vaultCount`, `nextExpiringDocument`
    - `totalMonthlySubscriptionCost`, `nextRenewal`
    - `overdueTaskCount`, `pendingTodoCount`, `nextRecurringTaskDue`
    - `recentTimelineEvent`
    - `upcomingReminders` (next 7 days), `overdueReminders`
- [ ] **Angular `DashboardComponent` — final version:**
  - 6 styled cards with real data, icons, accent colors
  - Overdue items in red, upcoming items in amber
  - Click any card → navigates to that module's page

### Day 5 Deliverable
> ✅ All 6 modules functional. All 4 triage paths work. Reminders auto-generate from documents, subscriptions, and tasks. Dashboard shows live aggregated data. App is feature-complete.

---

## Day 6 (Saturday): UI Polish + Responsive Design

**Goal:** The app looks premium. Proper loading states, error handling, animations, mobile-ready.

### Morning — Visual Polish (4 hours)

- [ ] **Design consistency pass:**
  - Consistent spacing, typography, color usage across all pages
  - All forms have Angular reactive form validation with `mat-error` messages
  - Loading skeletons or `mat-spinner` for all data-fetching views
  - Empty states for all lists ("No documents yet. Upload your first file →")
  - `mat-dialog` confirmations for all destructive actions
  - `MatSnackBar` toast notifications for success/error on all mutations
- [ ] **Micro-animations:**
  - Angular `@angular/animations` for route transitions
  - Sidebar active link indicator
  - Card hover effects (subtle elevation change)
  - Inbox items fade out on triage
  - Staggered list item entrance animations
- [ ] **Dashboard polish:**
  - Cards with gradient borders or subtle background patterns
  - Hover states that show more detail

### Afternoon — Responsive Design (3 hours)

- [ ] **Mobile layout:**
  - Material sidenav `mode="over"` on mobile, `mode="side"` on desktop
  - Hamburger menu toggle button in topbar
  - Stack cards vertically on `< 768px`
  - Full-width forms on mobile
  - `@media` breakpoints: 576px, 768px, 1024px, 1200px
- [ ] **Test on:**
  - 375px (iPhone SE), 390px (iPhone 14), 768px (iPad), 1440px (desktop)
  - Chrome DevTools responsive mode

### Evening — Error Handling (1 hour)

- [ ] **Angular `ErrorInterceptor`:** catch all HTTP errors, show user-friendly toasts
- [ ] **Spring Boot `GlobalExceptionHandler`:** handle all exceptions consistently
- [ ] **Session expiry:** auto-redirect to login with "Session expired" snackbar
- [ ] **File upload validation:** check size (50MB limit) and type before uploading
- [ ] **Offline banner:** detect `navigator.onLine` changes, show warning bar

### Day 6 Deliverable
> ✅ The app looks polished and premium. Every interaction has visual feedback. Works on mobile, tablet, desktop.

---

## Day 7 (Sunday): Deployment + Documentation + Demo

**Goal:** App is live on the internet. README and documentation are complete. Demo video recorded.

### Morning — Deployment (3 hours)

- [ ] **Dockerize:**
  - `Dockerfile` for Spring Boot API (multi-stage: Maven build → JRE runtime)
  - `Dockerfile` for Angular (multi-stage: `ng build` → Nginx serve)
  - `docker-compose.yml` for local dev (API + Web + PostgreSQL + MinIO)
- [ ] **Deploy backend:**
  - Deploy Spring Boot JAR to Railway / Render / Fly.io
  - Provision PostgreSQL (Railway / Neon / Supabase)
  - Configure environment variables (`SPRING_DATASOURCE_URL`, `JWT_SECRET`, S3 keys)
- [ ] **Deploy frontend:**
  - Build Angular production bundle: `ng build --configuration=production`
  - Deploy to Vercel / Netlify / Cloudflare Pages (static hosting)
  - Set `API_URL` environment variable to production backend URL
- [ ] **Setup S3:** Create production bucket (AWS S3, Cloudflare R2, or Backblaze B2)
- [ ] **Verify end-to-end in production:**
  - Register → Login → Upload encrypted file → Download and decrypt → Quick capture → Triage → Dashboard check

### Afternoon — Documentation (3 hours)

- [ ] **README.md:**
  - Project description, motivation, and one-line pitch
  - Screenshots of key screens (dashboard, vault, inbox, timeline)
  - Architecture diagram (text-based or Mermaid)
  - Tech stack table
  - Local dev setup instructions (both Spring Boot and Angular)
  - Environment variables reference
  - API endpoint summary
  - Security model explanation (encryption flow diagram)
- [ ] **SECURITY.md:**
  - Zero-knowledge encryption architecture
  - Key derivation flow (password → Argon2id → KEK → wraps DEKs)
  - What the server can and cannot see
  - Threat model summary

### Evening — Demo + Final Touches (2 hours)

- [ ] **Record 3-5 minute demo video:**
  - Registration flow (show recovery key warning)
  - Upload and encrypt a document in the vault
  - Quick capture a note → triage to task
  - Subscription tracker with cost summary
  - Complete a recurring task → see it reschedule
  - Timeline view
  - Dashboard with all live data
  - Notification bell with reminders
  - Mention: "Even if the database is breached, files are unreadable"
- [ ] **Final checks:**
  - No console errors, all pages load
  - Favicon, page titles, meta tags
  - Mobile quick test on a real phone

### Day 7 Deliverable
> ✅ App is deployed with a public URL. README with screenshots is done. Demo video recorded. Portfolio-ready.

---

## Daily Summary

| Day | Focus | Stack Used | Hours |
|-----|-------|-----------|:-----:|
| Day 1 (Mon) | Project setup + Auth | Spring Security + JWT + Angular routing | ~8h |
| Day 2 (Tue) | Encrypted Vault | Web Crypto API + S3 + Spring Data JPA | ~8h |
| Day 3 (Wed) | Quick Capture Inbox + Triage | Spring multipart upload + Angular dialogs | ~8h |
| Day 4 (Thu) | Subscriptions + Tasks | Full CRUD + completion engine | ~7h |
| Day 5 (Fri) | Timeline + Reminders + Dashboard | @Scheduled + aggregation queries | ~8h |
| Day 6 (Sat) | UI polish + responsive | Angular Material + animations + SCSS | ~8h |
| Day 7 (Sun) | Deploy + docs + demo | Docker + Railway/Vercel + README | ~8h |
| **Total** | | | **~55h** |

---

## What to Do If You Fall Behind

| If you're behind by... | Cut this... |
|---|---|
| Half a day | Skip Timeline (add after the week) |
| A full day | Skip Timeline + Reminders (show due dates inline instead) |
| 1.5 days | Skip Timeline + Reminders + Recurring tasks (keep only one-off To-Dos) |
| 2 days | Ship Vault + Inbox + Auth only. A polished 3-module app beats a broken 6-module app. |

> **Golden rule:** A deployed, polished app with 3 features is infinitely more impressive than a localhost app with 6 half-broken features. Ship quality over quantity.

---

## Post-Week-1 Backlog

Once the core is live, add these in priority order:

1. [ ] Unit + integration tests (JUnit 5 + Mockito for backend, Jasmine/Karma for Angular)
2. [ ] CI/CD pipeline (GitHub Actions: build → test → deploy)
3. [ ] Email notifications via Spring Mail + Resend/SMTP
4. [ ] MFA/TOTP support (using a TOTP library like `java-totp`)
5. [ ] PDF preview in vault (pdf.js in Angular)
6. [ ] Inbox age nudges
7. [ ] Task templates REST API (currently hardcoded in frontend)
8. [ ] Key rotation flow (change password → re-wrap all DEKs)
9. [ ] Data export (GDPR-style full download)
10. [ ] Performance: pagination, lazy loading Angular modules, Hibernate query tuning
