## FixLocal Backend Architecture

### 1. Platform Overview

FixLocal is a location-aware marketplace that connects end users with local tradespeople (electricians, plumbers, carpenters, etc.). The backend is built with Spring Boot 3, secured with JWT, and stores domain data in MongoDB. Real-time interactions (booking lifecycle events, live location streaming, unread notifications) are delivered over STOMP WebSockets.

### 2. Layered System Design

| Layer | Responsibilities | Key Packages |
|-------|------------------|--------------|
| **API** | REST + WebSocket endpoints, request validation, DTO marshalling | `com.fixlocal.controller`, `com.fixlocal.websocket` |
| **Application Services** | Orchestrate domain workflows, enforce invariants, publish booking/location events | `com.fixlocal.service` |
| **Domain Model** | Rich MongoDB aggregates, enumerations, DTO mappers | `com.fixlocal.model`, `com.fixlocal.dto` |
| **Infrastructure** | Mongo repositories, security, WebSocket config, background jobs | `com.fixlocal.repository`, `com.fixlocal.config`, `com.fixlocal.security` |

### 3. Core Domain Aggregates

* **User** – Represents both end users and tradespersons. Important fields:
  * `role`, `workingCity`, `occupation`, `experience`, `status`, `available`, `verified`, `blocked`
  * `currentBookingId` – ties an active booking to a tradesperson
  * `averageRating`, `totalReviews`, `completedJobs`
  * `profileImage`, `bio`, `phone`

* **Booking** – User request for a service at a location/time window.
  * Booking lifecycle: `PENDING → ACCEPTED → EN_ROUTE → ARRIVED → COMPLETED` with side branches to `REJECTED` or `CANCELLED`.
  * Contains service metadata (address, description, negotiated price) and cancellation details.
  * Dynamic pricing: bookings keep a full `offerHistory` of `PriceOffer` entries, track who made the latest offer, and indicate whose response is pending so counter-offers alternate. Accepting an offer marks it as final and sets the booking price while still in `PENDING` state.

* **Tradesperson profile** – Users with `TRADESPERSON` role can curate:
  * `skillTags` (up to 15) describing specialties for search/filtering.
  * `serviceOfferings` collection with per-service name, description, base price, and duration; managed via `/api/v1/users/me/services` CRUD APIs.

* **Review** – Immutable feedback tied to a completed booking.

* **Notification** – Persistent audit of important events (`BOOKING_CREATED`, `BOOKING_ACCEPTED`, …) with read/unread status.

* **Conversation & ChatMessage** – Booking-scoped conversations streaming over `/topic/chat/{conversationId}` with attachment metadata for compliance/audit.

* **Escrow/Payment** – Booking tracks `paymentStatus` and `paymentIntentId` with state machine (INITIATED → AUTHORIZED → CAPTURED/REFUNDED). `EscrowService` enforces capture only after completion.

* **LiveLocation** – Stores the last GPS coordinates emitted by a tradesperson for an accepted booking. TTL index cleans stale entries automatically.

### 4. Booking & Notification Workflow

1. User invokes `POST /api/v1/bookings` with the desired tradesperson, city, slot, and address.
2. Service validates availability, prevents overlap, persists booking, and notifies the tradesperson via:
   * **Notification** document for durability.
   * **WebSocket event** on `/topic/bookings/{tradespersonId}` so any connected dashboard updates instantly.
3. Tradesperson can `accept`, `reject`, or let other bookings proceed (optimistic locking ensures only available tradespersons can accept).
4. Once accepted, tradesperson publishes a `start-trip` event triggering `EN_ROUTE` state and periodic `LiveLocation` updates (`POST /api/v1/realtime/location`).
5. User receives live map updates via `/topic/bookings/{bookingId}/location` plus textual state changes via notifications.
6. Tradesperson ends the job via `complete` endpoint, which releases their availability, increments `completedJobs`, enables user reviews, and closes WebSocket streams.

### 5. Real-Time Messaging

* WebSocket endpoint: `/ws` using STOMP over SockJS with JWT handshake.
* Topics:
  * `/topic/bookings/{userId}` – booking lifecycle updates for a user.
  * `/topic/bookings/tradesperson/{tradespersonId}` – requests & status changes for a tradesperson.
  * `/topic/bookings/{bookingId}/location` – high-frequency GPS updates.
  * `/topic/notifications/{userId}` – unread counts and new notification payloads.
* `BookingEventPublisher` + `LocationEventPublisher` services push both persistent notifications and transient events.

### 6. Dashboards

* **User dashboard**: upcoming bookings, live tracking, ability to cancel, see past jobs and reviews.
* **Tradesperson dashboard**: queue of pending requests, live trip state (start/arrive/complete), ability to toggle availability, and metrics on completed jobs, ratings, and earnings.
* **Admin dashboard**: user/tradesperson management, verification, system-wide stats.

### 7. Security & Auth

* JWT-secured APIs with stateless sessions.
* Method-level authorization for admin-only routes.
* Request validation via Jakarta Validation, including granular checks (booking time windows, rating ranges, etc.).

### 8. Testing & Quality Gates

* Unit tests for services (booking lifecycle, notifications, rating aggregation).
* Integration tests hitting Mongo via Testcontainers (future work) or embedded Mongo.
* `mvn verify` ensures compilation & tests.

### 9. Operations & Configuration

* MongoDB indexes defined with Spring annotations (`@CompoundIndex`, TTL on LiveLocation).
* Centralized configuration via `application.yml` plus environment overrides.
* Logging via SLF4J with structured messages per booking or notification event.
