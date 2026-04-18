# FixLocal Frontend (React + Vite)

This package is the web client for FixLocal. It provides a single-page app for three roles:

- **USER** (customer)
- **TRADESPERSON**
- **ADMIN**

The frontend talks to backend microservices through the API Gateway (`:8080`) and handles booking flows, negotiation, chat, disputes, reviews, profile management, and admin operations.

---

## 1) Tech Stack

- **Framework:** React 18
- **Bundler:** Vite 4
- **Routing:** React Router 6
- **Styling:** TailwindCSS
- **HTTP:** Axios
- **Maps:** Leaflet + react-leaflet
- **Realtime client utilities:** `@stomp/stompjs` + `sockjs-client`
- **Forms/UI add-ons:** `react-phone-input-2`

---

## 2) Directory Structure

```text
fixlocal-frontend/
├─ src/
│  ├─ api/                 # axios instance + per-domain API helpers
│  ├─ components/          # reusable UI (BookingCard, ChatThread, maps, layout, etc.)
│  ├─ context/             # AuthContext (token/user session lifecycle)
│  ├─ hooks/               # feature hooks (bookings/live location)
│  ├─ pages/               # route pages (public + dashboards)
│  ├─ routes/              # AppRoutes + role guards
│  ├─ utils/               # geocode, auth payload encryption, STOMP helpers
│  ├─ App.jsx
│  └─ main.jsx
├─ docs/
│  └─ UI-DESIGN.md
├─ public/
├─ .env.example
└─ package.json
```

---

## 3) Environment Configuration

Copy `.env.example` to `.env` and configure:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=http://localhost:8080/ws
VITE_API_BASE_URL_HTTPS=https://localhost:8443
```

### How env vars are used in code

- `VITE_API_BASE_URL`
  - consumed by `src/api/axios.js`
  - if `/api/v1` suffix is missing, it is auto-appended
  - hard guard blocks old monolith base `http://localhost:8079`

- `VITE_WS_BASE_URL`
  - consumed by `src/utils/liveLocation.js`
  - used for SockJS factory when STOMP client is enabled

- `VITE_API_BASE_URL_HTTPS`
  - present in template env file
  - not currently referenced by source code

---

## 4) Run / Build / Lint

