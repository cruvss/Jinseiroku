# 🛡️ Jenseiroku — Secure Personal Life Operating System (Refactored)

## Executive Summary

Jenseiroku is a **privacy-first personal life management platform** that combines encrypted document storage, a quick-capture inbox, and practical life admin tools. It solves two primary problems: your important documents are scattered and insecure, and your day-to-day life administration (subscriptions, renewals, appointments, bills) is hard to track.

Unlike generic productivity apps, Jenseiroku treats your personal data with bank-grade security protocols (client-side zero-knowledge encryption) while offering an extremely friction-free entry point to dump data instantly and organize it later.

**One-line pitch:** *"Your entire adult life — captured instantly, organized securely, and automated dynamically."*

---

## The Problem

1. **Documents are scattered and insecure.** Your passport scan is in Gmail, your lease is in Google Drive, and your insurance card is a photo in your camera roll. If any of those services are breached, your most sensitive data is exposed.
2. **Life admin is invisible and relentless.** Your passport expires and you realize too late. You're paying for subscriptions you forgot to cancel. The dentist appointment you were supposed to book 6 months ago never happened.
3. **No single app connects documents to deadlines.** Your passport is in one app, and the reminder to renew it is in another (or nowhere). Jenseiroku connects the document to its expiration date, and then to a smart, lead-time aware reminder.
4. **Organizing data has high friction.** When a receipt, warranty, or idea comes up, you rarely have the time or energy to open a complex dashboard, choose the right folder, fill out a 10-field form, and catalog it. You need a way to capture it in 5 seconds and sort it later.

---

## Core Modules

### 1. 📥 Quick Capture Inbox (Core Intake)
A universal, low-friction dumping ground for anything life-related that comes at you during the day. Capture text or files in 5 seconds, then triage and organize them later when you have time.

#### Features
- **Zero-Friction Capture** — Single-tap input for notes, photos, or documents. No categories, folders, or required metadata fields at the time of entry.
- **Mixed Content Support** — Upload files (receipts, letter photos, PDFs) with accompanying short text notes.
- **Triage Actions** — Move items directly from the Inbox into other modules:
  - **Convert to Vault Document** (sends the file to the Encrypted Vault with a category and optional expiry).
  - **Convert to Subscription** (pre-fills a subscription entry with cost, name, and billing cycle parsed from the note).
  - **Convert to Task** (auto-creates either a one-off To-Do task or a recurring life task).
  - **Convert to Timeline Event** (posts directly to the Life Timeline).
  - **Delete** (permanently purges the captured item).
- **Inbox Age Nudges** — Highlight items sitting in the inbox for more than 7 days to prevent clutter.

---

### 2. 🔐 Encrypted Vault
A zero-knowledge document storage system where files are encrypted **in the browser** before they ever touch the server. The server stores encrypted blobs; it cannot read your data.

#### Features
- **Client-Side Encryption** — AES-256-GCM encryption occurs in the browser using the Web Crypto API.
- **Key Derivation** — A master Key Encryption Key (KEK) is derived from the user's password using Argon2id. The key never leaves the client.
- **Envelope Encryption** — Each document gets its own random Data Encryption Key (DEK), wrapped by the KEK. This allows password changes/key rotations without re-encrypting the underlying files.
- **Life Domains** — Group documents by categories:
  - 🪪 **Identity** (Passport, ID, licenses)
  - 🎓 **Education** (Degrees, certificates)
  - 🏥 **Health** (Insurance cards, vaccine records)
  - ⚖️ **Legal** (Contracts, leases, tax filings)
  - 🏠 **Property** (Warranties, deeds, manuals)
- **Metadata with Expiry Tracking** — Associate expiry dates with documents to feed directly into the Reminders Engine.
- **In-Memory File Decryption** — Decrypt and preview PDFs/images directly in-browser. Plaint-text data is never written to disk.
- **Recovery Key** — A printable key generated at signup to recover data if the user forgets their password. Without it, encrypted data is permanently lost.

