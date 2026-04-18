# FixLocal Backend (Microservices)

This directory contains the complete backend for FixLocal, split into Spring Boot microservices behind a Spring Cloud API Gateway.

## Stack

- Java 17
- Spring Boot 3.5.x
- Spring Security + JWT
- Spring Data MongoDB
- Spring Cloud Gateway
- Maven multi-module reactor

## Service Catalog

| Service | Port | Main Public Base Path | Purpose |
|---|---:|---|---|
| `api-gateway` | 8080 | n/a (entrypoint) | Single client entrypoint + route forwarding + CORS |
| `auth-service` | 8081 | `/api/v1/auth/**` | Register, login, forgot-password, auth payload encryption key |
| `user-service` | 8082 | `/api/v1/users/**`, `/api/v1/tradespersons/**` | Profile CRUD, availability, skill tags, services, tradesperson discovery |
| `booking-service` | 8084 | `/api/v1/bookings/**` | Booking lifecycle, offers, live-location REST, booking stats |
| `chat-service` | 8085 | `/api/v1/chat/**` | Conversation creation, messages, file attachment download |
| `notification-service` | 8086 | `/api/v1/notifications/**` | Notification listing and read state |
| `payment-service` | 8087 | `/api/v1/bookings/{id}/payments/**`, `/api/v1/payments/**` | Escrow payment state transitions |
| `review-service` | 8088 | `/api/v1/reviews/**` | Add review and list tradesperson reviews |
| `dispute-service` | 8089 | `/api/v1/disputes/**` | Dispute create/list/update/messaging |
| `testimonial-service` | 8090 | `/api/v1/testimonials/**` | Public testimonials + authenticated submission |
| `admin-service` | 8091 | `/api/v1/admin/**` | Admin users/trades/bookings/stats aggregation |

## Gateway Routing (Configured)

From `api-gateway/src/main/resources/application.yml`:

- `/api/v1/auth/**` → `auth-service`
- `/api/v1/users/**`, `/api/v1/tradespersons/**` → `user-service`
- `/api/v1/bookings/*/payments/**`, `/api/v1/payments/**` → `payment-service`
- `/api/v1/bookings/**` → `booking-service`
- `/api/v1/chat/**` → `chat-service`
- `/api/v1/notifications/**` → `notification-service`
- `/api/v1/reviews/**` → `review-service`
- `/api/v1/disputes/**` → `dispute-service`
- `/api/v1/testimonials/**` → `testimonial-service`
- `/api/v1/admin/**` → `admin-service`

Frontend should call: `http://localhost:8080`

## Internal Service Contracts

The backend already uses internal HTTP contracts for decoupling:

### user-service internal APIs
- `GET /internal/users/{id}`
- `GET /internal/users/by-email?email=...`
- `GET /internal/users/admin/users?...`
- `GET /internal/users/admin/stats`
- `PUT /internal/users/{id}/ratings/{rating}`
- `PUT /internal/users/{id}/block`
- `PUT /internal/users/{id}/unblock`
- `PUT /internal/users/{id}/verify`

### booking-service internal APIs
- `GET /internal/bookings/{bookingId}`
- `PUT /internal/bookings/{bookingId}/review`
- `GET /internal/bookings/admin/bookings?...`
- `GET /internal/bookings/admin/stats`
- `GET /internal/bookings/stats/user/{userId}`
- `GET /internal/bookings/stats/tradesperson/{tradespersonId}`

### chat-service internal APIs
- `GET /internal/chat/admin/stats`

### notification-service internal APIs
- `POST /internal/notifications`

## Security Model

- Each service is **stateless** (`SessionCreationPolicy.STATELESS`)
- JWT filter used for authenticated routes (except open/public matchers)
- Method-level security via `@EnableMethodSecurity`
- `auth-service` exposes `/api/v1/auth/**` publicly
- Public/permit-all examples:
  - `user-service`: `/api/v1/tradespersons/**`, `/internal/users/**`
  - `booking-service`: `/internal/bookings/**`
  - `chat-service`: `/internal/chat/**`
  - `notification-service`: `/internal/notifications/**`
  - `testimonial-service`: `GET /api/v1/testimonials/**`

## Data & Configuration

Default Mongo URI fallback in most services:

```text
mongodb://localhost:27017/fixlocal
```

Optional per-service DB URI overrides:

