# FixLocal API Testing Guide

This runbook describes how to exercise every FixLocal API (auth, users, tradespersons, bookings, chat, payments, admin) using cURL/Postman. Follow these steps in sequence to initialize test data and verify the full product flow.

---

## 1. Prerequisites

| Requirement | Notes |
|-------------|-------|
| Java 17 + Maven | `mvn spring-boot:run` to start the backend |
| MongoDB local instance | Default URI (localhost:27017) per `application.yml` |
| HTTP client | Postman collection or cURL (examples below) |
| JWT secret | Use the same secret as configured in `application.yml` when decoding tokens |

### Test Accounts

1. **Admin** – register via `/api/v1/auth/register` then manually set role in Mongo (or create seed data) and capture JWT.
2. **User** – register normally; this account will book services.
3. **Tradesperson** – register and later verify via admin endpoint.

Store the JWTs returned by `/api/v1/auth/login`:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@mail.com","password":"password"}'
```

Response contains `token` used in `Authorization: Bearer <token>` headers.

---

## 2. Authentication & User Management

### Register & Login
- `POST /api/v1/auth/register` – create user/tradesperson/admin (role defaults to USER unless admin sets it).
- `POST /api/v1/auth/login` – returns JWT for subsequent calls.

### Profile Management (User JWT)
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me` with `{ name, workingCity, bio, phone, skillTags[] }`
- `PUT /api/v1/users/me/skill-tags` to replace tags.
- `POST /api/v1/users/me/services` to add service offerings (tradesperson only).

Sample skill-tag update:

```bash
curl -X PUT http://localhost:8080/api/v1/users/me/skill-tags \
  -H "Authorization: Bearer <TRADES_JWT>" \
  -H "Content-Type: application/json" \
  -d '{"tags":["plumbing","emergency","repairs"]}'
```

---

## 3. Tradesperson Search & Geo Tests

Endpoint: `GET /api/v1/tradespersons/search?city=Bangalore&occupation=Plumber&tag=emergency&latitude=12.97&longitude=77.59&radiusKm=10`

Verify responses:
- Only verified, available tradespersons in the city.
- `distanceKm` populated when lat/lng provided.
- Filtering by `tag` and `minRating` works.

---

## 4. Booking Lifecycle & Negotiation

1. **Create booking** – `POST /api/v1/bookings` (User JWT) with `tradespersonId`, `bookingStartTime`, `bookingEndTime`, address, offer amount.
2. **Negotiation** – `POST /api/v1/bookings/{id}/offers` (alternating user/tradesperson JWTs) to submit counter-offers.
3. **Accept offer** – `PATCH /api/v1/bookings/{id}/offers/{offerId}/accept` (opposite party).
4. **Trade actions** – Tradesperson uses `PATCH /api/v1/bookings/{id}/accept`, `/start-trip`, `/arrived`, `/complete`.
5. **Cancellation** – either role via `PATCH /api/v1/bookings/{id}/cancel` with reason.

Checklist:
- Ensure notifications (WebSocket `/topic/bookings/...` and `/topic/notifications/...`) fire – observe via WebSocket client.
- Confirm status transitions follow `PENDING → ACCEPTED → EN_ROUTE → ARRIVED → COMPLETED`.
- Validate booking stats via `GET /api/v1/bookings/stats`.

Sample booking creation:

```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Authorization: Bearer <USER_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
        "tradespersonId":"<TP_ID>",
        "bookingStartTime":"2026-03-25T05:30:00Z",
        "bookingEndTime":"2026-03-25T07:30:00Z",
        "serviceAddress":"221B Baker St",
        "serviceDescription":"Fix heater",
        "offerAmount":150
      }'
```

---

## 5. Chat & Attachments

1. Fetch conversation: `GET /api/v1/chat/conversations/{bookingId}` (user or tradesperson JWT) – conversation auto-creates.
2. Send message: `POST /api/v1/chat/bookings/{bookingId}/messages` using `multipart/form-data` with fields `content` and optional `attachment`.
3. Stream updates via WebSocket topic `/topic/chat/{conversationId}` – verify `ChatMessage` payloads and attachment metadata.
4. Retrieve history: `GET /api/v1/chat/conversations/{conversationId}/messages?page=0&size=20`.

Sample cURL for sending text + file:

```bash
curl -X POST http://localhost:8080/api/v1/chat/bookings/<BOOKING_ID>/messages \
  -H "Authorization: Bearer <USER_JWT>" \
  -F content="Can you confirm arrival time?" \
  -F attachment=@/path/to/note.txt
```

---

## 6. Escrow / Payments

Sequence (admin/test harness can call these once booking price is finalized):

1. `POST /api/v1/bookings/{id}/payments/initiate?amount=150` (User JWT) – sets payment intent.
2. `POST .../payments/authorize` – marks `paymentStatus=AUTHORIZED`.
3. After booking is `COMPLETED`, `POST .../payments/capture` – payment captured.
4. Optional refund: `POST .../payments/refund`.

Validate booking object shows `paymentStatus` transitions and `paymentIntentId` not null.

---

## 7. Notifications API

- `GET /api/v1/notifications` – paginated list (user or tradesperson JWT).
- `PUT /api/v1/notifications/{id}/read`
- `PUT /api/v1/notifications/read-all`

Confirm events from bookings/chat escalate into notifications (see Mongo `notifications` collection).

---

## 8. Admin Workflows

Use Admin JWT (role=ADMIN).

1. **User/tradesperson lists** – `GET /api/v1/admin/users`, `/tradespersons` (Pageable params `page`,`size`).
2. **Verification & blocking** – `PATCH /api/v1/admin/tradespersons/{id}/verify`, `/users/{id}/block`, `/users/{id}/unblock`.
3. **Bookings list** – `GET /api/v1/admin/bookings`.
4. **Stats** – `GET /api/v1/admin/stats` returns extended KPIs (counts, average rating, active conversations, pending verifications, blocked accounts). Validate numbers against DB.

---

## 9. Running the Full Regression Suite

1. **Start services** – `mvn spring-boot:run` (or run jar) and ensure Mongo is running.
2. **Seed data** – optionally use scripts to insert baseline admin/tradesperson/user accounts.
3. **Execute flows** – Follow sections above in order: auth → user/tradesperson data → booking negotiations → payments → chat → notifications → admin dashboards.
4. **Automated tests** – run `mvn test` (currently 21 unit tests). Future work could add integration tests (Postman/Newman, Karate, etc.).
5. **Monitoring WebSockets** – use tools like wscat/Postman to subscribe to `/topic/bookings/...`, `/topic/chat/...`, `/topic/notifications/...` to confirm real-time events.

---

## 10. Tips

- Always include `Authorization: Bearer <token>` except for auth endpoints.
- Use ISO8601 timestamps in UTC for booking endpoints.
- For multipart chat messages, ensure `content` field is present even if there is an attachment.
- Admin actions should be performed carefully; blocking/verification impacts availability.

This guide can be imported into Postman by creating a collection with each step as a request; update tokens in the “Authorization” tab to run sequentially.