---

### 3. 💳 Subscription Tracker
Track recurring payments in your life to avoid unwanted renewals.

#### Features
- **Log Subscriptions** — Track cost, billing cycle (monthly, quarterly, semi-annual, annual), next billing date, and cancellation details.
- **Renewal Alerts** — Configurable alerts prior to renewal dates (e.g., 3 days, 1 week before).
- **Status Management** — Mark subscriptions as active, paused, or cancelled.
- **Document Association** — Attach receipts or contract PDFs from the Vault directly to subscription logs.

---

### 4. 🔔 Smart Reminders Engine
Proactive, **lead-time aware** notifications that account for how long real-world processes take.

#### Features
- **Dynamic Offsets** — Automatically schedules multiple notifications leading up to a deadline:
  - *Example:* Passport expires on March 15. The engine knows renewal takes ~6 weeks, scheduling notifications at 3 months (early warning), 6 weeks (action needed), and 2 weeks (urgent warning).
- **Task & Subscription Sync** — Auto-generates alerts for expiring documents, pending subscription renewals, and overdue tasks.
- **Notification Channels** — Delivers reminders via in-app notifications and scheduled email digests.

---

### 5. 📋 Tasks & Life Maintenance
A unified task management center for both one-off to-dos and recurring life maintenance schedules.

#### Features
- **One-off Tasks (To-Dos)** — Simple tasks with a target due date (e.g., *"Mail back router"*). Once completed, they are archived.
- **Recurring Life Tasks** — Operations that repeat on custom cycles (e.g., *"Dentist Checkup"*). Marking them done automatically logs the event and schedules the next due date based on the cycle.
- **Task Templates** — Built-in templates with default frequencies (e.g., Dentist: 6 months; Car Oil Change: 6 months/5,000 miles; Smoke Detector Battery: 1 year).
- **Completion Logging** — Logs completed dates, notes, and costs for audit history.

---

### 6. ⏳ Life Timeline
A private, chronological record of major life events, milestones, and personal progression.

#### Features
- **Event Logging** — Log career changes, moves, health events, and achievements.
- **Visual Chronology** — Scrollable vertical timeline with filters.
- **Document References** — Link vault documents directly to timeline milestones (e.g., degree scan linked to "Graduated University").

---

## Infrastructure & Security Requirements
*(No dedicated "Security Center" UI, but these security policies are strictly enforced at the database and backend levels)*
- **Stateless JWT Session Management** — Short-lived access tokens (stored in memory) and secure, HttpOnly, SameSite refresh tokens.
- **Argon2id Password Hashing** — Secure hashing at registration and verification.
- **Granular Authorization Middleware** — Every request validates the token signature and verifies the subject (`userId`) explicitly matches the resource owner.
- **Multi-Factor Authentication (MFA)** — Optional backend support for TOTP verification keys during authentication.

---

## Database Schema (Refactored)

### Schema (PostgreSQL)