- `BOOKING_MONGO_URI`
- `CHAT_MONGO_URI`
- `NOTIFICATION_MONGO_URI`
- `PAYMENT_MONGO_URI`
- `REVIEW_MONGO_URI`
- `DISPUTE_MONGO_URI`
- `TESTIMONIAL_MONGO_URI`

Common environment values:

- `MONGO_URI`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `APP_CORS_ALLOWED_ORIGINS`
- `HTTP_CLIENT_CONNECT_TIMEOUT_MS`
- `HTTP_CLIENT_READ_TIMEOUT_MS`

Internal base URL values:

- `USER_SERVICE_BASE_URL` (default `http://localhost:8082`)
- `BOOKING_SERVICE_BASE_URL` (default `http://localhost:8084`)
- `CHAT_SERVICE_BASE_URL` (default `http://localhost:8085`)
- `NOTIFICATION_SERVICE_BASE_URL` (used by booking-service, default `http://localhost:8086`)

Auth payload encryption values:

- `AUTH_PAYLOAD_ENCRYPTION_ENABLED`
- `AUTH_PAYLOAD_ENCRYPTION_REQUIRED`
- `AUTH_PAYLOAD_ENCRYPTION_KEY_ID`
- `AUTH_PAYLOAD_ENCRYPTION_PUBLIC_KEY_PEM`
- `AUTH_PAYLOAD_ENCRYPTION_PRIVATE_KEY_PEM`

## Build & Run

> Ensure `JAVA_HOME` points to JDK 17.

### Build all services

```bash
cd fixlocal-backend
mvn clean verify
```

### Build one service with required modules

```bash
cd fixlocal-backend
mvn -pl booking-service -am clean verify
```

### Run a single service

```bash
mvn -f fixlocal-backend/booking-service/pom.xml spring-boot:run
```

### Windows orchestration scripts

- `start-all-services.ps1` — starts all services in background, logs to `logs/*.log`
- `check-services.ps1` — checks `/actuator/health` on service ports
- `restart-all.ps1` — kills old listeners, restarts backend + frontend, verifies health

Examples:

```powershell
powershell -ExecutionPolicy Bypass -File .\start-all-services.ps1
powershell -ExecutionPolicy Bypass -File .\check-services.ps1
```

## Notable Domain Behaviors

- **Booking**: supports negotiation (`submitOffer`, `acceptOffer`) and lifecycle transitions (`accept`, `reject`, `start-trip`, `arrived`, `complete`, `cancel`)
- **Payment**: escrow-style state machine (`INITIATED`, `AUTHORIZED`, `CAPTURED`, `REFUNDED`)
- **Review**: enforces completed-booking + one-review-per-booking
- **Dispute**: role-aware access checks, threaded dispute messages
- **Admin**: aggregates metrics by calling user/booking/chat internal endpoints

## Important Implementation Note (Realtime Live Location)

Frontend includes STOMP/SockJS support for live location topics, but this backend currently exposes **REST live-location APIs only** (`POST/GET /api/v1/bookings/{id}/location`) and does not include websocket broker config classes in the present code state. Align docs/features accordingly if websocket broadcasting is later added.

## Health Endpoints

All services expose Actuator health/info:

- `/actuator/health`
- `/actuator/info`

---

For frontend integration details, see: `../fixlocal-frontend/README.md`

## Deep-Dive: Public API Surface by Service

This section maps directly to the controller code currently in this repo.

### auth-service (`/api/v1/auth`)

- `POST /register` → register user/tradesperson/admin account
- `POST /login` → issue JWT + user DTO
- `GET /encryption-key` → publish RSA public key + key id for frontend encryption
- `POST /forgot-password` → reset password (encrypted payload supported)

Implementation notes:
- `PayloadEncryptionService` supports RSA-OAEP-SHA256 decryption for password fields.
- Encryption can be toggled/required via `AUTH_PAYLOAD_ENCRYPTION_*` config.
- If PEM keys are absent, service generates an in-memory keypair on startup.

### user-service

