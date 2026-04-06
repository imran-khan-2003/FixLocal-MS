# FixLocal

FixLocal is a **location-aware marketplace** that lets homeowners discover, chat with, negotiate, and track verified tradespeople in real-time. The platform combines a Spring Boot + MongoDB backend with a React/Vite frontend, WebSocket messaging, escrow-style bookings, and dispute tooling for admins.

```
Frontend (React/Vite) ──▶ REST (Axios) ─┐
                                         │
WebSocket (STOMP/SockJS) ◀───────────────┼── Spring Boot 3 (Java 17)
                                         │
MongoDB + Aggregations ◀──────────────────┘
```

## 🔑 Product Pillars

1. **Identity & Safety** – Unified registration with KYC fields, verification badges, availability toggles, role-based access, and JWT-secured APIs.
2. **Search & Discovery** – City + skill aware search with rating/radius hints, inline service catalogs, and curated categories.
3. **Bookings & Negotiation** – Multi-step booking lifecycle (request → offers → accept → en route → arrived → complete) with escrow hooks.
4. **Live Tracking & Alerts** – Leaflet maps, OSRM routing, STOMP WebSockets for live ETA, and push-style notifications.
5. **Trust & Recovery** – Dispute console, audit-ready notification history, admin workflows to suspend or verify tradespeople.

## 🧱 Architecture Deep Dive

### Backend (Spring Boot 3)

| Concern | Implementation |
|---------|----------------|
| **Framework** | Spring Boot 3.5, Web, Validation, Actuator |
| **Data Layer** | Spring Data MongoDB + custom aggregations (see `UserRepository`) |
| **Auth** | Spring Security + JWT (custom filter, BCrypt), role-based endpoints |
| **Realtime** | STOMP over WebSocket (`/ws` endpoint) with JWT channel interceptor |
| **Domain** | Users, Bookings (+`PriceOffer`), LiveLocation, Dispute, Chat, Review, Testimonial |
| **Services** | `BookingService`, `UserService`, `NotificationService`, `EscrowService`, etc. |

Key backend highlights:
- **Booking orchestration** (`BookingService`): concurrency-safe acceptance, counter-offers, escrow placeholders (`initiatePayment`, `capturePayment`), live location storage (`LiveLocationRepository`), and event fan-out via STOMP topics.
- **User lifecycle** (`UserService` + `UserController`): profile editing, skill tag sanitization, service catalog CRUD, availability toggle syncing to `Status` enums.
- **Notifications + Events** (`NotificationService`): persists Mongo documents and publishes Booking/Location events to `/topic/notifications/{userId}` and booking-specific topics.
- **SecurityConfig**: stateless sessions, CORS enabled, JWT filter inserted before `UsernamePasswordAuthenticationFilter`, granular `requestMatchers` for public routes.
- **WebSocketConfig**: `/ws` SockJS endpoint, `/topic` broker, `JwtChannelInterceptor` to validate STOMP connects.
- **Repositories**: Mongo queries for city/occupation filtering, regex search, paginated stats, and custom aggregation for average ratings.

### Frontend (React 18 + Vite)

| Concern | Implementation |
|---------|----------------|
| **Router & Shell** | React Router v6 (`App.jsx`, `routes/`) |
| **State** | Context API (`AuthContext` for token persistence + profile hydration), custom hooks for live location |
| **UI** | TailwindCSS utility classes, custom component library under `src/components` |
| **APIs** | Axios clients (`api/axios`, `adminService`, `userService`, `testimonialService`, booking/dispute services) with token injection |
| **Realtime** | `@stomp/stompjs` + `sockjs-client` for WebSocket subscriptions; `react-leaflet` + `leaflet` for maps |
| **UX Flows** | Pages for Home, Register, Login, SearchResults, Profile, WorkerProfile, dashboards (user/tradesperson/admin) |

Notable frontend modules:
- **Home.jsx**: marketing hero, service/category shortcuts, testimonial fetch (`testimonialApi`), concierge CTA.
- **Register.jsx**: multi-role onboarding with location dropdown (`constants/locations.js`).
- **Dashboard pages**: `UserDashboard`, `TradespersonDashboard`, `AdminDashboard`, `Disputes`, `MyDisputes`, `TradespersonHistory`, etc., reflecting backend booking/dispute APIs.
- **Live Location Hooks**: subscription to `/topic/bookings/{id}/location`, map visualisation with stale detection (as documented in `docs/LIVE-LOCATION.md`).
- **AuthContext.jsx**: localStorage token caching, `/users/me` hydration, normalized role inference for legacy payloads.

