# FixLocal Microservices Migration

## Current status

The original backend was a **modular monolith** (single Spring Boot app, single deployable unit).

A microservice split has now been scaffolded under `fixlocal-backend/`:

- `api-gateway` (port `8080`) – routes all client traffic
- `auth-service` (port `8081`) – `/api/v1/auth/**`
- `user-service` (port `8082`) – `/api/v1/users/**`, `/api/v1/tradespersons/**`
- `booking-service` (port `8084`) – `/api/v1/bookings/**` (except payment sub-paths)
- `chat-service` (port `8085`) – `/api/v1/chat/**`
- `notification-service` (port `8086`) – `/api/v1/notifications/**`
- `payment-service` (port `8087`) – `/api/v1/bookings/{bookingId}/payments/**` and `/api/v1/payments/**`
- `review-service` (port `8088`) – `/api/v1/reviews/**`
- `dispute-service` (port `8089`) – `/api/v1/disputes/**`
- `testimonial-service` (port `8090`) – `/api/v1/testimonials/**`
- `admin-service` (port `8091`) – `/api/v1/admin/**` (aggregates admin operations via internal service contracts)

## Routing map

Gateway routes:

- `/api/v1/auth/**` -> `auth-service`
- `/api/v1/users/**`, `/api/v1/tradespersons/**` -> `user-service`
- `/api/v1/bookings/**/payments/**`, `/api/v1/payments/**` -> `payment-service`
- `/api/v1/bookings/**` -> `booking-service`
- `/api/v1/chat/**` -> `chat-service`
- `/api/v1/notifications/**` -> `notification-service`
- `/api/v1/reviews/**` -> `review-service`
- `/api/v1/disputes/**` -> `dispute-service`
- `/api/v1/testimonials/**` -> `testimonial-service`
- `/api/v1/admin/**` -> `admin-service`

Frontend can continue calling `http://localhost:8080`.

## Run locally (JDK 17 required)

> Your current machine is using Java 8 for Maven. Switch `JAVA_HOME` to JDK 17 first.

Start services in separate terminals:

```bash
mvn -f fixlocal-backend/common-lib/pom.xml clean install -DskipTests
mvn -f fixlocal-backend/auth-service/pom.xml spring-boot:run
mvn -f fixlocal-backend/user-service/pom.xml spring-boot:run
mvn -f fixlocal-backend/booking-service/pom.xml spring-boot:run
mvn -f fixlocal-backend/chat-service/pom.xml spring-boot:run
mvn -f fixlocal-backend/notification-service/pom.xml spring-boot:run
mvn -f fixlocal-backend/payment-service/pom.xml spring-boot:run
mvn -f fixlocal-backend/review-service/pom.xml spring-boot:run
mvn -f fixlocal-backend/dispute-service/pom.xml spring-boot:run
mvn -f fixlocal-backend/testimonial-service/pom.xml spring-boot:run
mvn -f fixlocal-backend/admin-service/pom.xml spring-boot:run
mvn -f fixlocal-backend/api-gateway/pom.xml spring-boot:run
```

> `common-lib` contains shared JWT and exception classes used by all services.

## Build / test automation (new reactor)

- A top-level aggregator POM now lives at `fixlocal-backend/pom.xml`. Run the entire backend from the repo root:

  ```bash
  cd fixlocal-backend
  mvn clean verify
  ```

- To build/test a subset with dependencies (e.g., booking-service plus common-lib):

  ```bash
  cd fixlocal-backend
  mvn -pl booking-service -am clean verify
  ```

  `-am` ensures shared modules such as `common-lib` are built first.

- For a single service without rebuilding its dependencies (useful when artifacts are already in the local repo):

  ```bash
  mvn -f fixlocal-backend/booking-service/pom.xml clean verify
  ```

These commands replace the older `-pl` invocations that failed due to the missing reactor.

Optional DB isolation env vars:

- `BOOKING_MONGO_URI`
- `CHAT_MONGO_URI`
- `NOTIFICATION_MONGO_URI`
- `PAYMENT_MONGO_URI`
- `REVIEW_MONGO_URI`
- `DISPUTE_MONGO_URI`
- `TESTIMONIAL_MONGO_URI`

If these are not set, each service falls back to `MONGO_URI`.

## Notes

- This is a **strangler-style migration baseline**: domain code is split into independently deployable services, but shared model/repository code is still duplicated between services.
- Next hardening step is to remove duplicated classes via service-owned contracts (REST/Feign/events) and separate databases per service.

### ✅ Further decomposition completed

As requested, the architecture was further broken down from the earlier split:

- `review-service`, `dispute-service`, and `testimonial-service` were split as dedicated domain services.
- API Gateway routes were updated to route these domains directly to their services.
- Each new service was pruned to domain-relevant files plus minimal linkage/security files.

### ✅ Internal service contracts added (review/dispute decoupling)

To start removing direct cross-domain data coupling, internal contract APIs were added and wired:

- `user-service`
  - `GET /internal/users/{id}`
  - `GET /internal/users/by-email?email=...`
  - `PUT /internal/users/{id}/ratings/{rating}`

- `booking-service`
  - `GET /internal/bookings/{bookingId}`
  - `PUT /internal/bookings/{bookingId}/review`

`review-service` and `dispute-service` now consume these endpoints for user/booking linkage instead of requiring local domain repositories for those concerns.

New internal base-url config keys:

- `USER_SERVICE_BASE_URL` (default `http://localhost:8082`)
- `BOOKING_SERVICE_BASE_URL` (default `http://localhost:8084`)

### ✅ Strict microservice file isolation enforced

Per your requirement, `review-service` and `dispute-service` were cleaned so they no longer keep copied user/booking domain files.

- Removed duplicated cross-service artifacts (DTOs, repositories, models) that belonged to other domains.
- Kept only service-owned domain classes:
  - `review-service`: `Review` domain + review DTOs/controllers/repository
  - `dispute-service`: `Dispute` domain + dispute DTOs/controllers/repository
- Cross-service linkage now happens only via HTTP contracts to owner services:
  - user data from `user-service` internal APIs
  - booking data from `booking-service` internal APIs

Implementation note: review/dispute services now parse linked responses as generic payload maps instead of importing copied DTO classes from other services.

## Migration progress

### ✅ Step 1 completed: enforce service ownership boundaries

- Gateway remains the public entrypoint and routes ownership to dedicated services.
- Auth domain is owned by `auth-service`.
- Users/tradespersons domain is owned by `user-service`.

### ✅ Step 2 completed: remove duplicated auth/tradesperson code from legacy placeholder service

The following overlap artifacts were removed from the legacy placeholder service because ownership is now in
`auth-service` and `user-service`:

- Controllers: `AuthController`, `UserController`, `TradespersonController`
- Services: `AuthService`, `TradespersonService`
- DTOs: `AuthResponse`, `RegisterRequest`, `LoginRequest`, `ForgotPasswordRequest`, `TradespersonDTO`

Core-domain APIs were moved into dedicated services (booking/chat/review/dispute/notification/testimonial/admin).

### ✅ Admin/core extraction completed

- `admin-service` is now active and owns `/api/v1/admin/**` through the gateway.
- `user-service`, `booking-service`, and `chat-service` expose dedicated internal APIs used by admin aggregation.
- The placeholder legacy service has been retired and removed from runtime orchestration.

### Next steps (recommended order)

1. Move inter-service calls from raw `RestTemplate` to typed clients (OpenFeign / contract clients).
2. Split persistence by service (separate Mongo DB/schema per service in all environments).
3. Add centralized discovery/config (Eureka + Config Server) if dynamic routing is needed.
4. Add contract tests + integration smoke tests in CI for gateway-to-service compatibility.