```bash
cd fixlocal-frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

Available scripts:

- `npm run dev` → start dev server
- `npm run build` → production build
- `npm run preview` → preview build output
- `npm run lint` → ESLint

---

## 5) Routing & Role Guards

Routing is defined in `src/routes/AppRoutes.jsx`.

### Public routes

- `/`
- `/login`
- `/forgot-password`
- `/register`
- `/search`
- `/worker/:id`
- `/terms`
- `/privacy`

### Protected routes

- `/profile` (any authenticated user)

### USER routes

- `/dashboard`
- `/dashboard/current`
- `/dashboard/history`
- `/dashboard/disputes/mine`

### TRADESPERSON routes

- `/dashboard/tradesperson`
- `/dashboard/tradesperson/current`
- `/dashboard/tradesperson/history`
- `/dashboard/tradesperson/ratings`
- `/dashboard/tradesperson/disputes`

### ADMIN routes

- `/dashboard/admin`
- `/dashboard/disputes`

### Guard behavior

`ProtectedRoute` checks:

1. authenticated token presence
2. role allowed for route

Unauthorized users are redirected to `/login`, and wrong-role users are redirected to `/`.

---

## 6) Authentication & Session Lifecycle

Auth is managed in `src/context/AuthContext.jsx`.

### Storage

- localStorage key: `fixlocal_auth`
- persisted fields: `token`, `user`

### Login behavior

- `login({ token, user })` stores token immediately
- if user object missing, profile is hydrated from `/users/me`

### Hydration behavior

- on app start: restore localStorage session
- if token exists but profile missing/partial: fetch `/users/me`
- on hydration failure: auto logout and session cleanup

### Role normalization

If `user.role` missing in older payloads, fallback inference uses:

- `roles[0]`
- `authorities[0]`
- `roleName`

---

## 7) Auth Payload Encryption (Frontend)

For login/register/forgot-password flows, frontend uses `src/utils/authEncryption.js`:

1. fetches `GET /auth/encryption-key`
2. imports RSA public key via Web Crypto
3. encrypts password fields using `RSA-OAEP (SHA-256)`
4. sends encrypted values + `encryptionKeyId`

Used by:

- `Login.jsx`
- `Register.jsx`
- `ForgotPassword.jsx`

---

## 8) API Layer (`src/api`)

### `axios.js`

- sets base URL to gateway `/api/v1`
- injects bearer token from `fixlocal_auth`

### Domain services

- `bookingService.js`
  - bookings list/create/status transitions
  - offer submit/accept
  - live-location REST
  - payment state actions

- `chatService.js`
  - conversation fetch
  - paginated messages
  - multipart message send with optional attachment

- `disputeService.js`
  - create dispute
  - list mine
  - list by booking
  - add dispute message

- `reviewService.js`
  - add review for booking
  - list tradesperson reviews

- `notificationService.js`
  - list notifications
  - mark one read
  - mark all read

- `adminService.js`
  - admin stats
  - users/trades listing with search/pagination
  - block/unblock
  - dispute list/detail/update + notes

- `dashboardService.js`
  - computes dashboard summaries from live endpoint data
  - no single monolith dashboard endpoint dependency

- `userService.js`
  - update profile
  - delete account

---

## 9) Feature Hooks

### `useCurrentBooking`

- finds current active booking for USER
- derives en-route booking for tracking
- loads conversation + messages
- wires `useLiveLocation`

### `useLiveLocation`

- initial fetch via `GET /bookings/{id}/location`
- computes stale state (>5 minutes)
- optional STOMP subscription to `/topic/bookings/{id}/location`

### `useTradespersonBookings`

- loads tradesperson bookings
- computes:
  - active list
  - history list
  - current booking (`ACCEPTED/EN_ROUTE/ARRIVED`)

---

## 10) Core UI/UX Flows

### Home (`Home.jsx`)

- city + service search
- GPS-based search with reverse geocoding
- testimonials fetch + authenticated testimonial submission

### Search + Worker Profile

- search filters (city/service/price/rating/sort)
- worker profile booking initiation with:
  - address suggestions (Photon)
  - optional browser GPS capture
  - fallback geocode to coordinates before booking creation

### User dashboard flows

- booking cards + actions
- offer negotiation (counter offer / accept)
- payment actions
- chat panel with attachment downloads
- dispute creation
- in-card rating/review submission for completed jobs

### Tradesperson dashboard flows

- request filters and lifecycle actions
- quote updates for pending requests
- cancellation flow
- chat panel
- disputes panel

### Admin dashboard flows

- KPI cards (users, tradespersons, bookings, chats, verification queue, blocked accounts)
- searchable user/trades tables
- block/unblock controls
- dispute management drawer:
  - status update
  - note timeline
  - respondent moderation

---

## 11) Chat & Attachment UX Details

`ChatThread` supports:

- date-grouped message rendering
- message timestamps
- multipart file upload (`image/*`, `application/pdf`)
- attachment download from `GET /chat/messages/{id}/attachment`
- loading/error handling for downloads and message actions

---

## 12) Mapping & Live Tracking UX

### `LiveLocationMap`

- renders tradesperson marker and optional destination marker
- attempts OSRM route polyline
- falls back to straight line when route unavailable
- shows distance + ETA when route metadata available
- stale indicator is reflected from live-location state

### `TradespersonLocationPanel`

- manual latitude/longitude input
- browser geolocation quick-fill
- POST live location updates
- optional auto-sharing loop (10s interval)
- route preview + distance/ETA to destination

---

## 13) Known Alignment Notes

1. Frontend has STOMP/SockJS live-location client code.
2. Current backend codebase primarily exposes REST live-location endpoints and does not include explicit websocket broker config classes.
3. For production, ensure backend realtime infrastructure is enabled if websocket subscription behavior is expected.

---

## 14) Related Documentation

- UI notes: `fixlocal-frontend/docs/UI-DESIGN.md`
- Root docs: `../docs/LIVE-LOCATION.md`, `../docs/VERIFICATION.md`
- Backend details: `../fixlocal-backend/README.md`