Public endpoints:
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`
- `PATCH /api/v1/users/me/availability?available=true|false`
- `PUT /api/v1/users/me/skill-tags`
- `POST /api/v1/users/me/services`
- `PUT /api/v1/users/me/services/{serviceId}`
- `DELETE /api/v1/users/me/services/{serviceId}`
- `DELETE /api/v1/users/me`

Tradesperson discovery endpoints:
- `GET /api/v1/tradespersons/search`
- `GET /api/v1/tradespersons/{id}`

Internal endpoints:
- `/internal/users/{id}`
- `/internal/users/by-email`
- `/internal/users/dashboard-profile`
- `/internal/users/{id}/ratings/{rating}`
- `/internal/users/admin/users`
- `/internal/users/{id}/block`
- `/internal/users/{id}/unblock`
- `/internal/users/{id}/verify`
- `/internal/users/admin/stats`

Implementation notes:
- Skill tags are normalized, deduplicated, and capped at 15 entries.
- Service offerings are owned by tradespersons and use generated IDs.
- Admin stats include average platform rating via repository aggregation.

### booking-service

Public endpoints:
- `POST /api/v1/bookings`
- `POST /api/v1/bookings/{bookingId}/offers`
- `PATCH /api/v1/bookings/{bookingId}/offers/{offerId}/accept`
- `PATCH /api/v1/bookings/{bookingId}/accept`
- `PATCH /api/v1/bookings/{bookingId}/reject`
- `PATCH /api/v1/bookings/{bookingId}/start-trip`
- `PATCH /api/v1/bookings/{bookingId}/arrived`
- `PATCH /api/v1/bookings/{bookingId}/complete`
- `PATCH /api/v1/bookings/{bookingId}/cancel`
- `GET /api/v1/bookings/{bookingId}`
- `GET /api/v1/bookings/user`
- `GET /api/v1/bookings/tradesperson`
- `GET /api/v1/bookings/stats`
- `POST /api/v1/bookings/{bookingId}/location`
- `GET /api/v1/bookings/{bookingId}/location`

Internal endpoints:
- `/internal/bookings/{bookingId}`
- `/internal/bookings/{bookingId}/review`
- `/internal/bookings/admin/bookings`
- `/internal/bookings/admin/stats`
- `/internal/bookings/stats/user/{userId}`
- `/internal/bookings/stats/tradesperson/{tradespersonId}`

Implementation notes:
- Enforces role ownership checks on all booking actions.
- Negotiation flow is turn-based (`awaitingResponseFrom`) and state-aware.
- Accept booking uses a Mongo conditional update to transition tradesperson `AVAILABLE -> BUSY` safely.
- Live-location stale threshold is 300 seconds (`LIVE_LOCATION_STALE_THRESHOLD_SECONDS`).
- Booking events are sent as internal notifications to notification-service.
- On complete/cancel, live location record is deleted.

### payment-service

Public endpoints (both path styles accepted):
- `POST /api/v1/bookings/{bookingId}/payments/initiate?amount=...`
- `POST /api/v1/bookings/{bookingId}/payments/authorize`
- `POST /api/v1/bookings/{bookingId}/payments/capture`
- `POST /api/v1/bookings/{bookingId}/payments/refund`
- `POST /api/v1/payments/bookings/{bookingId}/initiate`
- `POST /api/v1/payments/bookings/{bookingId}/authorize`
- `POST /api/v1/payments/bookings/{bookingId}/capture`
- `POST /api/v1/payments/bookings/{bookingId}/refund`

Implementation notes:
- Cannot capture before booking status is `COMPLETED`.
- Prevents duplicate capture/refund operations.
- Uses booking document fields `paymentStatus` and `paymentIntentId`.

### chat-service

Public endpoints:
- `GET /api/v1/chat/conversations/{bookingId}`
- `GET /api/v1/chat/conversations/{conversationId}/messages`
- `POST /api/v1/chat/bookings/{bookingId}/messages` (multipart form-data)
- `GET /api/v1/chat/messages/{messageId}/attachment`

Internal endpoints:
- `GET /internal/chat/admin/stats`

Implementation notes:
- Only booking participants can send/read chat.
- Attachment max size is 5 MB.
- Default attachment storage is local filesystem directory `chat_attachments`.
- Conversation unread counters are tracked per side.

### notification-service

Public endpoints:
- `GET /api/v1/notifications`
- `PUT /api/v1/notifications/{id}/read`
- `PUT /api/v1/notifications/read-all`

Internal endpoints:
- `POST /internal/notifications`

Implementation notes:
- Notifications are persisted in Mongo and scoped by authenticated user.
- `markAllAsRead` bulk-updates unread notifications for current user.

### review-service

Public endpoints:
- `POST /api/v1/reviews/{bookingId}`
- `GET /api/v1/reviews/tradesperson/{tradespersonId}`

Implementation notes:
- Review creation allowed only for booking owner (`USER`).
- Booking must be `COMPLETED`.
- Enforces one review per booking.
- Calls booking-service internal review update and user-service rating update.

### dispute-service

Public endpoints:
- `POST /api/v1/disputes`
- `GET /api/v1/disputes` (admin only)
- `GET /api/v1/disputes/{id}`
- `GET /api/v1/disputes/booking/{bookingId}`
- `GET /api/v1/disputes/mine`
- `PUT /api/v1/disputes/{id}`
- `POST /api/v1/disputes/{id}/messages`

Implementation notes:
- Dispute details are enriched from user-service + booking-service.
- Access checks allow admin and booking participants.
- Message timeline includes sender role/name enrichment when available.

### testimonial-service

Public endpoints:
- `GET /api/v1/testimonials?limit=6`
- `POST /api/v1/testimonials`

Implementation notes:
- Write endpoint requires authenticated role among USER/TRADESPERSON/ADMIN.

### admin-service

Public endpoints (admin role required):
- `GET /api/v1/admin/users`
- `GET /api/v1/admin/tradespersons` and `GET /api/v1/admin/trades`
- `PUT /api/v1/admin/users/{id}/block`
- `PUT /api/v1/admin/users/{id}/unblock`
- `PUT /api/v1/admin/tradespersons/{id}/verify`
- `GET /api/v1/admin/bookings`
- `GET /api/v1/admin/stats`

Implementation notes:
- Aggregates data from internal user/booking/chat services.
- Returns combined metrics for dashboard KPIs.

## Deep-Dive: Security Behavior

All services:
- Stateless JWT security
- CSRF disabled
- CORS enabled
- Method security enabled (`@EnableMethodSecurity`)

Notable permit-all routes:
- auth-service: `/api/v1/auth/**`
- user-service: `/api/v1/tradespersons/**`, `/internal/users/**`
- booking-service: `/internal/bookings/**`
- chat-service: `/internal/chat/**`
- notification-service: `/internal/notifications/**`
- testimonial-service: `/api/v1/testimonials/**`
- all services: actuator health/info endpoints

## Deep-Dive: Configuration Model

### Common patterns across services

- `JWT_SECRET`
- `JWT_EXPIRATION`
- `APP_CORS_ALLOWED_ORIGINS`
- `HTTP_CLIENT_CONNECT_TIMEOUT_MS`
- `HTTP_CLIENT_READ_TIMEOUT_MS`

### Database URI precedence examples

- booking-service: `BOOKING_MONGO_URI` → fallback `MONGO_URI`
- chat-service: `CHAT_MONGO_URI` → fallback `MONGO_URI`
- notification-service: `NOTIFICATION_MONGO_URI` → fallback `MONGO_URI`
- payment-service: `PAYMENT_MONGO_URI` → fallback `MONGO_URI`
- review-service: `REVIEW_MONGO_URI` → fallback `MONGO_URI`
- dispute-service: `DISPUTE_MONGO_URI` → fallback `MONGO_URI`
- testimonial-service: `TESTIMONIAL_MONGO_URI` → fallback `MONGO_URI`

### Internal service base URLs

- review/dispute/admin use `USER_SERVICE_BASE_URL` and/or `BOOKING_SERVICE_BASE_URL`
- admin also uses `CHAT_SERVICE_BASE_URL`
- booking uses `NOTIFICATION_SERVICE_BASE_URL`

## Deep-Dive: Operations and Scripts

### `start-all-services.ps1`
- Starts each service via Maven `spring-boot:run` in hidden cmd process
- Writes logs to `fixlocal-backend/logs/<service>.log`
- Skips startup if `/actuator/health` already responds on target port

### `check-services.ps1`
- Probes `/actuator/health` on all service ports
- Prints UP/DOWN summary and exits non-zero if any service is down

### `restart-all.ps1`
- Stops listeners on backend ports + frontend 5173
- Restarts all backend services using start script
- Starts frontend dev server (`npm run dev`) and verifies TCP/HTTP health

## Known Limitations / Alignment Notes

1. Frontend includes STOMP client plumbing, while backend currently relies on REST live-location endpoints and does not expose websocket broker config in code.
2. API testing guide in `docs/api` may include legacy references; controller files in each service are the source of truth for active endpoints.