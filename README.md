# Jinseiroku
A privacy-first personal life management system that lets you securely store documents, track subscriptions, manage tasks, and chronicle your life timeline — all with end-to-end encryption so your data stays yours.

---

## Features

| Module | Description |
|--------|-------------|
|**Encrypted Vault** | Upload and store sensitive documents encrypted client-side with AES-256-GCM before they ever leave your browser |
|**Task Manager** | Create, track, and auto-renew recurring tasks with category grouping and document linking |
|**Life Timeline** | Chronicle important life events with date ranges, categories, and linked vault documents |
|**Subscription Tracker** | Track recurring subscriptions with billing cycles, costs, and upcoming renewal alerts |
|**Inbox** | Capture quick notes or files that can later be processed into vault documents or tasks |
|**Reminder Engine** | Automated email reminders with configurable lead-time offsets |
|**Dashboard** | Aggregated view of upcoming renewals, overdue tasks, and recent vault activity |
|**Stripe Payments** | Subscription plan upgrades handled through Stripe Checkout (Demo only)|

---

## Architecture

```
Jinseiroku/
├── backend/          # Spring Boot 4 REST API (Java 25)
├── frontend/         # Angular 22 SPA
├── minioInit/        # MinIO object storage
└── docker-compose.yml
```

### Security Model

Jinseiroku uses a **zero-knowledge encryption architecture**:

1. **Argon2id** (WASM, client-side) derives a 256-bit **Key Encryption Key (KEK)** from the user's password + a per-user salt.
2. For each document, a random **Data Encryption Key (DEK)** is generated in the browser using `AES-256-GCM`.
3. The file bytes are encrypted with the DEK; the DEK is then **wrapped** with the KEK using `AES-KW`.
4. Only the wrapped DEK and encrypted ciphertext are sent to the server — the server never sees plaintext or the KEK.
5. Task titles, timeline entries, inbox content, and document metadata are also encrypted end-to-end.

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 25 | Runtime |
| Spring Boot | 4.1.0 | Web framework |
| Spring Security | — | JWT auth + route protection |
| Spring Data JPA | — | ORM layer |
| PostgreSQL | 15 | Primary database |
| Flyway | — | Database migrations |
| MinIO | 8.5.7 | S3-compatible object storage |
| JJWT | 0.12.7 | JWT access & refresh tokens |
| Argon2-JVM | 2.1 | Server-side password hashing |
| BouncyCastle | 1.84 | Cryptographic primitives |
| Stripe Java | 33.1.0 | Payment processing |
| SpringDoc OpenAPI | 3.0.3 | Swagger UI / API docs |
| Spring Mail | — | Email reminders |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Angular | 22 | SPA framework |
| Angular Material | 22 | UI component library |
| TypeScript | 6.0 | Type-safe JavaScript |
| SCSS | — | Styling |
| hash-wasm | 4.12 | Argon2id in the browser |
| Web Crypto API | — | AES-GCM / AES-KW encryption |
| RxJS | 7.8 | Reactive streams |
| PrimeIcons | 7 | Icon set |


## Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) & Docker Compose
- [Node.js](https://nodejs.org/) 24+ and npm 11+ *(for local frontend dev)*
- [Java 25](https://adoptium.net/) and Maven *(for local backend dev)*

---

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/Jinseiroku.git
cd Jinseiroku
```

---

### 2. Configure Environment Variables

Create a `.env` file in the project root:

```env
# Database
DATABASE_URL=jdbc:postgresql://db:5431/life [DEFAULT]
DATABASE_USER=sachin [DEFAULT]
DATABASE_PASSWORD=root [DEFAULT]

# JWT
JWT_SECRET=your_super_secret_jwt_key_at_least_256_bits

# MinIO
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=admin [DEFAULT]
MINIO_SECRET_KEY=admin123456 [DEFAULT]
MINIO_BUCKET=userdocuments  [DEFAULT]

# Mail (SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your@email.com
MAIL_PASSWORD=your_app_password
MAIL_FROM=your@email.com

# Stripe
STRIPE_KEY=sk_test_your_stripe_secret_key
```
> If you are lazy use default config as above.
---

### 3. Run with Docker Compose

```bash
docker-compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:9090 |
| Swagger UI | http://localhost:9090/swagger-ui.html |
| MinIO Console | http://localhost:9001 |

---

### 4. Run Locally (Development)

#### Backend

```bash
cd backend
./mvnw spring-boot:run
```

> Ensure PostgreSQL is running and the `.env` file (or environment variables) are available.

#### Frontend

```bash
cd frontend
npm install
npm start
```

The Angular dev server starts at **http://localhost:4200** with hot-reload enabled.

---

## Project Structure (For the nerds)

### Backend (`/backend`)

```
src/main/java/com/cruvs/backend/
├── BackendApplication.java         # Entry point
├── config/
│   ├── SecurityConfig.java         # Spring Security & CORS config
│   ├── MinioConfig.java            # MinIO client bean
│   ├── SwaggerConfig.java          # OpenAPI / Swagger setup
│   └── JsonConfig.java             # Jackson configuration
├── controller/
│   ├── AuthController.java         # /v1/auth  — register, login, refresh, logout
│   ├── VaultController.java        # /v1/vault — encrypted document CRUD + download
│   ├── TaskController.java         # /v1/tasks — task management
│   ├── TimelineController.java     # /v1/timeline — life events
│   ├── SubscriptionController.java # /v1/subscriptions — subscription tracking
│   ├── InboxController.java        # /v1/inbox — capture queue
│   ├── ReminderController.java     # /v1/reminders — reminder rules
│   ├── DashboardController.java    # /v1/dashboard — aggregated stats
│   ├── PaymentController.java      # /v1/payment — Stripe checkout
│   └── SubscriptionPlanController.java # /v1/plans — available plans
├── entity/                         # JPA entities (11 tables)
├── dto/                            # Request / Response DTOs
├── repository/                     # Spring Data JPA repositories
├── service/                        # Business logic layer (13 services)
├── security/
│   ├── JwtTokenProvider.java       # JWT sign / verify
│   └── JwtAuthenticationFilter.java # Token extraction filter
├── exception/                      # Global exception handlers
├── response/                       # Unified ApiResponse wrapper
└── util/                           # Helper utilities

src/main/resources/
├── application.yaml                # App configuration
└── db/migration/                   # Flyway SQL migrations (V1–V10)
```

### Frontend (`/frontend/src`)

```
app/
├── app.routes.ts                   # Route definitions
├── core/
│   ├── guards/auth.guard.ts        # Route protection
│   ├── interceptors/               # HTTP auth interceptor
│   ├── models/                     # TypeScript interfaces
│   └── services/
│       ├── auth.service.ts         # Login, register, token refresh
│       ├── crypto.service.ts       # Client-side AES encryption (Web Crypto API + Argon2id)
│       ├── task.service.ts         # Task API calls
│       ├── timeline.service.ts     # Timeline API calls
│       ├── subscription.service.ts # Subscription API calls
│       ├── inbox.service.ts        # Inbox API calls
│       ├── reminder.service.ts     # Reminder API calls
│       └── payment.service.ts      # Stripe payment calls
└── features/
    ├── auth/                       # Login & Register pages
    ├── dashboard/                  # Main layout shell + dashboard view
    ├── vault/                      # Encrypted document manager
    ├── tasks/                      # Task list & management
    ├── timeline/                   # Life timeline view
    ├── subscription/               # Subscription tracker
    ├── inbox/                      # Capture inbox
    ├── payment/                    # Stripe checkout page
    ├── payment-success/            # Post-payment confirmation
    ├── task-dialog/                # Task create/edit modal
    ├── timeline-dialog/            # Timeline event modal
    ├── subscription-dialog/        # Subscription modal
    └── profile-dialog/             # User profile modal
```

---

## Database Schema

Managed with **Flyway** migrations (V1 → V10):

| Migration | Table(s) Created |
|-----------|-----------------|
| V1 | `users`, `user_sessions` |
| V2 | Remove refresh token hash column |
| V3 | `vault_documents` |
| V4 | `inbox_items` |
| V5 | `subscriptions` |
| V6 | `tasks`, `task_completions` |
| V7 | `timeline_events` |
| V8 | `reminder_rules`, `scheduled_notifications` |
| V9 | `subscription_plan`, `subscription_plan_feature` |
| V10 | Subscription limits & relations |

---

## API Endpoints (Summary)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/v1/auth/register` | Register a new user |
| `POST` | `/v1/auth/login` | Login and receive access + refresh token |
| `POST` | `/v1/auth/refresh` | Refresh access token via HttpOnly cookie |
| `POST` | `/v1/auth/logout` | Invalidate session |
| `GET` | `/v1/auth/me` | Get current user profile |
| `GET` | `/v1/auth/salt` | Get encryption salt by email |
| `GET` | `/v1/vault` | List vault documents |
| `POST` | `/v1/vault` | Upload encrypted document |
| `GET` | `/v1/vault/{id}/download` | Download and decrypt document |
| `DELETE` | `/v1/vault/{id}` | Delete document |
| `GET/POST` | `/v1/tasks` | List / create tasks |
| `PUT/DELETE` | `/v1/tasks/{id}` | Update / delete task |
| `GET/POST` | `/v1/timeline` | List / create timeline events |
| `GET/POST` | `/v1/subscriptions` | List / create subscriptions |
| `GET/POST` | `/v1/inbox` | List / capture inbox items |
| `GET` | `/v1/dashboard` | Dashboard aggregated stats |
| `POST` | `/v1/payment/checkout` | Create Stripe checkout session |
| `GET` | `/v1/plans` | List available subscription plans |

> Full interactive documentation available at **`/swagger-ui.html`** [in local development]

---

## Docker Services

```yaml
services:
  db:        # PostgreSQL 15 on port 5431
  minio:     # MinIO object storage on ports 9000 / 9001 (console)
  backend:   # Spring Boot API on port 9090
  frontend:  # Angular dev server on port 4200
```

All services communicate over the `jenseiroku-network` bridge network.


---

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---
