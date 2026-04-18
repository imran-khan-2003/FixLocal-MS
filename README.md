# FixLocal

FixLocal is a role-based services marketplace where customers discover and book local tradespersons, negotiate pricing, chat in-app, track booking progress, raise disputes, and submit reviews. The platform is implemented as a **microservices backend + React frontend**.

---

## 1) Repository Overview

```text
FixLocal/
├─ fixlocal-backend/      # Spring Boot microservices + API Gateway + orchestration scripts
├─ fixlocal-frontend/     # React/Vite SPA with user, tradesperson, and admin dashboards
├─ docs/                  # Feature notes, verification checklist, API assets
├─ generate_postman_collection.py
└─ README.md
```

### Core capabilities implemented

- Authentication + account registration/login/forgot-password
- RSA-OAEP encrypted auth payload support (frontend + auth-service)
- User/tradesperson profile management
- Public tradesperson search with city/occupation/rating/radius filters
- Booking lifecycle + counter-offer negotiation
- Escrow-like payment status flow
- In-booking chat with file attachments
- Notification center (read / read-all)
- Review and rating submission after completion
- Dispute workflow with admin review/actions
- Testimonials
- Admin moderation + cross-service analytics

---

## 2) Runtime Architecture (Current)

```text
Frontend (React @ :5173)
          |
          v
API Gateway (@ :8080)
  ├─ auth-service         :8081
  ├─ user-service         :8082
  ├─ booking-service      :8084
  ├─ chat-service         :8085
  ├─ notification-service :8086
  ├─ payment-service      :8087
  ├─ review-service       :8088
  ├─ dispute-service      :8089
  ├─ testimonial-service  :8090
  └─ admin-service        :8091

MongoDB (default fallback per service: mongodb://localhost:27017/fixlocal)
```

### Important implementation note

The frontend contains STOMP/SockJS code for `/ws` and `/topic/bookings/{bookingId}/location`, but the backend codebase currently exposes **live-location via REST endpoints** and does not include active WebSocket broker configuration classes. Treat realtime websocket broadcasting as not fully wired in this repository state.

---

## 3) Domain Models & State Highlights

### Roles

- `USER`
- `TRADESPERSON`
- `ADMIN`

### Tradesperson availability status

- `AVAILABLE`
- `BUSY`
- `OFFLINE`

### Booking status lifecycle

- `PENDING`
- `ACCEPTED`
- `EN_ROUTE`
- `ARRIVED`
- `COMPLETED`
- `REJECTED`
- `CANCELLED`

### Payment state lifecycle

- `INITIATED`
- `AUTHORIZED`
- `CAPTURED`
- `REFUNDED`
- `FAILED`

---

## 4) End-to-End User Journeys

### Customer journey

1. Register/login (encrypted password payloads supported).
2. Search tradespersons by city/service and optional location radius.
3. Open profile, capture/select address + coordinates, create booking.
4. Negotiate offer while booking is `PENDING`.
5. Track booking state (`ACCEPTED → EN_ROUTE → ARRIVED → COMPLETED`).
6. Use chat + attachments during the job.
7. Initiate payment transitions and/or raise dispute if needed.
8. Submit review after completion.

### Tradesperson journey

1. Manage profile, services, skill tags, availability.
2. View incoming requests and quote/accept/reject.
3. Progress trip states and share live location via REST.
4. Communicate with customer through chat.
5. Handle disputes and monitor ratings history.

### Admin journey

1. View platform KPIs aggregated from user/booking/chat services.
2. Search users/tradespersons and block/unblock accounts.
3. Verify tradespersons.
4. Review and update dispute statuses, add notes, and moderate respondents.

---

## 5) Local Setup (Windows)

### Prerequisites

- Java 17
- Maven
- Node.js + npm
- MongoDB running locally (or custom `MONGO_URI`/service-specific URIs)

### Start backend services

```powershell
powershell -ExecutionPolicy Bypass -File .\fixlocal-backend\start-all-services.ps1
```

Health check:

```powershell
powershell -ExecutionPolicy Bypass -File .\fixlocal-backend\check-services.ps1
```

### Start frontend

```powershell
cd .\fixlocal-frontend
npm install
npm run dev
```

Open: `http://localhost:5173`

### Full restart helper (backend + frontend)

```powershell
powershell -ExecutionPolicy Bypass -File .\fixlocal-backend\restart-all.ps1
```

---

## 6) Docs Index

- Backend deep dive: [`fixlocal-backend/README.md`](fixlocal-backend/README.md)
- Frontend deep dive: [`fixlocal-frontend/README.md`](fixlocal-frontend/README.md)
- Live location behavior note: [`docs/LIVE-LOCATION.md`](docs/LIVE-LOCATION.md)
- Manual verification checklist: [`docs/VERIFICATION.md`](docs/VERIFICATION.md)
- API assets/testing docs: [`docs/api/`](docs/api/)

---

## 7) Tech Stack

- **Backend:** Spring Boot 3.5.x, Spring Security, Spring Data MongoDB, Spring Cloud Gateway, Actuator
- **Frontend:** React 18, React Router 6, Vite 4, TailwindCSS, Axios, Leaflet
- **Authentication:** JWT + RSA-OAEP auth payload encryption support
- **Build/Tooling:** Maven, npm, PowerShell orchestration scripts

---

For deep implementation details (endpoint maps, security behavior, service contracts, dashboard flows), use the backend and frontend READMEs linked above.