```
users
├── id (UUID, PK)
├── email (unique, indexed)
├── password_hash (Argon2id)
├── mfa_secret_encrypted (nullable)
├── mfa_enabled (boolean)
├── recovery_key_hash (for password reset validation)
├── encryption_salt (for client-side KDF)
├── encrypted_kek_verification (used to verify correct password in browser)
├── currency (default USD)
├── timezone (IANA timezone string)
├── created_at
└── updated_at

inbox_items
├── id (UUID, PK)
├── user_id (FK → users, indexed)
├── content_type (ENUM: text, file, mixed)
├── text_content_encrypted (encrypted note text)
├── file_storage_key (S3 key reference, nullable)
├── encrypted_dek (file decryption key wrapped by KEK, nullable)
├── status (ENUM: unprocessed, processed, deleted)
├── processed_to_type (nullable ENUM: vault, subscription, task, timeline)
├── processed_to_id (nullable UUID - FK to the generated entity)
├── captured_at (timestamp)
└── processed_at (nullable timestamp)

vault_documents
├── id (UUID, PK)
├── user_id (FK → users, indexed)
├── file_name_encrypted (encrypted in DB)
├── category (ENUM: identity, education, health, legal, finance, property, other)
├── tags_encrypted (JSONB array, encrypted)
├── notes_encrypted (nullable)
├── blob_storage_key (S3 key reference)
├── encrypted_dek (file decryption key wrapped by KEK)
├── file_size_bytes (integer)
├── mime_type (varchar)
├── expiry_date (nullable date - feeds reminder engine)
├── created_at
└── updated_at

subscriptions
├── id (UUID, PK)
├── user_id (FK → users, indexed)
├── name_encrypted
├── cost_cents (integer)
├── currency (ISO 4217)
├── billing_cycle (ENUM: monthly, quarterly, semi_annual, annual)
├── next_billing_date (date, indexed)
├── status (ENUM: active, paused, cancelled)
├── linked_document_id (FK → vault_documents, nullable)
├── created_at
└── updated_at

tasks
├── id (UUID, PK)
├── user_id (FK → users, indexed)
├── title_encrypted
├── description_encrypted (nullable)
├── category (ENUM: health, vehicle, home, finance, personal, other)
├── is_recurring (boolean, default false)
├── cycle_type (nullable ENUM: days, weeks, months, years)
├── cycle_interval (nullable integer)
├── due_date (date, indexed - next due date for recurring, due date for one-off)
├── lead_time_days (integer, default 7)
├── status (ENUM: pending, completed)
├── linked_document_id (FK → vault_documents, nullable)
├── created_at
└── updated_at

task_completions
├── id (UUID, PK)
├── task_id (FK → tasks, indexed)
├── completed_at (timestamp)
├── notes_encrypted (nullable)
└── created_at

timeline_events
├── id (UUID, PK)
├── user_id (FK → users, indexed)
├── title_encrypted
├── description_encrypted (nullable)
├── event_date (date, indexed)
├── end_date (nullable date)
├── category (ENUM: career, education, health, legal, personal, financial, property)
├── linked_document_ids (UUID array, nullable)
├── created_at
└── updated_at

reminder_rules
├── id (UUID, PK)
├── user_id (FK → users, indexed)
├── source_type (ENUM: document, subscription, task, custom)
├── source_id (UUID — polymorphic reference)
├── target_date (date)
├── lead_time_days (integer)
├── reminder_offsets (JSONB array of offsets, e.g. [-90, -30, -7])
├── status (ENUM: active, snoozed, dismissed, completed)
├── created_at
└── updated_at

scheduled_notifications
├── id (UUID, PK)
├── reminder_rule_id (FK → reminder_rules, indexed)
├── user_id (FK → users, indexed)
├── scheduled_for (timestamp, indexed)
├── channel (ENUM: in_app, email)
├── title (varchar)
├── body (text)
├── status (ENUM: pending, sent, failed)
├── sent_at (nullable)
└── created_at

user_sessions
├── id (UUID, PK)
├── user_id (FK → users, indexed)
├── refresh_token_hash (unique)
├── device_info (JSONB)
├── ip_address (inet)
├── expires_at (timestamp)
└── created_at
```

---

## API Design

Prefix: `/api/v1/`

### Auth & Infrastructure Endpoints
* `POST /auth/register` — Create account.
* `POST /auth/login` — Login (issues JWT & HttpOnly cookie).
* `POST /auth/refresh` — Rotate access token.
* `POST /auth/logout` — Revoke active session.
* `GET /sessions` — List active sessions.
* `DELETE /sessions/:id` — Revoke specific session.

### Inbox Endpoints
* `GET /inbox` — List active, unprocessed items.
* `POST /inbox` — Quick-capture note/file.
* `DELETE /inbox/:id` — Dismiss/delete item.
* `POST /inbox/:id/triage` — Convert to another entity (Vault, Subscription, Task, Timeline).