### Docs & QA
- `docs/VERIFICATION.md`: manual checklist for dispute workflow + live navigation QA.
- `docs/LIVE-LOCATION.md`: describes REST + WebSocket payloads, dashboard behaviour, and manual QA steps.
- `docs/api/`: REST reference (not shown here but mentioned for future maintainers).

## End-to-end Feature Map

1. **Account & Verification**
   - Auth routes (`/api/v1/auth/**`) handle register/login, issuing JWT.
   - Admin endpoints verify tradespeople, block/unblock users (`AdminController`).
   - Data seeder populates testimonials for demo builds.

2. **Discovery & Profiles**
   - `TradespersonController` + `UserRepository` filter by city/occupation/status.
   - `Profile.jsx` & `WorkerProfile.jsx` show ratings, skill tags, service offerings.

3. **Booking Lifecycle**
   - Users create bookings; tradespeople respond with counter-offers.
   - Escrow hooks (initiate → authorize → capture/refund) built into `BookingService` for future payment gateway integration.
   - Dashboard cards show statuses (`PENDING`, `ACCEPTED`, `EN_ROUTE`, `ARRIVED`, `COMPLETED`).

4. **Live Location & Notifications**
   - Tradesperson sends coordinates via REST; backend stores TTL’d `LiveLocation` docs and emits WebSocket events.
   - Users subscribe via STOMP, see Leaflet map + distance badge (OSRM route optional).
   - NotificationService pushes booking/dispute events and unread counts to `/topic/notifications/{userId}`.

5. **Disputes & Reviews**
   - `DisputeController` surfaces creation, messaging, admin updates; frontends at `pages/dashboard/Disputes.jsx`, `MyDisputes.jsx`, `TradespersonDisputes.jsx`.
   - `ReviewController` supports post-job feedback; aggregated rating accessible on profile cards.

6. **Admin Visibility**
   - `AdminController` returns paginated users/tradespersons, booking stats, verification toggles.
   - `AdminDashboard.jsx` (plus `TradespersonRatings.jsx`, `TradespersonCurrentBooking.jsx`) provide cohort analytics, repeated offender views, and manual overrides.

## Tech Stack (Detailed)

| Layer | Libraries / Tools |
|-------|-------------------|
| **Frontend** | React 18, Vite 4, React Router 6, TailwindCSS 3, Axios, `@stomp/stompjs`, `sockjs-client`, `react-leaflet`, `leaflet`, `react-phone-input-2`, ESLint |
| **Backend** | Java 17, Spring Boot 3.5, Spring Data MongoDB, Spring Security, Spring WebSocket, Lombok, jjwt, MongoTemplate Aggregations, Actuator |
| **Database** | MongoDB (Bookings, Users, LiveLocation, Notifications, Reviews, Disputes, Testimonials) |
| **Messaging** | STOMP over WebSocket with SockJS fallback |
| **Build/Tooling** | Maven Wrapper, npm, ESLint, Tailwind CLI |

## Local Development

```bash
# Backend (microservices)
powershell -ExecutionPolicy Bypass -File microservices/restart-all.ps1

# Frontend
cd fixlocal-frontend
npm install
npm run dev

# Visit http://localhost:5173
```

MongoDB connection details are configured via Spring Boot application properties (not included in the repo). Ensure a local Mongo instance is running or update the connection string accordingly.

## Observability & Ops
- **Actuator** exposes health metrics for Kubernetes or VM probes.
- **Auditability**: Notifications + Disputes persist message history for compliance.
- **Role-based method security** (`@EnableMethodSecurity`) keeps service methods scoped even if endpoints evolve.

## Roadmap Ideas
- Native payment gateway + automatic escrow release.
- Push notifications (FCM/Web Push) mirroring WebSocket events.
- Auto-scheduling + route optimization for multi-job days.
- AI-assisted skill tagging & pricing suggestions during onboarding.

---

### Further Reading
- [`docs/VERIFICATION.md`](docs/VERIFICATION.md) – manual QA for dispute + navigation flows.
- [`docs/LIVE-LOCATION.md`](docs/LIVE-LOCATION.md) – how live tracking works end to end.
- [`docs/api/`](docs/api/) – REST reference (endpoints, payloads, auth requirements).

Happy fixing! 🔧