### Vault Endpoints
* `GET /vault/documents` — Get document metadata list.
* `GET /vault/documents/:id/download` — Get file download URL/blob reference.
* `POST /vault/documents` — Add encrypted document.
* `PATCH /vault/documents/:id` — Update document metadata.
* `DELETE /vault/documents/:id` — Soft-delete.

### Subscription Endpoints
* `GET /subscriptions` — Get active subscriptions.
* `POST /subscriptions` — Add subscription.
* `PATCH /subscriptions/:id` — Update subscription details.
* `DELETE /subscriptions/:id` — Remove subscription.

### Tasks Endpoints
* `GET /tasks` — List tasks (filterable by status: pending/completed, and type: recurring/one-off).
* `POST /tasks` — Create new task (one-off or recurring).
* `PATCH /tasks/:id` — Edit task details.
* `DELETE /tasks/:id` — Remove task.
* `POST /tasks/:id/complete` — Mark task as complete (if recurring, calculates next due date and logs to task_completions; if one-off, sets status to completed).
* `GET /tasks/:id/completions` — List completion history for a recurring task.
* `GET /tasks/templates` — Get pre-built task templates.

### Timeline Endpoints
* `GET /timeline` — Chronological milestones list.
* `POST /timeline` — Create milestone event.

---

## Tech Stack
* **Frontend:** Angular 18+ + TypeScript + Angular Material or PrimeNG.
* **Client Cryptography:** Web Crypto API (Native AES-256-GCM).
* **Backend:** Spring Boot 3.x + Java 17+ (Spring Security, Spring Mail).
* **Database & ORM:** PostgreSQL + Spring Data JPA (Hibernate).
* **Migrations:** Flyway.
* **Scheduling:** Spring `@Scheduled`.
* **Object Storage:** AWS S3 (Production) / MinIO (Local Dev) via AWS SDK for Java.

---

## Development Roadmap (10-Week Plan)

### Phase 1: Foundation (Week 1–2)
* Initialize workspace monorepo.
* Setup Next.js, Fastify backend, and Prisma with PostgreSQL.
* Implement user registration, Argon2id hashing, and JWT token rotation.
* Deploy basic landing/auth interface.

### Phase 2: Client Crypto & Vault (Week 3–4)
* Implement browser Web Crypto API pipelines (KEK derivation, DEK wrapper, AES encryption).
* Setup MinIO / S3 document uploading and downloading mechanisms.
* Implement client-side PDF/Image decryption previews in memory.
* Create recovery key registration step.

### Phase 3: Quick Capture Inbox (Week 5)
* Create Quick Capture component (quick input form + image capture).
* Build database structures for raw inbox files/notes (`inbox_items`).
* Build the Triage Dashboard UI where users can see files/notes and trigger transformations.
* Connect triage handlers (Inbox Item → Vault Document).

### Phase 4: Subscriptions & Tasks (Week 6)
* Create subscription registration views.
* Implement the unified Tasks UI (separate sections/tabs for To-Do List and Recurring Life Tasks).
* Implement backend endpoints for task completion logging and auto-rescheduling.
* Build triage handlers (Inbox Item → Subscription; Inbox Item → Task).

### Phase 5: Reminders Engine (Week 7–8)
* Setup BullMQ scheduler using Redis.
* Write reminder generator loops mapping document expiries, tasks (both due dates and recurring schedules), and subscription renewal dates.
* Add email notification services (Resend / Nodemailer) for automated reminders.

### Phase 6: Timeline & Integration (Week 9)
* Implement vertical timeline UI with date-ordering.
* Build triage handler (Inbox Item → Timeline Event).
* Connect document links to timeline items and task completions.

### Phase 7: Dashboard & Polish (Week 10)
* Assemble main dashboard (unprocessed inbox items count, upcoming reminders list, overdue tasks).
* Conduct performance testing (large file uploads, memory profiles of Web Crypto decrypts).
* Build comprehensive tests (Vitest + Playwright E2E).
* Release production build.
